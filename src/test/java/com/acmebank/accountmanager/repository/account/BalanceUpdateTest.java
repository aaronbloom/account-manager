package com.acmebank.accountmanager.repository.account;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BalanceUpdateTest {

    @Test
    void shouldCreateBalanceUpdate() {
        final BalanceUpdate balanceUpdate = new BalanceUpdate("123", BigDecimal.ONE);

        assertEquals(BigDecimal.ONE, balanceUpdate.getBalance(), "Should get balance");
        assertEquals("123", balanceUpdate.getAccountId(), "Should get account ID");
    }
}