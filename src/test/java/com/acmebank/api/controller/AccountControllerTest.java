package com.acmebank.api.controller;

import com.acmebank.api.response.AccountBalanceResponse;
import com.acmebank.bank.BankManager;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class AccountControllerTest {

    private BankManager bankManager;
    private Context ctx;
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        bankManager = mock(BankManager.class);
        ctx = mock(Context.class);
        accountController = new AccountController(bankManager);
    }

    @Test
    void shouldGetAccountBalance() throws Exception {
        final Validator<String> validator = mock(Validator.class);
        when(validator.check(any(), anyString())).thenReturn(validator); // TODO invocation check
        when(validator.get()).thenReturn("1234567");
        when(ctx.queryParam(eq("accountId"), any(Class.class))).thenReturn(validator);
        when(bankManager.getAccountBalance("1234567")).thenReturn(new BigDecimal(42));

        accountController.getBalance(ctx);

        verify(bankManager).getAccountBalance(eq("1234567"));
        final String expectedBody = JavalinJson.toJson(new AccountBalanceResponse("1234567", new BigDecimal(42)));
        verify(ctx).json(eq(expectedBody));
    }
}