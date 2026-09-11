package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> getAll() {
        return jdbcTemplate.query(
                """
                        SELECT id, name
                        FROM mpa
                        ORDER BY id
                        """,
                mpaRowMapper()
        );
    }

    @Override
    public Optional<Mpa> getById(int id) {
        List<Mpa> ratings = jdbcTemplate.query(
                """
                        SELECT id, name
                        FROM mpa
                        WHERE id = ?
                        """,
                mpaRowMapper(),
                id
        );

        return ratings.stream().findFirst();
    }

    private org.springframework.jdbc.core.RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) ->
                new Mpa(
                        rs.getInt("id"),
                        rs.getString("name")
                );
    }
}