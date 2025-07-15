package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import java.util.*;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    private static final String FIND_ALL_QUERY = """
            SELECT
            	director_id,
            	director_firstname,
            	director_lastname
            FROM public.DIRECTOR
            ORDER BY DIRECTOR_ID ASC
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT
            	director_id,
            	director_firstname,
            	director_lastname
            FROM public.DIRECTOR
            WHERE director_id = ?
            """;

    private static final String FIND_DIRECTORS_BY_FILM_ID_QUERY = """
            SELECT
                d.director_id,
                d.director_firstname,
                d.director_lastname
            FROM public.DIRECTOR d
            INNER JOIN FILM_DIRECTORS f ON f.DIRECTOR_ID = d.DIRECTOR_ID
            WHERE f.FILM_ID = ?
            ORDER BY d.DIRECTOR_ID ASC
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO public.DIRECTOR
            (director_firstname, director_lastname)
            VALUES(?, ?);
            """;

    private static final String UPDATE_QUERY = """
            UPDATE PUBLIC.DIRECTOR
                SET
                    director_firstname = ?,
                    director_lastname = ?
            WHERE director_id = ?;
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM public.DIRECTOR
            WHERE director_id = ?;
            """;

    private static final String DELETE_FILMS_DIRECTOR_QUERY = """
            DELETE FROM public.FILM_DIRECTORS
            WHERE director_id = ?;
            """;

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper, Director.class);
    }

    public Director create(Director director) {
        Long id = insert(
                INSERT_QUERY,
                director.getFirstName(),
                director.getLastName()
        );
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        update(UPDATE_QUERY,
                director.getFirstName(),
                director.getLastName(),
                director.getId()
        );
        return director;
    }

    public boolean delete(Director director) {
        jdbc.update(DELETE_FILMS_DIRECTOR_QUERY, director.getId());
        return delete(DELETE_QUERY, director.getId());
    }

    public Optional<Director> findDirectorById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Director> getDirectors() {
        return findMany(FIND_ALL_QUERY);
    }

    public List<Director> findDirectorsByFilmId(Long filmId) {
        return findMany(FIND_DIRECTORS_BY_FILM_ID_QUERY, filmId);
    }
}
