package com.nikhil.project.uber.uberApp.services;

import com.nikhil.project.uber.uberApp.entities.User;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.repositories.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.nikhil.project.uber.uberApp.TestDataFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = new UserService(userRepository);

    @Test
    void loadUserByUsername_whenFound_returnsUser() {
        User user = user(1L, "test@example.com", Role.RIDER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThat(userService.loadUserByUsername("test@example.com")).isEqualTo(user);
    }

    @Test
    void loadUserByUsername_whenMissing_returnsNull() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThat(userService.loadUserByUsername("missing@example.com")).isNull();
    }

    @Test
    void getUserById_whenMissing_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }
}
