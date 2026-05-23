package com.nikhil.project.uber.uberApp.controllers;


import com.nikhil.project.uber.uberApp.dto.*;
import com.nikhil.project.uber.uberApp.entities.enums.RideRequestStatus;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.services.RiderService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RiderControllerUnitTest {

    private final RiderService riderService = mock(RiderService.class);
    private final RiderController riderController = new RiderController(riderService);

    @Test
    void requestAndCancelRide_delegateToRiderService() {
        RideRequestDto rideRequestDto = new RideRequestDto();
        rideRequestDto.setId(1L);
        rideRequestDto.setRideRequestStatus(RideRequestStatus.PENDING);
        RideDto cancelledRide = new RideDto();
        cancelledRide.setId(1L);
        cancelledRide.setRideStatus(RideStatus.CANCELLED);

        when(riderService.requestRide(rideRequestDto)).thenReturn(rideRequestDto);
        when(riderService.cancelRide(1L)).thenReturn(cancelledRide);

        assertThat(riderController.requestRide(rideRequestDto).getBody()).isEqualTo(rideRequestDto);
        assertThat(riderController.cancelRide(1L).getBody().getRideStatus()).isEqualTo(RideStatus.CANCELLED);
    }

    @Test
    void profileRatingAndRidesEndpoints_delegateToRiderService() {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setRideId(1L);
        ratingDto.setRating(5);
        DriverDto driverDto = new DriverDto(1L, null, 5.0, true, "KA01AB1234");
        RiderDto riderDto = new RiderDto(1L, null, 4.6);
        RideDto rideDto = new RideDto();
        rideDto.setId(1L);
        rideDto.setRideStatus(RideStatus.ENDED);

        when(riderService.rateDriver(1L, 5)).thenReturn(driverDto);
        when(riderService.getMyProfile()).thenReturn(riderDto);
        when(riderService.getAllMyRides(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(rideDto)));

        assertThat(riderController.rateDriver(ratingDto).getBody()).isEqualTo(driverDto);
        assertThat(riderController.getMyProfile().getBody()).isEqualTo(riderDto);
        assertThat(riderController.getAllMyRides(0, 10).getBody().getContent()).containsExactly(rideDto);
    }
}
