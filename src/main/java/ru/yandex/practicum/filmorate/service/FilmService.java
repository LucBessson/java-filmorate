package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        getFilmOrThrow(film.getId());
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        return getFilmOrThrow(id);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);

        if (userStorage.getById(userId) == null) {
            throw new NotFoundException(
                    "Пользователь с id " + userId + " не найден"
            );
        }

        film.getLikes().add(userId);

        filmStorage.update(film);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);

        if (userStorage.getById(userId) == null) {
            throw new NotFoundException(
                    "Пользователь с id " + userId + " не найден"
            );
        }

        film.getLikes().remove(userId);

        filmStorage.update(film);

        log.info("Пользователь {} удалил лайк фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator
                        .comparingInt((Film film) -> film.getLikes().size())
                        .reversed())
                .limit(count)
                .toList();
    }

    private Film getFilmOrThrow(int id) {
        Film film = filmStorage.getById(id);

        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        return film;
    }
}