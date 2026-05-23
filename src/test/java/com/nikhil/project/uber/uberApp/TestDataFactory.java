package com.nikhil.project.uber.uberApp;

import com.nikhil.project.uber.uberApp.entities.*;
import com.nikhil.project.uber.uberApp.entities.enums.*;

import java.util.HashSet;
import java.util.Set;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User user(Long id, String email, Role... roles) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRoles(new HashSet<>(Set.of(roles)));
        return user;
    }

    public static Rider rider(Long id, User user) {
        return Rider.builder()
                .id(id)
                .user(user)
                .rating(4.5)
                .build();
    }

    public static Driver driver(Long id, User user, boolean available) {
        return Driver.builder()
                .id(id)
                .user(user)
                .rating(4.7)
                .available(available)
                .vehicleId("KA01AB1234")
                .build();
    }

    public static RideRequest rideRequest(Long id, Rider rider, RideRequestStatus status) {
        RideRequest rideRequest = new RideRequest();
        rideRequest.setId(id);
        rideRequest.setRider(rider);
        rideRequest.setRideRequestStatus(status);
        rideRequest.setPaymentMethod(PaymentMethod.WALLET);
        rideRequest.setFare(100.0);
        return rideRequest;
    }

    public static Ride ride(Long id, Rider rider, Driver driver, RideStatus status) {
        Ride ride = new Ride();
        ride.setId(id);
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setRideStatus(status);
        ride.setPaymentMethod(PaymentMethod.WALLET);
        ride.setFare(100.0);
        ride.setOtp("1234");
        return ride;
    }

    public static Payment payment(Long id, Ride ride, PaymentMethod method, PaymentStatus status) {
        return Payment.builder()
                .id(id)
                .ride(ride)
                .paymentMethod(method)
                .paymentStatus(status)
                .amount(ride.getFare())
                .build();
    }

    public static Wallet wallet(Long id, User user, double balance) {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setUser(user);
        wallet.setBalance(balance);
        return wallet;
    }

    public static Rating rating(Long id, Ride ride, Integer driverRating, Integer riderRating) {
        return Rating.builder()
                .id(id)
                .ride(ride)
                .driver(ride.getDriver())
                .rider(ride.getRider())
                .driverRating(driverRating)
                .riderRating(riderRating)
                .build();
    }
}
