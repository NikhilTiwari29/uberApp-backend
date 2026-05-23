package com.nikhil.project.uber.uberApp.security;

import com.nikhil.project.uber.uberApp.entities.User;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.nikhil.project.uber.uberApp.TestDataFactory.user;
import static org.assertj.core.api.Assertions.assertThat;

class JWTServiceTest {

    private final JWTService jwtService = new JWTService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecretKey",
                "test-secret-key-with-more-than-thirty-two-characters");
    }

    @Test
    void generateAccessToken_andGetUserIdFromToken_roundTripsUserId() {
        User user = user(42L, "test@example.com", Role.RIDER);

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void generateRefreshToken_andGetUserIdFromToken_roundTripsUserId() {
        User user = user(43L, "test@example.com", Role.RIDER);

        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(43L);
    }
}
