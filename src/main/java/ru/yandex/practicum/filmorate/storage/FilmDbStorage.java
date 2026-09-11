package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("filmDbStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = """
                INSERT INTO films
                (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(
                    3,
                    film.getReleaseDate() == null
                            ? null
                            : Date.valueOf(film.getReleaseDate())
            );
            statement.setInt(4, film.getDuration());

            if (film.getMpa() == null) {
                statement.setObject(5, null);
            } else {
                statement.setInt(5, film.getMpa().getId());
            }

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Не удалось получить id фильма");
        }

        film.setId(key.intValue());

        saveGenres(film);

        return getById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() == null
                        ? null
                        : Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null
                        ? null
                        : film.getMpa().getId(),
                film.getId()
        );

        jdbcTemplate.update(
                "DELETE FROM film_genres WHERE film_id = ?",
                film.getId()
        );

        saveGenres(film);

        return getById(film.getId()).orElse(film);
    }

    @Override
    public Optional<Film> getById(int id) {
        List<Film> films = jdbcTemplate.query(
                """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """,
                filmRowMapper(),
                id
        );

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);

        loadGenres(film);
        loadLikes(film);

        return Optional.of(film);
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = jdbcTemplate.query(
                """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                ORDER BY f.id
                """,
                filmRowMapper()
        );

        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        return films;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update(
                "DELETE FROM films WHERE id = ?",
                id
        );
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query(
                """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name,
                       COUNT(l.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id,
                         f.name,
                         f.description,
                         f.release_date,
                         f.duration,
                         f.mpa_id,
                         m.name
                ORDER BY likes_count DESC, f.id
                LIMIT ?
                """,
                filmRowMapper(),
                count
        );

        for (Film film : films) {
            loadGenres(film);
            loadLikes(film);
        }

        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update(
                """
                MERGE INTO likes (film_id, user_id)
                KEY (film_id, user_id)
                VALUES (?, ?)
                """,
                filmId,
                userId
        );
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(
                """
                DELETE FROM likes
                WHERE film_id = ?
                  AND user_id = ?
                """,
                filmId,
                userId
        );
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(
                    """
                    INSERT INTO film_genres (film_id, genre_id)
                    VALUES (?, ?)
                    """,
                    film.getId(),
                    genre.getId()
            );
        }
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbcTemplate.query(
                """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """,
                (rs, rowNum) ->
                        new Genre(
                                rs.getInt("id"),
                                rs.getString("name")
                        ),
                film.getId()
        );

        film.setGenres(new java.util.HashSet<>(genres));
    }

    private void loadLikes(Film film) {
        List<Integer> likes = jdbcTemplate.query(
                """
                SELECT user_id
                FROM likes
                WHERE film_id = ?
                """,
                (rs, rowNum) -> rs.getInt("user_id"),
                film.getId()
        );

        film.setLikes(new java.util.HashSet<>(likes));
    }

    private org.springframework.jdbc.core.RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();

            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));

            Date releaseDate = rs.getDate("release_date");

            if (releaseDate != null) {
                film.setReleaseDate(releaseDate.toLocalDate());
            }

            film.setDuration(rs.getInt("duration"));

            int mpaId = rs.getInt("mpa_id");

            if (!rs.wasNull()) {
                film.setMpa(
                        new Mpa(
                                mpaId,
                                rs.getString("mpa_name")
                        )
                );
            }

            return film;
        };
    }
}