package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.dto.RideDto;
import com.nikhil.project.uber.uberApp.dto.RiderDto;


import com.nikhil.project.uber.uberApp.entities.*;
import com.nikhil.project.uber.uberApp.entities.enums.RideRequestStatus;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.repositories.DriverRepository;
import com.nikhil.project.uber.uberApp.services.PaymentService;
import com.nikhil.project.uber.uberApp.services.RatingService;
import com.nikhil.project.uber.uberApp.services.RideRequestService;
import com.nikhil.project.uber.uberApp.services.RideService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private RideRequestService rideRequestService;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RideService rideService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    private PaymentService paymentService;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private DriverServiceImpl driverService;

    private User driverUser;
    private Driver currentDriver;
    private Rider rider;

    @BeforeEach
    void setUp() {
        driverUser = user(2L, "driver@test.com", Role.DRIVER);
        currentDriver = driver(1L, driverUser, true);
        rider = rider(1L, user(1L, "rider@test.com", Role.RIDER));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(driverUser, null, driverUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void acceptRide_whenPendingAndDriverAvailable_createsRideAndMarksDriverUnavailable() {
        RideRequest rideRequest = rideRequest(1L, rider, RideRequestStatus.PENDING);
        Ride ride = ride(10L, rider, currentDriver, RideStatus.CONFIRMED);

        when(rideRequestService.findRideRequestById(rideRequest.getId())).thenReturn(rideRequest);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));
        when(driverRepository.save(currentDriver)).thenReturn(currentDriver);
        when(rideService.createNewRide(rideRequest, currentDriver)).thenReturn(ride);

        RideDto rideDto = driverService.acceptRide(rideRequest.getId());

        assertThat(rideDto.getId()).isEqualTo(10L);
        assertThat(currentDriver.getAvailable()).isFalse();
        verify(rideService).createNewRide(rideRequest, currentDriver);
    }

    @Test
    void acceptRide_whenRequestNotPending_throwsRuntimeException() {
        RideRequest rideRequest = rideRequest(1L, rider, RideRequestStatus.CONFIRMED);
        when(rideRequestService.findRideRequestById(rideRequest.getId())).thenReturn(rideRequest);

        assertThrows(RuntimeException.class, () -> driverService.acceptRide(rideRequest.getId()));
    }

    @Test
    void acceptRide_whenDriverUnavailable_throwsRuntimeException() {
        RideRequest rideRequest = rideRequest(1L, rider, RideRequestStatus.PENDING);
        currentDriver.setAvailable(false);

        when(rideRequestService.findRideRequestById(rideRequest.getId())).thenReturn(rideRequest);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));

        assertThrows(RuntimeException.class, () -> driverService.acceptRide(rideRequest.getId()));
    }

    @Test
    void startRide_whenConfirmedOwnerAndOtpMatches_startsRideAndCreatesPaymentAndRating() {
        Ride ride = ride(10L, rider, currentDriver, RideStatus.CONFIRMED);
        when(rideService.getRideById(ride.getId())).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));
        when(rideService.updateRideStatus(ride, RideStatus.ONGOING)).thenAnswer(invocation -> {
            ride.setRideStatus(RideStatus.ONGOING);
            return ride;
        });

        RideDto rideDto = driverService.startRide(ride.getId(), "1234");

        assertThat(rideDto.getRideStatus()).isEqualTo(RideStatus.ONGOING);
        assertThat(ride.getStartedAt()).isNotNull();
        verify(paymentService).createNewPayment(ride);
        verify(ratingService).createNewRating(ride);
    }

    @Test
    void startRide_whenOtpDoesNotMatch_throwsRuntimeException() {
        Ride ride = ride(10L, rider, currentDriver, RideStatus.CONFIRMED);
        when(rideService.getRideById(ride.getId())).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));

        assertThrows(RuntimeException.class, () -> driverService.startRide(ride.getId(), "9999"));
    }

    @Test
    void endRide_whenOngoingOwner_endsRideFreesDriverAndProcessesPayment() {
        currentDriver.setAvailable(false);
        Ride ride = ride(10L, rider, currentDriver, RideStatus.ONGOING);
        when(rideService.getRideById(ride.getId())).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));
        when(rideService.updateRideStatus(ride, RideStatus.ENDED)).thenAnswer(invocation -> {
            ride.setRideStatus(RideStatus.ENDED);
            return ride;
        });
        when(driverRepository.save(currentDriver)).thenReturn(currentDriver);

        RideDto rideDto = driverService.endRide(ride.getId());

        assertThat(rideDto.getRideStatus()).isEqualTo(RideStatus.ENDED);
        assertThat(ride.getEndedAt()).isNotNull();
        assertThat(currentDriver.getAvailable()).isTrue();
        verify(paymentService).processPayment(ride);
    }

    @Test
    void cancelRide_whenConfirmedOwner_cancelsRideAndFreesDriver() {
        currentDriver.setAvailable(false);
        Ride ride = ride(10L, rider, currentDriver, RideStatus.CONFIRMED);
        when(rideService.getRideById(ride.getId())).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));
        when(rideService.updateRideStatus(ride, RideStatus.CANCELLED)).thenAnswer(invocation -> {
            ride.setRideStatus(RideStatus.CANCELLED);
            return ride;
        });
        when(driverRepository.save(currentDriver)).thenReturn(currentDriver);

        RideDto rideDto = driverService.cancelRide(ride.getId());

        assertThat(rideDto.getRideStatus()).isEqualTo(RideStatus.CANCELLED);
        assertThat(currentDriver.getAvailable()).isTrue();
    }

    @Test
    void rateRider_whenRideEndedAndOwned_delegatesToRatingService() {
        Ride ride = ride(10L, rider, currentDriver, RideStatus.ENDED);
        RiderDto expected = new RiderDto(rider.getId(), null, 4.5);

        when(rideService.getRideById(ride.getId())).thenReturn(ride);
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));
        when(ratingService.rateRider(ride, 5)).thenReturn(expected);

        assertThat(driverService.rateRider(ride.getId(), 5)).isEqualTo(expected);
    }

    @Test
    void getCurrentDriver_whenUserHasNoDriver_throwsNotFound() {
        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.getCurrentDriver());
    }

    @Test
    void getMyProfileAndRides_returnCurrentDriverData() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Ride ride = ride(10L, rider, currentDriver, RideStatus.ENDED);

        when(driverRepository.findByUser(driverUser)).thenReturn(Optional.of(currentDriver));
        when(rideService.getAllRidesOfDriver(currentDriver, pageRequest)).thenReturn(new PageImpl<>(List.of(ride)));

        assertThat(driverService.getMyProfile().getId()).isEqualTo(currentDriver.getId());
        assertThat(driverService.getAllMyRides(pageRequest).getContent()).hasSize(1);
    }

    @Test
    void updateDriverAvailabilityAndCreateNewDriver_saveDriver() {
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(driverService.updateDriverAvailability(currentDriver, false).getAvailable()).isFalse();
        assertThat(driverService.createNewDriver(currentDriver)).isEqualTo(currentDriver);
    }
}
