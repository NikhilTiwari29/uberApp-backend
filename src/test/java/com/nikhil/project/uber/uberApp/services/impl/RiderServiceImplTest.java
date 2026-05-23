package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.configs.MapperConfig;
import com.nikhil.project.uber.uberApp.dto.DriverDto;
import com.nikhil.project.uber.uberApp.dto.PointDto;
import com.nikhil.project.uber.uberApp.dto.RideDto;
import com.nikhil.project.uber.uberApp.dto.RideRequestDto;
import com.nikhil.project.uber.uberApp.entities.*;
import com.nikhil.project.uber.uberApp.entities.enums.PaymentMethod;
import com.nikhil.project.uber.uberApp.entities.enums.RideRequestStatus;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.repositories.RideRequestRepository;
import com.nikhil.project.uber.uberApp.repositories.RiderRepository;
import com.nikhil.project.uber.uberApp.services.DriverService;
import com.nikhil.project.uber.uberApp.services.RatingService;
import com.nikhil.project.uber.uberApp.services.RideService;
import com.nikhil.project.uber.uberApp.strategies.DriverMatchingStrategy;
import com.nikhil.project.uber.uberApp.strategies.RideFareCalculationStrategy;
import com.nikhil.project.uber.uberApp.strategies.RideStrategyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiderServiceImplTest {

    @Spy
    private ModelMapper modelMapper = new MapperConfig().modelMapper();

    @Mock
    private RideStrategyManager rideStrategyManager;

    @Mock
    private RideRequestRepository rideRequestRepository;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private RideService rideService;

    @Mock
    private DriverService driverService;

    @Mock
    private RatingService ratingService;

    @Mock
    private RideFareCalculationStrategy rideFareCalculationStrategy;

    @Mock
    private DriverMatchingStrategy driverMatchingStrategy;

    @InjectMocks
    private RiderServiceImpl riderService;

    private User riderUser;
    private Rider currentRider;

    @BeforeEach
    void setUp() {
        riderUser = user(1L, "rider@test.com", Role.RIDER);
        currentRider = rider(1L, riderUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(riderUser, null, riderUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestRide_whenValid_savesPendingRequestWithFareAndFindsDrivers() {
        RideRequestDto rideRequestDto = new RideRequestDto();
        rideRequestDto.setPickupLocation(new PointDto(new double[]{77.59, 12.97}));
        rideRequestDto.setDropOffLocation(new PointDto(new double[]{77.60, 12.98}));
        rideRequestDto.setPaymentMethod(PaymentMethod.WALLET);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(currentRider));
        when(rideStrategyManager.rideFareCalculationStrategy()).thenReturn(rideFareCalculationStrategy);
        when(rideFareCalculationStrategy.calculateFare(any(RideRequest.class))).thenReturn(BigDecimal.valueOf(180.00));
        when(rideStrategyManager.driverMatchingStrategy(currentRider.getRating())).thenReturn(driverMatchingStrategy);
        when(driverMatchingStrategy.findMatchingDriver(any(RideRequest.class))).thenReturn(List.of(
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true)
        ));
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(invocation -> {
            RideRequest rideRequest = invocation.getArgument(0);
            rideRequest.setId(20L);
            return rideRequest;
        });

        RideRequestDto savedRequest = riderService.requestRide(rideRequestDto);

        assertThat(savedRequest.getId()).isEqualTo(20L);
        assertThat(savedRequest.getFare()).isEqualByComparingTo(BigDecimal.valueOf(180.00));
        assertThat(savedRequest.getRideRequestStatus()).isEqualTo(RideRequestStatus.PENDING);

        ArgumentCaptor<RideRequest> captor = ArgumentCaptor.forClass(RideRequest.class);
        verify(rideRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getRider()).isEqualTo(currentRider);
        assertThat(captor.getValue().getPaymentMethod()).isEqualTo(PaymentMethod.WALLET);
        verify(driverMatchingStrategy).findMatchingDriver(captor.getValue());
    }

    @Test
    void cancelRide_whenOwnerAndConfirmed_cancelsRideAndFreesDriver() {
        Driver driver = driver(1L, user(2L, "driver@test.com", Role.DRIVER), false);
        Ride ride = ride(1L, currentRider, driver, RideStatus.CONFIRMED);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(currentRider));
        when(rideService.getRideById(ride.getId())).thenReturn(ride);
        when(rideService.updateRideStatus(ride, RideStatus.CANCELLED)).thenAnswer(invocation -> {
            ride.setRideStatus(RideStatus.CANCELLED);
            return ride;
        });

        RideDto rideDto = riderService.cancelRide(ride.getId());

        assertThat(rideDto.getRideStatus()).isEqualTo(RideStatus.CANCELLED);
        verify(driverService).updateDriverAvailability(driver, true);
    }

    @Test
    void cancelRide_whenDifferentRider_throwsRuntimeException() {
        Rider otherRider = rider(2L, user(3L, "other@test.com", Role.RIDER));
        Ride ride = ride(1L, otherRider, driver(1L, user(2L, "driver@test.com", Role.DRIVER), false),
                RideStatus.CONFIRMED);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(currentRider));
        when(rideService.getRideById(ride.getId())).thenReturn(ride);

        assertThrows(RuntimeException.class, () -> riderService.cancelRide(ride.getId()));
    }

    @Test
    void cancelRide_whenNotConfirmed_throwsRuntimeException() {
        Ride ride = ride(1L, currentRider, driver(1L, user(2L, "driver@test.com", Role.DRIVER), false),
                RideStatus.ONGOING);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(currentRider));
        when(rideService.getRideById(ride.getId())).thenReturn(ride);

        assertThrows(RuntimeException.class, () -> riderService.cancelRide(ride.getId()));
    }

    @Test
    void rateDriver_whenRideEndedAndOwned_delegatesToRatingService() {
        Driver driver = driver(1L, user(2L, "driver@test.com", Role.DRIVER), true);
        Ride ride = ride(1L, currentRider, driver, RideStatus.ENDED);
        DriverDto expected = new DriverDto(driver.getId(), null, 5.0, true, driver.getVehicleId());

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(currentRider));
        when(rideService.getRideById(ride.getId())).thenReturn(ride);
        when(ratingService.rateDriver(ride, 5)).thenReturn(expected);

        assertThat(riderService.rateDriver(ride.getId(), 5)).isEqualTo(expected);
    }

    @Test
    void getCurrentRider_whenUserHasNoRider_throwsNotFound() {
        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> riderService.getCurrentRider());
    }

    @Test
    void createNewRider_setsUserAndInitialRating() {
        when(riderRepository.save(any(Rider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rider rider = riderService.createNewRider(riderUser);

        assertThat(rider.getUser()).isEqualTo(riderUser);
        assertThat(rider.getRating()).isZero();
    }

    @Test
    void getMyProfileAndRides_returnCurrentRiderData() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Ride ride = ride(1L, currentRider, driver(1L, user(2L, "driver@test.com", Role.DRIVER), true),
                RideStatus.ENDED);

        when(riderRepository.findByUser(riderUser)).thenReturn(Optional.of(currentRider));
        when(rideService.getAllRidesOfRider(currentRider, pageRequest)).thenReturn(new PageImpl<>(List.of(ride)));

        assertThat(riderService.getMyProfile().getId()).isEqualTo(currentRider.getId());
        assertThat(riderService.getAllMyRides(pageRequest).getContent()).hasSize(1);
    }
}
