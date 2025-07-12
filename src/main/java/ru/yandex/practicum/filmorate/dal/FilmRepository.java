package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
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

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, RatingRepository ratingRepository,
                          GenreRepository genreRepository) {
        super(jdbc, mapper, Film.class);
        this.ratingRepository = ratingRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public Film create(Film film) {
        Optional<Rating> rating = ratingRepository.findById(film.getMpa().getId());
        if (rating.isEmpty()) {
            throw new NotFoundException("Рейтинг не найден с ID: " + film.getMpa().getId());
        }

        List<Genre> genres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Optional<Genre> genreOpt = genreRepository.findById(genre.getId());
                if (genreOpt.isEmpty()) {
                    throw new NotFoundException("Жанр не найден с ID: " + genre.getId());
                }
                genres.add(genreOpt.get());
            }
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
            for (Genre genre : film.getGenres()) {
                String insertGenreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbc.update(insertGenreSql, id, genre.getId());
            }
        }

        return film;
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
        return film;
    }

    @Override
    public boolean delete(Film film) {
        return delete(DELETE_QUERY, film.getId());
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

    public List<Long> getLikedFilmsByUser(Long userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        return jdbc.queryForList(sql,Long.class, userId);
    }

    public List<Long> getUsersLikedSameFilms(List<Long> filmIds,Long ownUserId) {
        if (filmIds.isEmpty()) return List.of();

        String parSql = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));
        String sql = String.format("SELECT DISTINCT user_id FROM likes WHERE film_id IN (%s) AND user_id != ?",parSql);

        List<Object> params =  new ArrayList<>(filmIds);
        params.add(ownUserId);

        return jdbc.queryForList(sql,Long.class, params.toArray());
    }

    public List<Long> getRecommendedFilmIds(Long userId, Long similarUserId) {
        String sql = "SELECT l.film_id FROM likes l WHERE l.user_id = ? AND l.film_id NOT IN (" +
                "SELECT film_id FROM likes WHERE user_id = ?)";
        return jdbc.queryForList(sql,Long.class, similarUserId, userId);
    }
}
