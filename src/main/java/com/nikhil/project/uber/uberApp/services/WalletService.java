package com.nikhil.project.uber.uberApp.services;

import com.nikhil.project.uber.uberApp.entities.Ride;
import com.nikhil.project.uber.uberApp.entities.User;
import com.nikhil.project.uber.uberApp.entities.Wallet;
import com.nikhil.project.uber.uberApp.entities.enums.TransactionMethod;

import java.math.BigDecimal;

public interface WalletService {

    Wallet addMoneyToWallet(User user, BigDecimal amount,
                            String transactionId, Ride ride,
                            TransactionMethod transactionMethod);

    Wallet deductMoneyFromWallet(User user, BigDecimal amount,
                                 String transactionId, Ride ride,
                                 TransactionMethod transactionMethod);

    void withdrawAllMyMoneyFromWallet();

    Wallet findWalletById(Long walletId);

    Wallet createNewWallet(User user);

    Wallet findByUser(User user);

}
