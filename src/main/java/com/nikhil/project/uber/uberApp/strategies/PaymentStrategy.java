package com.nikhil.project.uber.uberApp.strategies;

import com.nikhil.project.uber.uberApp.entities.Payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    BigDecimal PLATFORM_COMMISSION = BigDecimal.valueOf(0.30);
    void processPayment(Payment payment);

}
