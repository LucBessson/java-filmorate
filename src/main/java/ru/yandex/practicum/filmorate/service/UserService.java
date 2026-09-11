package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage) {

        this.userStorage = userStorage;
    }

    public User create(User user) {
        setDefaultName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        getUserOrThrow(user.getId());
        setDefaultName(user);
        return userStorage.update(user);
    }

    public User getById(int id) {
        return getUserOrThrow(id);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        validateDifferentUsers(userId, friendId);

        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userStorage.addFriend(userId, friendId);

        log.info(
                "Пользователь {} добавил пользователя {} в друзья",
                userId,
                friendId
        );
    }

    public void removeFriend(int userId, int friendId) {
        validateDifferentUsers(userId, friendId);

        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userStorage.removeFriend(userId, friendId);

        log.info(
                "Пользователь {} удалил пользователя {} из друзей",
                userId,
                friendId
        );
    }

    public List<User> getFriends(int userId) {
        getUserOrThrow(userId);

        return userStorage.getFriends(userId).stream()
                .map(this::getUserOrThrow)
                .toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        validateDifferentUsers(userId, otherId);

        getUserOrThrow(userId);
        getUserOrThrow(otherId);

        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(this::getUserOrThrow)
                .toList();
    }

    private User getUserOrThrow(int userId) {
        return userStorage.getById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Пользователь с id " + userId + " не найден"));
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateDifferentUsers(int userId, int otherUserId) {
        if (userId == otherUserId) {
            throw new ValidationException(
                    "Пользователь не может выполнить операцию с самим собой");
        }
    }
}