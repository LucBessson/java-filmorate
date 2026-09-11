package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> getAll() {
        return jdbcTemplate.query(
                """
                        SELECT id, name
                        FROM genres
                        ORDER BY id
                        """,
                genreRowMapper()
        );
    }

    @Override
    public Optional<Genre> getById(int id) {
        List<Genre> genres = jdbcTemplate.query(
                """
                        SELECT id, name
                        FROM genres
                        WHERE id = ?
                        """,
                genreRowMapper(),
                id
        );

        return genres.stream().findFirst();
    }

    private org.springframework.jdbc.core.RowMapper<Genre> genreRowMapper() {
        return (rs, rowNum) ->
                new Genre(
                        rs.getInt("id"),
                        rs.getString("name")
                );
    }
}