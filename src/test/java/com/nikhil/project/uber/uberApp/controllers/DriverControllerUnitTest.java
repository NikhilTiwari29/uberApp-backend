package com.nikhil.project.uber.uberApp.controllers;


import com.nikhil.project.uber.uberApp.dto.*;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.services.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DriverControllerUnitTest {

    private final DriverService driverService = mock(DriverService.class);
    private final DriverController driverController = new DriverController(driverService);

    @Test
    void rideLifecycleEndpoints_delegateToDriverService() {
        RideDto confirmedRide = rideDto(1L, RideStatus.CONFIRMED);
        RideDto ongoingRide = rideDto(1L, RideStatus.ONGOING);
        RideDto endedRide = rideDto(1L, RideStatus.ENDED);
        RideStartDto rideStartDto = new RideStartDto();
        rideStartDto.setOtp("1234");

        when(driverService.acceptRide(10L)).thenReturn(confirmedRide);
        when(driverService.startRide(1L, "1234")).thenReturn(ongoingRide);
        when(driverService.endRide(1L)).thenReturn(endedRide);
        when(driverService.cancelRide(1L)).thenReturn(rideDto(1L, RideStatus.CANCELLED));

        assertThat(driverController.acceptRide(10L).getBody().getRideStatus()).isEqualTo(RideStatus.CONFIRMED);
        assertThat(driverController.startRide(1L, rideStartDto).getBody().getRideStatus()).isEqualTo(RideStatus.ONGOING);
        assertThat(driverController.endRide(1L).getBody().getRideStatus()).isEqualTo(RideStatus.ENDED);
        assertThat(driverController.cancelRide(1L).getBody().getRideStatus()).isEqualTo(RideStatus.CANCELLED);
    }

    @Test
    void profileRatingAndRidesEndpoints_delegateToDriverService() {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setRideId(1L);
        ratingDto.setRating(5);
        RiderDto riderDto = new RiderDto(1L, null, 5.0);
        DriverDto driverDto = new DriverDto(1L, null, 4.8, true, "KA01AB1234");
        RideDto rideDto = rideDto(1L, RideStatus.ENDED);

        when(driverService.rateRider(1L, 5)).thenReturn(riderDto);
        when(driverService.getMyProfile()).thenReturn(driverDto);
        when(driverService.getAllMyRides(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(rideDto)));

        assertThat(driverController.rateRider(ratingDto).getBody()).isEqualTo(riderDto);
        assertThat(driverController.getMyProfile().getBody()).isEqualTo(driverDto);
        assertThat(driverController.getAllMyRides(0, 10).getBody().getContent()).containsExactly(rideDto);
    }

    private RideDto rideDto(Long id, RideStatus rideStatus) {
        RideDto rideDto = new RideDto();
        rideDto.setId(id);
        rideDto.setRideStatus(rideStatus);
        return rideDto;
    }
}
