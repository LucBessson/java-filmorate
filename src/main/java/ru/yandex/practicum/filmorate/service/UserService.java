package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        return userStorage.create(user);
    }

    public User update(User user) {
        getUserOrThrow(user.getId());
        return userStorage.update(user);
    }

    public User getById(int id) {
        return getUserOrThrow(id);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователь {} добавил пользователя {} в друзья", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
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
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherId);

        Set<Integer> commonFriendIds = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonFriendIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(int id) {
        User user = userStorage.getById(id);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return user;
    }
}