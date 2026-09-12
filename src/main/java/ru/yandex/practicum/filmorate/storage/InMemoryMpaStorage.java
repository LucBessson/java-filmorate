package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InMemoryMpaStorage implements MpaStorage {

    private final List<Mpa> ratings = List.of(
            new Mpa(1, "G"),
            new Mpa(2, "PG"),
            new Mpa(3, "PG-13"),
            new Mpa(4, "R"),
            new Mpa(5, "NC-17")
    );

    @Override
    public Collection<Mpa> getAll() {
        return ratings;
    }

    @Override
    public Optional<Mpa> getById(int id) {
        return ratings.stream()
                .filter(mpa -> mpa.getId() == id)
                .findFirst();
    }
}