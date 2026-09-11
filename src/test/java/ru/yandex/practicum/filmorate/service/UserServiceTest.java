package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@mail.ru");
        return userService.create(user);
    }

    @Test
    void shouldAddFriendMutually() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");

        userService.addFriend(user1.getId(), user2.getId());

        User result1 = userService.getById(user1.getId());
        User result2 = userService.getById(user2.getId());

        assertTrue(result1.getFriends().contains(user2.getId()));
        assertTrue(result2.getFriends().contains(user1.getId()));
    }

    @Test
    void shouldNotAddDuplicateFriend() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user2.getId());

        User result = userService.getById(user1.getId());

        assertEquals(1, result.getFriends().size());
    }

    @Test
    void shouldRemoveFriendMutually() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");

        userService.addFriend(user1.getId(), user2.getId());
        userService.removeFriend(user1.getId(), user2.getId());

        User result1 = userService.getById(user1.getId());
        User result2 = userService.getById(user2.getId());

        assertFalse(result1.getFriends().contains(user2.getId()));
        assertFalse(result2.getFriends().contains(user1.getId()));
    }

    @Test
    void shouldReturnFriends() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");

        userService.addFriend(user1.getId(), user2.getId());

        List<User> friends = userService.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals(user2.getId(), friends.get(0).getId());
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");
        User commonFriend = createUser("common");

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userService.getCommonFriends(
                user1.getId(),
                user2.getId()
        );

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.get(0).getId());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoCommonFriends() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");

        List<User> commonFriends = userService.getCommonFriends(
                user1.getId(),
                user2.getId()
        );

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenAddingHimselfAsFriend() {
        User user = createUser("user1");

        assertThrows(
                ValidationException.class,
                () -> userService.addFriend(user.getId(), user.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenRemovingHimselfAsFriend() {
        User user = createUser("user1");

        assertThrows(
                ValidationException.class,
                () -> userService.removeFriend(user.getId(), user.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenGettingCommonFriendsWithHimself() {
        User user = createUser("user1");

        assertThrows(
                ValidationException.class,
                () -> userService.getCommonFriends(user.getId(), user.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        assertThrows(
                NotFoundException.class,
                () -> userService.getById(999)
        );
    }

    @Test
    void shouldThrowExceptionWhenFriendDoesNotExist() {
        User user = createUser("user1");

        assertThrows(
                NotFoundException.class,
                () -> userService.addFriend(user.getId(), 999)
        );
    }
}