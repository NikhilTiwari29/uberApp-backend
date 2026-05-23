package com.nikhil.project.uber.uberApp.services.impl;


import com.nikhil.project.uber.uberApp.entities.Driver;
import com.nikhil.project.uber.uberApp.entities.Ride;
import com.nikhil.project.uber.uberApp.entities.RideRequest;
import com.nikhil.project.uber.uberApp.entities.Rider;
import com.nikhil.project.uber.uberApp.entities.enums.RideRequestStatus;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.repositories.RideRepository;
import com.nikhil.project.uber.uberApp.services.RideRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RideRequestService rideRequestService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private RideServiceImpl rideService;

    @Test
    void getRideById_whenExists_returnsRide() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.CONFIRMED);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThat(rideService.getRideById(1L)).isEqualTo(ride);
    }

    @Test
    void getRideById_whenMissing_throwsNotFound() {
        when(rideRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rideService.getRideById(99L));
    }

    @Test
    void createNewRide_confirmsRequestAndCreatesRideWithOtp() {
        Rider rider = rider(1L, user(1L, "rider@test.com", Role.RIDER));
        Driver driver = driver(1L, user(2L, "driver@test.com", Role.DRIVER), true);
        RideRequest rideRequest = rideRequest(5L, rider, RideRequestStatus.PENDING);
        AtomicReference<Long> idBeforeSave = new AtomicReference<>();

        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride savedRide = invocation.getArgument(0);
            idBeforeSave.set(savedRide.getId());
            savedRide.setId(10L);
            return savedRide;
        });

        Ride ride = rideService.createNewRide(rideRequest, driver);

        assertThat(rideRequest.getRideRequestStatus()).isEqualTo(RideRequestStatus.CONFIRMED);
        assertThat(ride.getId()).isEqualTo(10L);
        assertThat(ride.getRideStatus()).isEqualTo(RideStatus.CONFIRMED);
        assertThat(ride.getDriver()).isEqualTo(driver);
        assertThat(ride.getOtp()).matches("\\d{4}");
        verify(rideRequestService).update(rideRequest);

        ArgumentCaptor<Ride> captor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(captor.capture());
        assertThat(idBeforeSave.get()).isNull();
    }

    @Test
    void updateRideStatus_setsStatusAndSavesRide() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.CONFIRMED);
        when(rideRepository.save(ride)).thenReturn(ride);

        Ride savedRide = rideService.updateRideStatus(ride, RideStatus.ONGOING);

        assertThat(savedRide.getRideStatus()).isEqualTo(RideStatus.ONGOING);
    }

    @Test
    void getAllRides_delegatesToRepository() {
        Rider rider = rider(1L, user(1L, "rider@test.com", Role.RIDER));
        Driver driver = driver(1L, user(2L, "driver@test.com", Role.DRIVER), true);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Ride ride = ride(1L, rider, driver, RideStatus.ENDED);

        when(rideRepository.findByRider(rider, pageRequest)).thenReturn(new PageImpl<>(List.of(ride)));
        when(rideRepository.findByDriver(driver, pageRequest)).thenReturn(new PageImpl<>(List.of(ride)));

        assertThat(rideService.getAllRidesOfRider(rider, pageRequest).getContent()).containsExactly(ride);
        assertThat(rideService.getAllRidesOfDriver(driver, pageRequest).getContent()).containsExactly(ride);
    }
}
