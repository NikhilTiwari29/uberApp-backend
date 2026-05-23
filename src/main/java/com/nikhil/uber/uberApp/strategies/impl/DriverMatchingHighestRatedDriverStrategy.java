package com.nikhil.uber.uberApp.strategies.impl;

import com.nikhil.project.uber.uberApp.entities.Driver;
import com.nikhil.project.uber.uberApp.entities.RideRequest;
import com.nikhil.project.uber.uberApp.repositories.DriverRepository;
import com.nikhil.project.uber.uberApp.strategies.DriverMatchingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional()
public class DriverMatchingHighestRatedDriverStrategy implements DriverMatchingStrategy {

    private final DriverRepository driverRepository;

    @Override
    public List<Driver> findMatchingDriver(RideRequest rideRequest) {
        return driverRepository.findTenNearbyTopRatedDrivers(rideRequest.getPickupLocation());
    }
}
