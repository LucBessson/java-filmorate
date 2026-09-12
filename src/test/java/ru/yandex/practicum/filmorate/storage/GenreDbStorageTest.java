package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        Collection<Genre> genres = genreStorage.getAll();

        assertThat(genres)
                .hasSize(6)
                .extracting(Genre::getId)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> result = genreStorage.getById(1);

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    void shouldReturnEmptyWhenGenreDoesNotExist() {
        Optional<Genre> result = genreStorage.getById(999);

        assertThat(result).isEmpty();
    }
}