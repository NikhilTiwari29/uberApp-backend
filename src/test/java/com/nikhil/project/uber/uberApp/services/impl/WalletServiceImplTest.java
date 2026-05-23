package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.entities.Ride;
import com.nikhil.project.uber.uberApp.entities.User;
import com.nikhil.project.uber.uberApp.entities.Wallet;
import com.nikhil.project.uber.uberApp.entities.WalletTransaction;
import com.nikhil.project.uber.uberApp.entities.enums.RideStatus;
import com.nikhil.project.uber.uberApp.entities.enums.Role;
import com.nikhil.project.uber.uberApp.entities.enums.TransactionMethod;
import com.nikhil.project.uber.uberApp.entities.enums.TransactionType;
import com.nikhil.project.uber.uberApp.exceptions.ResourceNotFoundException;
import com.nikhil.project.uber.uberApp.exceptions.RuntimeConflictException;
import com.nikhil.project.uber.uberApp.repositories.WalletRepository;
import com.nikhil.project.uber.uberApp.services.WalletTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.nikhil.project.uber.uberApp.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionService walletTransactionService;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void addMoneyToWallet_whenValidAmount_creditsWalletAndCreatesTransaction() {
        User user = user(1L, "rider@test.com", Role.RIDER);
        Wallet wallet = wallet(1L, user, 50.0);
        Ride ride = ride(1L, rider(1L, user), driver(1L, user(2L, "driver@test.com", Role.DRIVER), true),
                RideStatus.ENDED);

        when(walletRepository.findByUserForUpdate(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet savedWallet = walletService.addMoneyToWallet(user, BigDecimal.valueOf(25.00), "txn-1", ride, TransactionMethod.BANKING);

        assertThat(savedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(75.00));
        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionService).createNewWalletTransaction(captor.capture());
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.CREDIT);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(25.00));
        assertThat(captor.getValue().getTransactionId()).isEqualTo("txn-1");
    }

    @Test
    void deductMoneyFromWallet_whenBalanceEnough_debitsWalletAndCreatesTransaction() {
        User user = user(1L, "rider@test.com", Role.RIDER);
        Wallet wallet = wallet(1L, user, 100.0);
        Ride ride = ride(1L, rider(1L, user), driver(1L, user(2L, "driver@test.com", Role.DRIVER), true),
                RideStatus.ENDED);

        when(walletRepository.findByUserForUpdate(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet savedWallet = walletService.deductMoneyFromWallet(user, BigDecimal.valueOf(40.00), "txn-2", ride, TransactionMethod.RIDE);

        assertThat(savedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionService).createNewWalletTransaction(captor.capture());
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.DEBIT);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    void deductMoneyFromWallet_whenBalanceInsufficient_throwsConflict() {
        User user = user(1L, "rider@test.com", Role.RIDER);
        Wallet wallet = wallet(1L, user, 10.0);
        when(walletRepository.findByUserForUpdate(user)).thenReturn(Optional.of(wallet));

        assertThrows(RuntimeConflictException.class,
                () -> walletService.deductMoneyFromWallet(user, BigDecimal.valueOf(20.00), null, null, TransactionMethod.RIDE));

        verify(walletRepository, never()).save(any());
        verifyNoInteractions(walletTransactionService);
    }

    @Test
    void addOrDeductMoney_whenAmountInvalid_throwsConflict() {
        User user = user(1L, "rider@test.com", Role.RIDER);

        assertThrows(RuntimeConflictException.class,
                () -> walletService.addMoneyToWallet(user, BigDecimal.ZERO, null, null, TransactionMethod.RIDE));
        assertThrows(RuntimeConflictException.class,
                () -> walletService.deductMoneyFromWallet(user, BigDecimal.valueOf(-1.00), null, null, TransactionMethod.RIDE));

        verifyNoInteractions(walletRepository, walletTransactionService);
    }

    @Test
    void findWalletById_whenMissing_throwsNotFound() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> walletService.findWalletById(99L));
    }

    @Test
    void createNewWallet_whenUserProvided_savesZeroBalanceWallet() {
        User user = user(1L, "rider@test.com", Role.RIDER);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.createNewWallet(user);

        assertThat(wallet.getUser()).isEqualTo(user);
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
