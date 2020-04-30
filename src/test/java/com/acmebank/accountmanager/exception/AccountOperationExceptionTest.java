package com.acmebank.accountmanager.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountOperationExceptionTest {

    @Test
    void shouldCreateAccountOperationException() {
        final AccountOperationException ex = new AccountOperationException("Something wrong", "1234");

        assertEquals("1234", ex.getAccountId(), "Account ID should be present");
    }

}