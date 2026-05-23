package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.entities.Payment;
import com.nikhil.project.uber.uberApp.entities.Ride;

import com.nikhil.project.uber.uberApp.entities.enums.PaymentMethod;
import com.nikhil.project.uber.uberApp.entities.enums.PaymentStatus;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.repositories.PaymentRepository;
import com.nikhil.project.uber.uberApp.strategies.PaymentStrategy;
import com.nikhil.project.uber.uberApp.strategies.PaymentStrategyManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStrategyManager paymentStrategyManager;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_whenPaymentExists_delegatesToSelectedStrategy() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        Payment payment = payment(1L, ride, PaymentMethod.WALLET, PaymentStatus.PENDING);

        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(payment));
        when(paymentStrategyManager.paymentStrategy(PaymentMethod.WALLET)).thenReturn(paymentStrategy);

        paymentService.processPayment(ride);

        verify(paymentStrategy).processPayment(payment);
    }

    @Test
    void processPayment_whenPaymentMissing_throwsNotFound() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.processPayment(ride));
    }

    @Test
    void createNewPayment_setsRideMethodAmountAndPendingStatus() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ONGOING);
        ride.setPaymentMethod(PaymentMethod.CASH);
        ride.setFare(250.0);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.createNewPayment(ride);

        assertThat(payment.getRide()).isEqualTo(ride);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(payment.getAmount()).isEqualTo(250.0);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void updatePaymentStatus_setsStatusAndSavesPayment() {
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        Payment payment = payment(1L, ride, PaymentMethod.CASH, PaymentStatus.PENDING);

        paymentService.updatePaymentStatus(payment, PaymentStatus.CONFIRMED);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        verify(paymentRepository).save(payment);
    }
}
