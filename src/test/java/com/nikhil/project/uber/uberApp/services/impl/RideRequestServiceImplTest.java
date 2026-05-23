package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.entities.RideRequest;
import com.nikhil.project.uber.uberApp.entities.enums.RideRequestStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.repositories.RideRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideRequestServiceImplTest {

    @Mock
    private RideRequestRepository rideRequestRepository;

    @InjectMocks
    private RideRequestServiceImpl rideRequestService;

    @Test
    void findRideRequestById_whenExists_returnsRideRequest() {
        RideRequest rideRequest = rideRequest(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                RideRequestStatus.PENDING);
        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(rideRequest));

        RideRequest found = rideRequestService.findRideRequestById(1L);

        assertThat(found).isEqualTo(rideRequest);
    }

    @Test
    void findRideRequestById_whenMissing_throwsNotFound() {
        when(rideRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rideRequestService.findRideRequestById(99L));
    }

    @Test
    void update_whenRideRequestExists_savesRideRequest() {
        RideRequest rideRequest = rideRequest(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                RideRequestStatus.CONFIRMED);
        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(rideRequest));

        rideRequestService.update(rideRequest);

        verify(rideRequestRepository).save(rideRequest);
    }

    @Test
    void update_whenRideRequestMissing_throwsNotFound() {
        RideRequest rideRequest = rideRequest(99L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                RideRequestStatus.CONFIRMED);
        when(rideRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rideRequestService.update(rideRequest));
        verify(rideRequestRepository, never()).save(any());
    }
}
