package com.nikhil.project.uber.uberApp.strategies;

import com.nikhil.project.uber.uberApp.entities.RideRequest;

import java.math.BigDecimal;

public interface RideFareCalculationStrategy {

    BigDecimal RIDE_FARE_MULTIPLIER = BigDecimal.TEN;

    BigDecimal calculateFare(RideRequest rideRequest);

}
