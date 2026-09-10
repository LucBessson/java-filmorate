package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
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

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} добавил пользователя {} в друзья", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        validateDifferentUsers(userId, friendId);

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} удалил пользователя {} из друзей", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = getUserOrThrow(userId);

        return user.getFriends().stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        validateDifferentUsers(userId, otherId);

        User user = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherId);

        Set<Integer> commonFriendIds = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonFriendIds.stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
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