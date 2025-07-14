package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.SearchBy;
import ru.yandex.practicum.filmorate.controller.SortBy;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
@Qualifier("dbStorage")
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    final RatingRepository ratingRepository;
    final GenreRepository genreRepository;
    final DirectorRepository directorRepository;

    static final String INSERT_QUERY = "INSERT INTO film(name, description, release_date, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    static final String FIND_BY_ID_QUERY = "SELECT * FROM film WHERE id = ?";
    static final String UPDATE_QUERY = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, " +
            "rating_id = ? WHERE id = ?";
    static final String DELETE_QUERY = "DELETE FROM film WHERE id = ?";
    static final String FIND_ALL_QUERY = "SELECT * FROM film";
    static final String FIND_POPULAR_QUERY = "SELECT f.*, COUNT(l.user_id) AS likes_count " +
            "FROM film f LEFT JOIN likes l ON f.id = l.film_id GROUP BY f.id " +
            "ORDER BY likes_count DESC LIMIT ?";
    static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    static final String INSERT_FILM_DIRECTOR_QUERY = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    static final String DELETE_FILM_GENRE_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    static final String DELETE_FILM_DIRECTOR_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    static final String FIND_BY_DIRECTOR_SORT_YEAR = """
            SELECT
            	f.ID,
            	f.NAME,
            	f.DESCRIPTION,
            	f.RELEASE_DATE,
            	f.DURATION,
            	f.RATING_ID
            FROM film f
            INNER JOIN film_directors fd ON fd.FILM_ID = f.ID
            WHERE fd.DIRECTOR_ID = ?
            ORDER BY f.RELEASE_DATE ASC
            """;
    static final String FIND_BY_DIRECTOR_SORT_LIKES = """
            SELECT
            	f.ID,
            	f.NAME,
            	f.DESCRIPTION,
            	f.RELEASE_DATE,
            	f.DURATION,
            	f.RATING_ID,
            	count(l.*) AS likes
            FROM film f
            INNER JOIN film_directors fd ON fd.FILM_ID = f.ID
            LEFT JOIN likes l ON l.FILM_ID = f.ID
            WHERE fd.DIRECTOR_ID = ?
            GROUP BY f.ID,
            		f.NAME,
            		f.DESCRIPTION,
            		f.RELEASE_DATE,
            		f.DURATION,
            		f.RATING_ID
            ORDER BY likes desc
            """;

    static final String FIND_FILMS_BY_QUERY = """
            SELECT
            	f.ID,
            	f.NAME,
            	f.DESCRIPTION,
            	f.RELEASE_DATE,
            	f.DURATION,
            	f.RATING_ID,
            	count(l.*) AS likes
            FROM film f
            LEFT JOIN film_directors fd ON fd.FILM_ID = f.ID
            LEFT JOIN director d ON d.DIRECTOR_ID = fd.DIRECTOR_ID
            LEFT JOIN likes l ON l.FILM_ID = f.ID
            WHERE 1=1
            %s
            GROUP BY f.ID,
                    f.NAME,
                    f.DESCRIPTION,
                    f.RELEASE_DATE,
                    f.DURATION,
                    f.RATING_ID
            ORDER BY likes desc
            """;

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, RatingRepository ratingRepository,
                          GenreRepository genreRepository, DirectorRepository directorRepository) {
        super(jdbc, mapper, Film.class);
        this.ratingRepository = ratingRepository;
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
    }

    @Override
    public Film create(Film film) {
        Optional<Rating> rating = ratingRepository.findById(film.getMpa().getId());
        if (rating.isEmpty()) {
            throw new NotFoundException("Рейтинг не найден с ID: " + film.getMpa().getId());
        }

        List<Genre> genres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genres.addAll(film.getGenres());
        }
        Map<Long, Genre> genreMap = new LinkedHashMap<>();
        for (Genre genre : genres) {
            genreMap.putIfAbsent(genre.getId(), genre);
        }
        List<Genre> uniqueGenres = new ArrayList<>(genreMap.values());

        film.setMpa(rating.get());
        film.setGenres(uniqueGenres);

        Long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                rating.get().getId()
        );
        film.setId(id);

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> jdbc.update(INSERT_FILM_GENRE_QUERY, id, genre.getId()));
        }

        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director ->
                    jdbc.update(INSERT_FILM_DIRECTOR_QUERY, id, director.getId()));
        }

        return getFilmById(id).get();
    }

    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (film.getGenres() != null) {
            List<Genre> oldGenres = genreRepository.findGenresByFilmId(film.getId());
            boolean isNeedToDeleteOldGenres = !oldGenres.isEmpty();
            if (isNeedToDeleteOldGenres) {
                delete(DELETE_FILM_GENRE_QUERY, film.getId());
            }
            film.getGenres().forEach(genre -> jdbc.update(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId()));
        }

        if (film.getDirectors() != null) {
            List<Director> oldDirectors = directorRepository.findDirectorsByFilmId(film.getId());
            boolean isNeedToDeleteOldDirectors = !oldDirectors.isEmpty();
            if (isNeedToDeleteOldDirectors) {
                delete(DELETE_FILM_DIRECTOR_QUERY, film.getId());
            }
            film.getDirectors().forEach(director -> jdbc.update(INSERT_FILM_DIRECTOR_QUERY,
                    film.getId(), director.getId()));
        }
        return getFilmById(film.getId()).get();
    }

    @Override
    public boolean delete(Film film) {
        return delete(DELETE_QUERY, film.getId());
    }

    public boolean deleteFilmWithRelations(Long filmId) {
        String deleteLikesSql = "DELETE FROM likes WHERE film_id = ?";
        jdbc.update(deleteLikesSql, filmId);
        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteGenresSql, filmId);
        String deleteDirectorsSql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbc.update(deleteDirectorsSql, filmId);
        String deleteReviewSql = "DELETE FROM review WHERE film_id = ?";
        jdbc.update(deleteReviewSql, filmId);
        return delete(DELETE_QUERY, filmId);
    }


    @Override
    public Optional<Film> getFilmById(Long filmId) {
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    @Override
    public List<Film> getFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    public List<Film> getPopularFilms(Integer countFilms) {
        return findMany(FIND_POPULAR_QUERY, countFilms);
    }

    public void addLike(Long filmId, Long userId) {
        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, filmId, userId);
        if (count == 0) {
            String insertSql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbc.update(insertSql, filmId, userId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        String deleteLikeSql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(deleteLikeSql, filmId, userId);
    }

    @Override
    public List<Film> getFilmsByDirectorId(Long directorId, SortBy sortBy) {
        return switch (sortBy) {
            case YEAR -> findMany(FIND_BY_DIRECTOR_SORT_YEAR, directorId);
            case LIKES -> findMany(FIND_BY_DIRECTOR_SORT_LIKES, directorId);
        };
    }

    public List<Film> getFilmsByQuery(String text, List<SearchBy> filters) {
        if (filters.containsAll(List.of(SearchBy.TITLE, SearchBy.DIRECTOR))) {
            StringBuilder fltrs = new StringBuilder("AND (f.NAME ILIKE '%").append(text).append("%' ")
                    .append("OR d.DIRECTOR_FIRSTNAME ILIKE '%").append(text).append("%')");
            return findMany(String.format(FIND_FILMS_BY_QUERY, fltrs));
        } else {
            if (filters.size() > 1) {
                throw new ValidationException(filters.toString(),
                        String.format("Некорректно указаны параметры фильтрации. Допустимые значения:%s",
                                Arrays.toString(SearchBy.values()).toLowerCase()));
            }

            StringBuilder fltr = new StringBuilder("AND f.NAME ILIKE '%").append(text).append("%'");
            switch (filters.getFirst()) {
                case TITLE -> {

                }
                case DIRECTOR -> fltr = new StringBuilder("AND d.DIRECTOR_FIRSTNAME ILIKE '%")
                        .append(text).append("%'");
            }
            String q = String.format(FIND_FILMS_BY_QUERY, fltr);
            return findMany(q);
        }

    public List<Film> findPopularFilmsByGenreAndYear(int limit, Long genreId, Integer year) {
        String sql = "SELECT f.*, COUNT(l.user_id) AS likes_count " +
                "FROM film f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "JOIN film_genres fg ON f.id = fg.film_id " +
                "WHERE 1=1 ";

        if (genreId != null) {
            sql += "AND fg.genre_id = " + genreId + " ";
        }
        if (year != null) {
            sql += "AND EXTRACT(YEAR FROM f.release_date) = " + year + " ";
        }

        sql += "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT " + limit;

        return findMany(sql);
    }

    public List<Long> getLikedFilmsByUser(Long userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        return jdbc.queryForList(sql, Long.class, userId);
    }

    public List<Long> getUsersLikedSameFilms(List<Long> filmIds, Long ownUserId) {
        if (filmIds.isEmpty()) return List.of();

        String parSql = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));
        String sql = String.format("SELECT DISTINCT user_id FROM likes WHERE film_id IN (%s) AND user_id != ?", parSql);

        List<Object> params = new ArrayList<>(filmIds);
        params.add(ownUserId);

        return jdbc.queryForList(sql, Long.class, params.toArray());
    }

    public List<Long> getRecommendedFilmIds(Long userId, Long similarUserId) {
        String sql = "SELECT l.film_id FROM likes l WHERE l.user_id = ? AND l.film_id NOT IN (" +
                "SELECT film_id FROM likes WHERE user_id = ?)";
        return jdbc.queryForList(sql, Long.class, similarUserId, userId);
    }

    @Override
    public List<Film> getCommonLikedFilms(Long userId, Long friendId) {
        String sql = """
                SELECT f.*, COUNT(l2.user_id) AS like_count
                FROM film f
                JOIN likes l1 ON f.id = l1.film_id
                JOIN likes l2 ON f.id = l2.film_id
                WHERE l1.user_id = ?
                  AND f.id IN (SELECT film_id FROM likes WHERE user_id = ?)
                GROUP BY f.id
                ORDER BY like_count DESC
                """;

        return findMany(sql, userId, friendId);
    }
}
