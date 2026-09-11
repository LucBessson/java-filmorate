package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();

    private int nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = users.get(userId);

        if (user != null) {
            user.getFriends().add(friendId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = users.get(userId);

        if (user != null) {
            user.getFriends().remove(friendId);
        }
    }

    @Override
    public List<Integer> getFriends(int userId) {
        User user = users.get(userId);

        if (user == null) {
            return List.of();
        }

        return user.getFriends().stream().toList();
    }

    @Override
    public List<Integer> getCommonFriends(int userId, int otherUserId) {
        User user = users.get(userId);
        User otherUser = users.get(otherUserId);

        if (user == null || otherUser == null) {
            return List.of();
        }

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .toList();
    }
}