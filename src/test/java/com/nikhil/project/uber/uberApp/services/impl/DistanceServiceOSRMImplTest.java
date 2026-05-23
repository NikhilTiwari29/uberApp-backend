package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.dto.PointDto;
import com.nikhil.project.uber.uberApp.utils.GeometryUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DistanceServiceOSRMImplTest {

    @Test
    void calculateDistance_whenOsrmReturnsRoute_returnsDistanceInKilometers() {
        RestClient restClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        DistanceServiceOSRMImpl distanceService = new DistanceServiceOSRMImpl(restClient);
        OSRMRoute route = new OSRMRoute();
        route.setDistance(12500.0);
        OSRMResponseDto responseDto = new OSRMResponseDto();
        responseDto.setRoutes(List.of(route));

        when(restClient.get()
                .uri("77.0,12.0;78.0,13.0")
                .retrieve()
                .body(OSRMResponseDto.class)).thenReturn(responseDto);

        double distance = distanceService.calculateDistance(
                GeometryUtil.createPoint(new PointDto(new double[]{77.0, 12.0})),
                GeometryUtil.createPoint(new PointDto(new double[]{78.0, 13.0}))
        );

        assertThat(distance).isEqualTo(12.5);
    }

    @Test
    void calculateDistance_whenClientFails_wrapsException() {
        RestClient restClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        DistanceServiceOSRMImpl distanceService = new DistanceServiceOSRMImpl(restClient);

        when(restClient.get()
                .uri("77.0,12.0;78.0,13.0")
                .retrieve()
                .body(OSRMResponseDto.class)).thenThrow(new RuntimeException("network down"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> distanceService.calculateDistance(
                GeometryUtil.createPoint(new PointDto(new double[]{77.0, 12.0})),
                GeometryUtil.createPoint(new PointDto(new double[]{78.0, 13.0}))
        ));

        assertThat(exception.getMessage()).contains("Error getting data from OSRM");
    }
}
