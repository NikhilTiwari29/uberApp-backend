package com.nikhil.project.uber.uberApp.strategies.impl;

import com.nikhil.project.uber.uberApp.entities.Driver;
import com.nikhil.project.uber.uberApp.entities.RideRequest;
import com.nikhil.project.uber.uberApp.entities.enums.RideRequestStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.repositories.DriverRepository;
import com.nikhil.project.uber.uberApp.services.DistanceService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FareAndDriverMatchingStrategyTest {

    @Test
    void defaultFareStrategy_returnsDistanceTimesBaseMultiplier() {
        DistanceService distanceService = mock(DistanceService.class);
        RiderFareDefaultFareCalculationStrategy strategy = new RiderFareDefaultFareCalculationStrategy(distanceService);
        RideRequest rideRequest = rideRequest(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                RideRequestStatus.PENDING);
        when(distanceService.calculateDistance(null, null)).thenReturn(12.5);

        assertThat(strategy.calculateFare(rideRequest)).isEqualByComparingTo(BigDecimal.valueOf(125.00));
    }

    @Test
    void surgeFareStrategy_appliesSurgeFactor() {
        DistanceService distanceService = mock(DistanceService.class);
        RideFareSurgePricingFareCalculationStrategy strategy = new RideFareSurgePricingFareCalculationStrategy(distanceService);
        RideRequest rideRequest = rideRequest(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                RideRequestStatus.PENDING);
        when(distanceService.calculateDistance(null, null)).thenReturn(10.0);

        assertThat(strategy.calculateFare(rideRequest)).isEqualByComparingTo(BigDecimal.valueOf(200.00));
    }

    @Test
    void nearestDriverStrategy_delegatesToNearestDriverRepositoryQuery() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        DriverMatchingNearestDriverStrategy strategy = new DriverMatchingNearestDriverStrategy(driverRepository);
        RideRequest rideRequest = rideRequest(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                RideRequestStatus.PENDING);
        Driver driver = driver(1L, user(2L, "driver@test.com", Role.DRIVER), true);
        when(driverRepository.findTenNearestDrivers(null)).thenReturn(List.of(driver));

        assertThat(strategy.findMatchingDriver(rideRequest)).containsExactly(driver);
    }

    @Test
    void highestRatedDriverStrategy_delegatesToTopRatedRepositoryQuery() {
        DriverRepository driverRepository = mock(DriverRepository.class);
        DriverMatchingHighestRatedDriverStrategy strategy = new DriverMatchingHighestRatedDriverStrategy(driverRepository);
        RideRequest rideRequest = rideRequest(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                RideRequestStatus.PENDING);
        Driver driver = driver(1L, user(2L, "driver@test.com", Role.DRIVER), true);
        when(driverRepository.findTenNearbyTopRatedDrivers(null)).thenReturn(List.of(driver));

        assertThat(strategy.findMatchingDriver(rideRequest)).containsExactly(driver);
    }
}
