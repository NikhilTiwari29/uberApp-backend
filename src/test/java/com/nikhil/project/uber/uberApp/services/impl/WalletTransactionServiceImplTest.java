package com.nikhil.project.uber.uberApp.services.impl;

import com.nikhil.project.uber.uberApp.entities.WalletTransaction;
import com.nikhil.project.uber.uberApp.repositories.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WalletTransactionServiceImplTest {

    @Test
    void createNewWalletTransaction_savesTransaction() {
        WalletTransactionRepository repository = mock(WalletTransactionRepository.class);
        WalletTransactionServiceImpl service = new WalletTransactionServiceImpl(repository, new ModelMapper());
        WalletTransaction transaction = new WalletTransaction();

        service.createNewWalletTransaction(transaction);

        verify(repository).save(transaction);
    }
}
