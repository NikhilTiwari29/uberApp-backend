package com.nikhil.project.uber.uberApp.controllers;

import com.nikhil.project.uber.uberApp.TestContainerConfiguration;
import com.nikhil.project.uber.uberApp.dto.OnboardDriverDto;
import com.nikhil.project.uber.uberApp.dto.SignupDto;
import com.nikhil.project.uber.uberApp.entities.User;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.repositories.DriverRepository;
import com.nikhil.project.uber.uberApp.repositories.RiderRepository;
import com.nikhil.project.uber.uberApp.repositories.UserRepository;
import com.nikhil.project.uber.uberApp.security.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient(timeout = "100000")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainerConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private JWTService jwtService;

    private User user;

    @BeforeEach
    void setUpEach() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRoles(new HashSet<>(Set.of(Role.RIDER)));
    }

    @Test
    void testSignUp_success() {
        SignupDto signupDto = new SignupDto();
        signupDto.setEmail("test@example.com");
        signupDto.setName("Test name");
        signupDto.setPassword("password");

        webTestClient.post()
                .uri("/auth/signup")
                .bodyValue(signupDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.email").isEqualTo(signupDto.getEmail())
                .jsonPath("$.data.name").isEqualTo(signupDto.getName());
    }

    @Test
    void testOnboardDriver_success() {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setName("Admin User");
        admin.setPassword("password");
        admin.setRoles(new HashSet<>(Set.of(Role.ADMIN)));
        User savedAdmin = userRepository.save(admin);

        user.setEmail("new-driver@example.com");
        User savedUser = userRepository.save(user);

        OnboardDriverDto onboardDriverDto = new OnboardDriverDto();
        onboardDriverDto.setVehicleId("ABC123");

        webTestClient
                .post()
                .uri("/auth/onBoardNewDriver/{userId}", savedUser.getId())
                .header("Authorization", "Bearer " + jwtService.generateAccessToken(savedAdmin))
                .bodyValue(onboardDriverDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.vehicleId").isEqualTo("ABC123");

        User onboardedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(onboardedUser.getRoles()).contains(Role.DRIVER);
        assertThat(driverRepository.findByUser(onboardedUser)).isPresent();
    }
}
