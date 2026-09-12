package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void shouldCreateUser() {
        User user = createUser("user1");

        User savedUser = userStorage.create(user);

        assertThat(savedUser.getId()).isPositive();
        assertThat(savedUser.getEmail()).isEqualTo("user1@mail.ru");
        assertThat(savedUser.getLogin()).isEqualTo("user1");
        assertThat(savedUser.getName()).isEqualTo("user1");
    }

    @Test
    void shouldFindUserById() {
        User user = userStorage.create(createUser("user1"));

        Optional<User> result = userStorage.getById(user.getId());

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(foundUser -> {
                    assertThat(foundUser.getId()).isEqualTo(user.getId());
                    assertThat(foundUser.getLogin()).isEqualTo("user1");
                });
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result = userStorage.getById(999);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.create(createUser("user1"));
        userStorage.create(createUser("user2"));

        Collection<User> users = userStorage.getAll();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactly("user1", "user2");
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.create(createUser("user1"));

        user.setName("Новое имя");
        user.setEmail("new@mail.ru");

        User updatedUser = userStorage.update(user);

        assertThat(updatedUser.getId()).isEqualTo(user.getId());
        assertThat(updatedUser.getName()).isEqualTo("Новое имя");
        assertThat(updatedUser.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    void shouldDeleteUser() {
        User user = userStorage.create(createUser("user1"));

        userStorage.delete(user.getId());

        assertThat(userStorage.getById(user.getId())).isEmpty();
    }

    @Test
    void shouldAddFriend() {
        User user1 = userStorage.create(createUser("user1"));
        User user2 = userStorage.create(createUser("user2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        List<Integer> friends = userStorage.getFriends(user1.getId());

        assertThat(friends)
                .containsExactly(user2.getId());
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = userStorage.create(createUser("user1"));
        User user2 = userStorage.create(createUser("user2"));

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());

        List<Integer> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void shouldReturnFriends() {
        User user1 = userStorage.create(createUser("user1"));
        User user2 = userStorage.create(createUser("user2"));
        User user3 = userStorage.create(createUser("user3"));

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user1.getId(), user3.getId());

        List<Integer> friends = userStorage.getFriends(user1.getId());

        assertThat(friends)
                .containsExactlyInAnyOrder(
                        user2.getId(),
                        user3.getId()
                );
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = userStorage.create(createUser("user1"));
        User user2 = userStorage.create(createUser("user2"));
        User commonFriend = userStorage.create(createUser("common"));
        User onlyUser1Friend = userStorage.create(createUser("only1"));
        User onlyUser2Friend = userStorage.create(createUser("only2"));

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user1.getId(), onlyUser1Friend.getId());

        userStorage.addFriend(user2.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), onlyUser2Friend.getId());

        List<Integer> commonFriends =
                userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .containsExactly(commonFriend.getId());
    }

    @Test
    void friendshipShouldBeOneWay() {
        User user1 = userStorage.create(createUser("user1"));
        User user2 = userStorage.create(createUser("user2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        assertThat(userStorage.getFriends(user1.getId()))
                .containsExactly(user2.getId());

        assertThat(userStorage.getFriends(user2.getId()))
                .isEmpty();
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return user;
    }
}