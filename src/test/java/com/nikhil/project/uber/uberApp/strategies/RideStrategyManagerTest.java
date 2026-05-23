package com.nikhil.project.uber.uberApp.strategies;

import com.nikhil.project.uber.uberApp.strategies.impl.DriverMatchingHighestRatedDriverStrategy;
import com.nikhil.project.uber.uberApp.strategies.impl.DriverMatchingNearestDriverStrategy;
import com.nikhil.project.uber.uberApp.strategies.impl.RideFareSurgePricingFareCalculationStrategy;
import com.nikhil.project.uber.uberApp.strategies.impl.RiderFareDefaultFareCalculationStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RideStrategyManagerTest {

    private final DriverMatchingHighestRatedDriverStrategy highestRatedDriverStrategy =
            mock(DriverMatchingHighestRatedDriverStrategy.class);
    private final DriverMatchingNearestDriverStrategy nearestDriverStrategy =
            mock(DriverMatchingNearestDriverStrategy.class);
    private final RideFareSurgePricingFareCalculationStrategy surgePricingFareCalculationStrategy =
            mock(RideFareSurgePricingFareCalculationStrategy.class);
    private final RiderFareDefaultFareCalculationStrategy defaultFareCalculationStrategy =
            mock(RiderFareDefaultFareCalculationStrategy.class);

    private final RideStrategyManager rideStrategyManager = new RideStrategyManager(
            highestRatedDriverStrategy,
            nearestDriverStrategy,
            surgePricingFareCalculationStrategy,
            defaultFareCalculationStrategy
    );

    @Test
    void driverMatchingStrategy_usesHighestRatedForHighRatedRiders() {
        assertThat(rideStrategyManager.driverMatchingStrategy(4.8)).isEqualTo(highestRatedDriverStrategy);
        assertThat(rideStrategyManager.driverMatchingStrategy(4.79)).isEqualTo(nearestDriverStrategy);
    }

    @Test
    void rideFareCalculationStrategy_returnsConfiguredStrategyForCurrentTime() {
        assertThat(rideStrategyManager.rideFareCalculationStrategy())
                .isIn(defaultFareCalculationStrategy, surgePricingFareCalculationStrategy);
    }
}
