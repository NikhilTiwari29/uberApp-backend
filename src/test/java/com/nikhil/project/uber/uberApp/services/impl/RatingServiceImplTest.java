package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.dto.DriverDto;
import com.nikhil.project.uber.uberApp.dto.RiderDto;

import com.nikhil.project.uber.uberApp.entities.*;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.exceptions.RuntimeConflictException;
import com.nikhil.project.uber.uberApp.repositories.DriverRepository;
import com.nikhil.project.uber.uberApp.repositories.RatingRepository;
import com.nikhil.project.uber.uberApp.repositories.RiderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RiderRepository riderRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test
    void rateDriver_whenNotRated_updatesRatingAndDriverAverageIgnoringNulls() {
        User riderUser = user(1L, "rider@test.com", Role.RIDER);
        User driverUser = user(2L, "driver@test.com", Role.DRIVER);
        Rider rider = rider(1L, riderUser);
        Driver driver = driver(1L, driverUser, true);
        Ride ride = ride(1L, rider, driver, RideStatus.ENDED);
        Rating currentRating = rating(1L, ride, null, null);

        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(currentRating));
        when(ratingRepository.findByDriver(driver)).thenReturn(List.of(
                currentRating,
                rating(2L, ride, 5, null),
                rating(3L, ride, null, null)
        ));
        when(driverRepository.save(driver)).thenReturn(driver);

        DriverDto driverDto = ratingService.rateDriver(ride, 4);

        assertThat(driverDto.getRating()).isEqualTo(4.5);
        assertThat(driver.getRating()).isEqualTo(4.5);
        verify(ratingRepository).save(currentRating);
    }

    @Test
    void rateDriver_whenAlreadyRated_throwsConflict() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating(1L, ride, 5, null)));

        assertThrows(RuntimeConflictException.class, () -> ratingService.rateDriver(ride, 4));

        verify(driverRepository, never()).save(any());
    }

    @Test
    void rateDriver_whenRatingMissing_throwsNotFound() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ratingService.rateDriver(ride, 4));
    }

    @Test
    void rateRider_whenNotRated_updatesRatingAndRiderAverageIgnoringNulls() {
        User riderUser = user(1L, "rider@test.com", Role.RIDER);
        User driverUser = user(2L, "driver@test.com", Role.DRIVER);
        Rider rider = rider(1L, riderUser);
        Driver driver = driver(1L, driverUser, true);
        Ride ride = ride(1L, rider, driver, RideStatus.ENDED);
        Rating currentRating = rating(1L, ride, null, null);

        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(currentRating));
        when(ratingRepository.findByRider(rider)).thenReturn(List.of(
                currentRating,
                rating(2L, ride, null, 3),
                rating(3L, ride, null, null)
        ));
        when(riderRepository.save(rider)).thenReturn(rider);

        RiderDto riderDto = ratingService.rateRider(ride, 5);

        assertThat(riderDto.getRating()).isEqualTo(4.0);
        assertThat(rider.getRating()).isEqualTo(4.0);
        verify(ratingRepository).save(currentRating);
    }

    @Test
    void rateRider_whenAlreadyRated_throwsConflict() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        when(ratingRepository.findByRide(ride)).thenReturn(Optional.of(rating(1L, ride, null, 5)));

        assertThrows(RuntimeConflictException.class, () -> ratingService.rateRider(ride, 4));
    }

    @Test
    void createNewRating_savesRatingLinkedToRideRiderAndDriver() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ONGOING);

        ratingService.createNewRating(ride);

        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(captor.capture());
        assertThat(captor.getValue().getRide()).isEqualTo(ride);
        assertThat(captor.getValue().getRider()).isEqualTo(ride.getRider());
        assertThat(captor.getValue().getDriver()).isEqualTo(ride.getDriver());
    }
}
