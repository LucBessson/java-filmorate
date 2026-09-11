package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    User update(User user);

    Optional<User> getById(int id);

    Collection<User> getAll();

    void delete(int id);

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    List<Integer> getFriends(int userId);

    List<Integer> getCommonFriends(int userId, int otherUserId);
}