package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void shouldFindAllMpa() {
        Collection<Mpa> ratings = mpaStorage.getAll();

        assertThat(ratings)
                .hasSize(5)
                .extracting(Mpa::getId)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void shouldFindMpaById() {
        Optional<Mpa> result = mpaStorage.getById(3);

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId()).isEqualTo(3);
                    assertThat(mpa.getName()).isEqualTo("PG-13");
                });
    }

    @Test
    void shouldReturnEmptyWhenMpaDoesNotExist() {
        Optional<Mpa> result = mpaStorage.getById(999);

        assertThat(result).isEmpty();
    }
}