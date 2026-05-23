package com.nikhil.project.uber.uberApp.advices;

import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.exceptions.RuntimeConflictException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returnsNotFoundResponse() {
        var response = handler.handleResourceNotFound(new ResourceNotFoundException("missing"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getError().getMessage()).isEqualTo("missing");
    }

    @Test
    void handleRuntimeConflict_returnsConflictResponse() {
        var response = handler.handleRuntimeConflictException(new RuntimeConflictException("conflict"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void handleAuthenticationAndJwt_returnUnauthorizedResponse() {
        assertThat(handler.handleAuthenticationException(new BadCredentialsException("bad")).getStatusCode().value())
                .isEqualTo(401);
        assertThat(handler.handleJwtException(new JwtException("bad token")).getStatusCode().value())
                .isEqualTo(401);
    }

    @Test
    void handleAccessDeniedAndGenericException_returnExpectedResponses() {
        assertThat(handler.handleAccessDeniedException(new AccessDeniedException("denied")).getStatusCode().value())
                .isEqualTo(403);
        assertThat(handler.handleInternalServerError(new Exception("boom")).getStatusCode().value())
                .isEqualTo(500);
    }
}
