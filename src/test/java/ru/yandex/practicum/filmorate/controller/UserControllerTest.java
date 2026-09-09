package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testUser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        return user;
    }

    @Test
    void shouldCreateUserWithValidData() {
        User user = createValidUser();

        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        User user = createValidUser();
        user.setEmail("");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = createValidUser();
        user.setEmail(null);

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAtSymbol() {
        User user = createValidUser();
        user.setEmail("mail.ru");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User user = createValidUser();
        user.setLogin("test user");

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsEmpty() {
        User user = createValidUser();

        user.setLogin("myLogin");
        user.setName("");

        User createdUser = userController.create(user);

        assertEquals("myLogin", createdUser.getName());
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsNull() {
        User user = createValidUser();

        user.setLogin("myLogin");
        user.setName(null);

        User createdUser = userController.create(user);

        assertEquals("myLogin", createdUser.getName());
    }

    @Test
    void shouldAllowBirthdayToday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(
                ValidationException.class,
                () -> userController.create(user)
        );
    }
}