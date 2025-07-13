package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.SortBy;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.sql.Date;
import java.util.*;

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
        // Удаляем лайки, связанные с фильмом
        String deleteLikesSql = "DELETE FROM likes WHERE film_id = ?";
        jdbc.update(deleteLikesSql, filmId);
        // Удаляем связи с жанрами
        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteGenresSql, filmId);
        //Удаляем связи с режисерами
        String deleteDirectorsSql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbc.update(deleteDirectorsSql, filmId);
        // Удаляем сам фильм
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
        String insertLikeSql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(insertLikeSql, filmId, userId);
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
}
