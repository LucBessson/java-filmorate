package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void shouldCreateFilm() {
        Film film = createFilm("Интерстеллар");

        Film savedFilm = filmStorage.create(film);

        assertThat(savedFilm.getId()).isPositive();
        assertThat(savedFilm.getName()).isEqualTo("Интерстеллар");
        assertThat(savedFilm.getDescription()).isEqualTo("Фантастический фильм");
        assertThat(savedFilm.getDuration()).isEqualTo(169);
        assertThat(savedFilm.getMpa())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 3);
    }

    @Test
    void shouldFindFilmById() {
        Film film = filmStorage.create(createFilm("Интерстеллар"));

        Optional<Film> result = filmStorage.getById(film.getId());

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(foundFilm -> {
                    assertThat(foundFilm.getId()).isEqualTo(film.getId());
                    assertThat(foundFilm.getName()).isEqualTo("Интерстеллар");
                });
    }

    @Test
    void shouldReturnEmptyWhenFilmDoesNotExist() {
        Optional<Film> result = filmStorage.getById(999);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(createFilm("Интерстеллар"));
        filmStorage.create(createFilm("Матрица"));

        Collection<Film> films = filmStorage.getAll();

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactly("Интерстеллар", "Матрица");
    }

    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.create(createFilm("Интерстеллар"));

        film.setName("Интерстеллар 2");
        film.setDescription("Новое описание");
        film.setDuration(180);

        Film updatedFilm = filmStorage.update(film);

        assertThat(updatedFilm.getId()).isEqualTo(film.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Интерстеллар 2");
        assertThat(updatedFilm.getDescription()).isEqualTo("Новое описание");
        assertThat(updatedFilm.getDuration()).isEqualTo(180);
    }

    @Test
    void shouldDeleteFilm() {
        Film film = filmStorage.create(createFilm("Интерстеллар"));

        filmStorage.delete(film.getId());

        assertThat(filmStorage.getById(film.getId())).isEmpty();
    }

    @Test
    void shouldSaveAndLoadGenres() {
        Film film = createFilm("Интерстеллар");
        film.setGenres(Set.of(
                new Genre(3, "Мультфильм"),
                new Genre(5, "Документальный")
        ));

        Film savedFilm = filmStorage.create(film);

        Film result = filmStorage.getById(savedFilm.getId()).orElseThrow();

        assertThat(result.getGenres())
                .extracting(Genre::getId)
                .containsExactly(3, 5);
    }

    @Test
    void shouldUpdateGenres() {
        Film film = createFilm("Интерстеллар");
        film.setGenres(Set.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        ));

        Film savedFilm = filmStorage.create(film);

        savedFilm.setGenres(Set.of(
                new Genre(4, "Триллер"),
                new Genre(6, "Боевик")
        ));

        filmStorage.update(savedFilm);

        Film result = filmStorage.getById(savedFilm.getId()).orElseThrow();

        assertThat(result.getGenres())
                .extracting(Genre::getId)
                .containsExactly(4, 6);
    }

    @Test
    void shouldAddLike() {
        User user = createUser("user1");
        Film film = filmStorage.create(createFilm("Интерстеллар"));

        filmStorage.addLike(film.getId(), user.getId());

        Film result = filmStorage.getById(film.getId()).orElseThrow();

        assertThat(result.getLikes())
                .containsExactly(user.getId());
    }

    @Test
    void shouldRemoveLike() {
        User user = createUser("user1");
        Film film = filmStorage.create(createFilm("Интерстеллар"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        Film result = filmStorage.getById(film.getId()).orElseThrow();

        assertThat(result.getLikes()).isEmpty();
    }

    @Test
    void shouldReturnPopularFilms() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");

        Film film1 = filmStorage.create(createFilm("Фильм 1"));
        Film film2 = filmStorage.create(createFilm("Фильм 2"));

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        filmStorage.addLike(film1.getId(), user1.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms)
                .extracting(Film::getId)
                .containsExactly(film2.getId(), film1.getId());
    }

    @Test
    void shouldLimitPopularFilms() {
        filmStorage.create(createFilm("Фильм 1"));
        filmStorage.create(createFilm("Фильм 2"));
        filmStorage.create(createFilm("Фильм 3"));

        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        film.setMpa(new Mpa(3, "PG-13"));
        return film;
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return userStorage.create(user);
    }
}