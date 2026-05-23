package com.nikhil.project.uber.uberApp.controllers;


import com.nikhil.project.uber.uberApp.dto.*;
import com.nikhil.project.uber.uberApp.services.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthControllerUnitTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController authController = new AuthController(authService);

    @Test
    void signUp_returnsCreatedUser() {
        SignupDto signupDto = new SignupDto("Test User", "test@example.com", "password");
        UserDto userDto = new UserDto(1L, "Test User", "test@example.com", Set.of());
        when(authService.signup(signupDto)).thenReturn(userDto);

        var response = authController.signUp(signupDto);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(userDto);
    }

    @Test
    void onboardNewDriver_returnsCreatedDriver() {
        OnboardDriverDto onboardDriverDto = new OnboardDriverDto();
        onboardDriverDto.setVehicleId("KA01AB1234");
        DriverDto driverDto = new DriverDto(1L, null, 0.0, true, "KA01AB1234");
        when(authService.onboardNewDriver(1L, "KA01AB1234")).thenReturn(driverDto);

        var response = authController.onBoardNewDriver(1L, onboardDriverDto);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(driverDto);
    }

    @Test
    void login_returnsAccessTokenAndRefreshCookie() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authService.login("test@example.com", "password"))
                .thenReturn(new String[]{"access-token", "refresh-token"});

        var responseEntity = authController.login(loginRequestDto, request, response);

        assertThat(responseEntity.getBody().getAccessToken()).isEqualTo("access-token");
        Cookie refreshCookie = response.getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo("refresh-token");
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/auth/refresh");
    }

    @Test
    void refresh_whenRefreshCookieExists_returnsNewAccessToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "refresh-token"));
        when(authService.refreshToken("refresh-token")).thenReturn("new-access-token");

        var response = authController.refresh(request);

        assertThat(response.getBody().getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void refresh_whenCookieMissing_throwsAuthenticationException() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThrows(AuthenticationServiceException.class, () -> authController.refresh(request));
    }
}
