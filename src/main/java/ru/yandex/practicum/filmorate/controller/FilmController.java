package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate MINIMUM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            String message = "Фильм с id " + film.getId() + " не найден";
            log.error(message);
            throw new NotFoundException(message);
        }
        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Получен запрос на получение списка фильмов");
        return films.values();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            String message = "Название фильма не может быть пустым";
            throw new ValidationException(message);
        }

        if (film.getDescription() != null
                && film.getDescription().length() > 200) {
            String message = "Описание фильма не может превышать 200 символов";
            throw new ValidationException(message);
        }

        if (film.getReleaseDate().isBefore(MINIMUM_RELEASE_DATE)) {
            String message = "Дата релиза не может быть раньше 28 декабря 1895 года";
            log.error(message);
            throw new ValidationException(message);
        }

        if (film.getDuration() <= 0) {
            String message = "Продолжительность фильма должна быть положительным числом";
            throw new ValidationException(message);
        }
    }
}