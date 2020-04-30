package com.acmebank.accountmanager.bank;

import com.acmebank.accountmanager.repository.account.BalanceUpdate;
import com.acmebank.accountmanager.exception.AccountOperationException;
import com.acmebank.accountmanager.repository.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankManagerTest {

    @Test
    void shouldGetAccountBalance() throws Exception {
        final AccountRepository accountRepository = mock(AccountRepository.class);
        when(accountRepository.getBalance(eq("123"))).thenReturn(new BigDecimal(12));
        final BankManager bankManager = new BankManager(accountRepository);

        final BigDecimal accountBalance = bankManager.getAccountBalance("123");

        assertEquals(new BigDecimal(12), accountBalance, "Should get correct account balance");

        verify(accountRepository, times(1)).getBalance(eq("123"));
    }

    @Test
    void shouldNotTransferBetweenSameAccounts() {
        final AccountRepository accountRepository = mock(AccountRepository.class);
        final BankManager bankManager = new BankManager(accountRepository);

        assertThrows(AccountOperationException.class,
                () -> bankManager.transfer("123", "123", new BigDecimal(42)));

        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void shouldCalculateTransferCorrectly() throws Exception {
        final AccountRepository accountRepository = mock(AccountRepository.class);
        when(accountRepository.getBalance("123")).thenReturn(new BigDecimal(150));
        when(accountRepository.getBalance("999")).thenReturn(new BigDecimal(50));
        final BankManager bankManager = new BankManager(accountRepository);

        bankManager.transfer("123", "999", new BigDecimal(42));

        verify(accountRepository, times(2)).getBalance(anyString());

        final ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(accountRepository, times(1)).updateBalance(argumentCaptor.capture());
        final List<BalanceUpdate> capturedArgument = (List<BalanceUpdate>) argumentCaptor.getValue();

        assertEquals(2, capturedArgument.size(), "Should have two balance updates");
        final BalanceUpdate firstUpdate = capturedArgument.get(0);
        assertEquals("123", firstUpdate.getAccountId(), "Should have correct account id for balance");
        assertEquals(new BigDecimal(108), firstUpdate.getBalance(), "Should have correct new balance");

        final BalanceUpdate secondUpdate = capturedArgument.get(1);
        assertEquals("999", secondUpdate.getAccountId(), "Should have correct account id for balance");
        assertEquals(new BigDecimal(92), secondUpdate.getBalance(), "Should have correct new balance");
    }

    @Test
    void shouldNotTransferMoreThanIsInFromBalance() throws Exception {
        final AccountRepository accountRepository = mock(AccountRepository.class);
        when(accountRepository.getBalance("123")).thenReturn(new BigDecimal(150));
        final BankManager bankManager = new BankManager(accountRepository);

        assertThrows(AccountOperationException.class,
                () -> bankManager.transfer("123", "555", new BigDecimal(200)));
    }

    @Test
    void shouldTransferExactlySameAmountAsFromBalanceSuccessfully() throws Exception {
        final AccountRepository accountRepository = mock(AccountRepository.class);
        when(accountRepository.getBalance("123")).thenReturn(new BigDecimal(150));
        when(accountRepository.getBalance("555")).thenReturn(new BigDecimal(10));
        final BankManager bankManager = new BankManager(accountRepository);

        assertDoesNotThrow(() -> bankManager.transfer("123", "555", new BigDecimal(150)));
    }

}