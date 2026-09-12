package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryGenreStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryMpaStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private InMemoryMpaStorage mpaStorage;
    private InMemoryGenreStorage genreStorage;


    @BeforeEach
    void setUp() {

        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        mpaStorage = new InMemoryMpaStorage();
        genreStorage = new InMemoryGenreStorage();

        filmService = new FilmService(
                filmStorage,
                userStorage,
                mpaStorage,
                genreStorage
        );
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setDuration(120);
        return filmStorage.create(film);
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@mail.ru");
        return userStorage.create(user);
    }

    @Test
    void shouldAddLikeToFilm() {
        Film film = createFilm("Интерстеллар");
        User user = createUser("user1");

        filmService.addLike(film.getId(), user.getId());

        Film result = filmService.getById(film.getId());

        assertTrue(result.getLikes().contains(user.getId()));
    }

    @Test
    void shouldNotAddDuplicateLike() {
        Film film = createFilm("Интерстеллар");
        User user = createUser("user1");

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user.getId());

        Film result = filmService.getById(film.getId());

        assertEquals(1, result.getLikes().size());
    }

    @Test
    void shouldRemoveLikeFromFilm() {
        Film film = createFilm("Интерстеллар");
        User user = createUser("user1");

        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());

        Film result = filmService.getById(film.getId());

        assertFalse(result.getLikes().contains(user.getId()));
    }

    @Test
    void shouldThrowExceptionWhenFilmDoesNotExist() {
        User user = createUser("user1");

        assertThrows(
                NotFoundException.class,
                () -> filmService.addLike(999, user.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        Film film = createFilm("Интерстеллар");

        assertThrows(
                NotFoundException.class,
                () -> filmService.addLike(film.getId(), 999)
        );
    }

    @Test
    void shouldReturnPopularFilmsSortedByLikes() {
        Film film1 = createFilm("Фильм 1");
        Film film2 = createFilm("Фильм 2");
        Film film3 = createFilm("Фильм 3");

        User user1 = createUser("user1");
        User user2 = createUser("user2");

        filmService.addLike(film1.getId(), user1.getId());

        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film2.getId(), user2.getId());

        List<Film> popular = filmService.getPopularFilms(3);

        assertEquals(film2.getId(), popular.get(0).getId());
        assertEquals(film1.getId(), popular.get(1).getId());
        assertEquals(film3.getId(), popular.get(2).getId());
    }

    @Test
    void shouldLimitNumberOfPopularFilms() {
        createFilm("Фильм 1");
        createFilm("Фильм 2");
        createFilm("Фильм 3");

        List<Film> popular = filmService.getPopularFilms(2);

        assertEquals(2, popular.size());
    }

    @Test
    void shouldThrowExceptionWhenPopularFilmsCountIsZero() {
        assertThrows(
                ValidationException.class,
                () -> filmService.getPopularFilms(0)
        );
    }

    @Test
    void shouldThrowExceptionWhenPopularFilmsCountIsNegative() {
        assertThrows(
                ValidationException.class,
                () -> filmService.getPopularFilms(-1)
        );
    }
}