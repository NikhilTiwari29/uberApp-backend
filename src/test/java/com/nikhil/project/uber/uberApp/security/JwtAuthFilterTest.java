package com.nikhil.project.uber.uberApp.security;

import com.nikhil.project.uber.uberApp.entities.User;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.services.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static com.nikhil.project.uber.uberApp.TestDataFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private final JWTService jwtService = mock(JWTService.class);
    private final UserService userService = mock(UserService.class);
    private final HandlerExceptionResolver handlerExceptionResolver = mock(HandlerExceptionResolver.class);
    private final TestableJwtAuthFilter filter = new TestableJwtAuthFilter(jwtService, userService);

    JwtAuthFilterTest() {
        ReflectionTestUtils.setField(filter, "handlerExceptionResolver", handlerExceptionResolver);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenAuthorizationHeaderMissing_continuesWithoutAuthentication()
            throws ServletException, IOException {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userService);
    }

    @Test
    void doFilterInternal_whenBearerTokenValid_setsAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        User user = user(1L, "test@example.com", Role.RIDER);
        when(jwtService.getUserIdFromToken("token")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    @Test
    void doFilterInternal_whenTokenParsingFails_delegatesToExceptionResolver()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer bad-token");
        RuntimeException exception = new RuntimeException("bad token");
        when(jwtService.getUserIdFromToken("bad-token")).thenThrow(exception);

        filter.doFilter(request, response, new MockFilterChain());

        verify(handlerExceptionResolver).resolveException(request, response, null, exception);
    }

    private static class TestableJwtAuthFilter extends JwtAuthFilter {
        TestableJwtAuthFilter(JWTService jwtService, UserService userService) {
            super(jwtService, userService);
        }

        void doFilter(MockHttpServletRequest request, MockHttpServletResponse response, MockFilterChain filterChain)
                throws ServletException, IOException {
            doFilterInternal(request, response, filterChain);
        }
    }
}
