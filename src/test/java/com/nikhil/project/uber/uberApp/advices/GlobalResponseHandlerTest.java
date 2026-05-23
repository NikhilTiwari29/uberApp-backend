package com.nikhil.project.uber.uberApp.advices;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalResponseHandlerTest {

    private final GlobalResponseHandler handler = new GlobalResponseHandler();

    @Test
    void supports_alwaysReturnsTrue() {
        assertThat(handler.supports(null, null)).isTrue();
    }

    @Test
    void beforeBodyWrite_wrapsNormalBodyInApiResponse() {
        ServerHttpRequest request = requestFor("/riders/getMyProfile");

        Object response = handler.beforeBodyWrite(Map.of("name", "test"), null, MediaType.APPLICATION_JSON,
                null, request, mock(ServerHttpResponse.class));

        assertThat(response).isInstanceOf(ApiResponse.class);
        assertThat(((ApiResponse<?>) response).getData()).isEqualTo(Map.of("name", "test"));
    }

    @Test
    void beforeBodyWrite_keepsExistingApiResponseAndAllowedRoutesUnwrapped() {
        ApiResponse<String> apiResponse = new ApiResponse<>("already wrapped");

        assertThat(handler.beforeBodyWrite(apiResponse, null, MediaType.APPLICATION_JSON, null,
                requestFor("/riders/getMyProfile"), mock(ServerHttpResponse.class))).isEqualTo(apiResponse);
        assertThat(handler.beforeBodyWrite("openapi", null, MediaType.APPLICATION_JSON, null,
                requestFor("/v3/api-docs"), mock(ServerHttpResponse.class))).isEqualTo("openapi");
    }

    private ServerHttpRequest requestFor(String path) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("http://localhost" + path));
        return request;
    }
}
