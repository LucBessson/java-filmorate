package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        return film;
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film film = createValidFilm();

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = createValidFilm();
        film.setName("");

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        Film film = createValidFilm();
        film.setName(null);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldAllowDescriptionWith200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldAllowReleaseDateDecember28_1895() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeDecember28_1895() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = createValidFilm();
        film.setDuration(0);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = createValidFilm();
        film.setDuration(-10);

        assertThrows(
                ValidationException.class,
                () -> filmController.create(film)
        );
    }
}