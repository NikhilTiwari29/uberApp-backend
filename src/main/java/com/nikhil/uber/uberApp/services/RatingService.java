package com.nikhil.uber.uberApp.services;

import com.nikhil.project.uber.uberApp.dto.DriverDto;
import com.nikhil.project.uber.uberApp.dto.RiderDto;
import com.nikhil.project.uber.uberApp.entities.Ride;

public interface RatingService {

    DriverDto rateDriver(Ride ride, Integer rating);
    RiderDto rateRider(Ride ride, Integer rating);

    void createNewRating(Ride ride);
}
