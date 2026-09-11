package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("userDbStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users
                (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());

            if (user.getBirthday() == null) {
                statement.setObject(4, null);
            } else {
                statement.setDate(
                        4,
                        Date.valueOf(user.getBirthday())
                );
            }

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Не удалось получить id пользователя"
            );
        }

        user.setId(key.intValue());

        return getById(user.getId()).orElse(user);
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                """
                        UPDATE users
                        SET email = ?,
                            login = ?,
                            name = ?,
                            birthday = ?
                        WHERE id = ?
                        """,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() == null
                        ? null
                        : Date.valueOf(user.getBirthday()),
                user.getId()
        );

        return getById(user.getId()).orElse(user);
    }

    @Override
    public Optional<User> getById(int id) {
        List<User> users = jdbcTemplate.query(
                """
                        SELECT id,
                               email,
                               login,
                               name,
                               birthday
                        FROM users
                        WHERE id = ?
                        """,
                userRowMapper(),
                id
        );

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);

        loadFriends(user);

        return Optional.of(user);
    }

    @Override
    public Collection<User> getAll() {
        List<User> users = jdbcTemplate.query(
                """
                        SELECT id,
                               email,
                               login,
                               name,
                               birthday
                        FROM users
                        ORDER BY id
                        """,
                userRowMapper()
        );

        for (User user : users) {
            loadFriends(user);
        }

        return users;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update(
                "DELETE FROM users WHERE id = ?",
                id
        );
    }

    @Override
    public void addFriend(int userId, int friendId) {
        jdbcTemplate.update(
                """
                        MERGE INTO friendships (user_id, friend_id)
                        KEY (user_id, friend_id)
                        VALUES (?, ?)
                        """,
                userId,
                friendId
        );
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update(
                """
                        DELETE FROM friendships
                        WHERE user_id = ?
                          AND friend_id = ?
                        """,
                userId,
                friendId
        );
    }

    @Override
    public List<Integer> getFriends(int userId) {
        return jdbcTemplate.query(
                """
                        SELECT friend_id
                        FROM friendships
                        WHERE user_id = ?
                        ORDER BY friend_id
                        """,
                (rs, rowNum) -> rs.getInt("friend_id"),
                userId
        );
    }

    @Override
    public List<Integer> getCommonFriends(int userId, int otherUserId) {
        return jdbcTemplate.query(
                """
                        SELECT f1.friend_id
                        FROM friendships f1
                        JOIN friendships f2
                          ON f1.friend_id = f2.friend_id
                        WHERE f1.user_id = ?
                          AND f2.user_id = ?
                        ORDER BY f1.friend_id
                        """,
                (rs, rowNum) -> rs.getInt("friend_id"),
                userId,
                otherUserId
        );
    }

    private void loadFriends(User user) {
        user.setFriends(
                new java.util.HashSet<>(getFriends(user.getId()))
        );
    }

    private org.springframework.jdbc.core.RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));

            Date birthday = rs.getDate("birthday");

            if (birthday != null) {
                user.setBirthday(birthday.toLocalDate());
            }

            return user;
        };
    }
}