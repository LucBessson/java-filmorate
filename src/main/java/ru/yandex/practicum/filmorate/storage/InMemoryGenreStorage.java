package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InMemoryGenreStorage implements GenreStorage {

    private final List<Genre> genres = List.of(
            new Genre(1, "Комедия"),
            new Genre(2, "Драма"),
            new Genre(3, "Мультфильм"),
            new Genre(4, "Триллер"),
            new Genre(5, "Документальный"),
            new Genre(6, "Боевик")
    );

    @Override
    public Collection<Genre> getAll() {
        return genres;
    }

    @Override
    public Optional<Genre> getById(int id) {
        return genres.stream()
                .filter(genre -> genre.getId() == id)
                .findFirst();
    }
}