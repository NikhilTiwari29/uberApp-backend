package com.nikhil.project.uber.uberApp.strategies;

import com.nikhil.project.uber.uberApp.entities.enums.PaymentMethod;
import com.nikhil.project.uber.uberApp.strategies.impl.CashPaymentStrategy;
import com.nikhil.project.uber.uberApp.strategies.impl.WalletPaymentStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PaymentStrategyManagerTest {

    @Test
    void paymentStrategy_returnsStrategyForPaymentMethod() {
        WalletPaymentStrategy walletPaymentStrategy = mock(WalletPaymentStrategy.class);
        CashPaymentStrategy cashPaymentStrategy = mock(CashPaymentStrategy.class);
        PaymentStrategyManager manager = new PaymentStrategyManager(walletPaymentStrategy, cashPaymentStrategy);

        assertThat(manager.paymentStrategy(PaymentMethod.WALLET)).isEqualTo(walletPaymentStrategy);
        assertThat(manager.paymentStrategy(PaymentMethod.CASH)).isEqualTo(cashPaymentStrategy);
    }
}
