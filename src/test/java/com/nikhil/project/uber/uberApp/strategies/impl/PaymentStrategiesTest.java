package com.nikhil.project.uber.uberApp.strategies.impl;

import com.nikhil.project.uber.uberApp.entities.Payment;
import com.nikhil.project.uber.uberApp.entities.Ride;

import com.nikhil.project.uber.uberApp.entities.enums.*;
import com.nikhil.project.uber.uberApp.repositories.PaymentRepository;
import com.nikhil.project.uber.uberApp.services.WalletService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentStrategiesTest {

    @Test
    void walletPaymentStrategy_deductsRiderFareCreditsDriverCutAndConfirmsPayment() {
        WalletService walletService = mock(WalletService.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        WalletPaymentStrategy strategy = new WalletPaymentStrategy(walletService, paymentRepository);
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        ride.setFare(BigDecimal.valueOf(100.00));
        Payment payment = payment(1L, ride, PaymentMethod.WALLET, PaymentStatus.PENDING);

        strategy.processPayment(payment);

        verify(walletService).deductMoneyFromWallet(ride.getRider().getUser(), BigDecimal.valueOf(100.00), null, ride,
                TransactionMethod.RIDE);
        verify(walletService).addMoneyToWallet(ride.getDriver().getUser(), BigDecimal.valueOf(70.00).setScale(2), null, ride,
                TransactionMethod.RIDE);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        verify(paymentRepository).save(payment);
    }

    @Test
    void cashPaymentStrategy_deductsPlatformCommissionFromDriverAndConfirmsPayment() {
        WalletService walletService = mock(WalletService.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CashPaymentStrategy strategy = new CashPaymentStrategy(walletService, paymentRepository);
        Ride ride = ride(1L, rider(1L, user(1L, "rider@test.com", Role.RIDER)),
                driver(1L, user(2L, "driver@test.com", Role.DRIVER), true), RideStatus.ENDED);
        ride.setFare(BigDecimal.valueOf(200.00));
        Payment payment = payment(1L, ride, PaymentMethod.CASH, PaymentStatus.PENDING);

        strategy.processPayment(payment);

        verify(walletService).deductMoneyFromWallet(ride.getDriver().getUser(), BigDecimal.valueOf(60.00).setScale(2), null, ride,
                TransactionMethod.RIDE);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        verify(paymentRepository).save(payment);
    }
}
