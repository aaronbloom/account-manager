package com.acmebank.api.controller;

import com.acmebank.api.response.AccountBalanceResponse;
import com.acmebank.bank.BankManager;
import com.acmebank.exception.AccountNotFoundException;
import com.acmebank.exception.AccountOperationException;
import com.acmebank.exception.RepositoryException;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import kotlin.jvm.functions.Function1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    private BankManager bankManager;
    private Context ctx;
    private AccountController accountController;
    private Validator<String> stringValidator;
    private Validator<BigDecimal> bigDecimalValidator;

    @BeforeEach
    void setUp() {
        bankManager = mock(BankManager.class);
        ctx = mock(Context.class);
        accountController = new AccountController(bankManager);
        stringValidator = mock(Validator.class);
        bigDecimalValidator = mock(Validator.class);
    }

    @Test
    void shouldGetAccountBalance() throws Exception {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get()).thenReturn("1234567");

        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(bankManager.getAccountBalance("1234567")).thenReturn(new BigDecimal(42));

        accountController.getBalance(ctx);

        verify(bankManager).getAccountBalance(eq("1234567"));

        final ArgumentCaptor<AccountBalanceResponse> argumentCaptor = ArgumentCaptor.forClass(AccountBalanceResponse.class);
        verify(ctx, times(1)).json(argumentCaptor.capture());

        assertEquals("1234567", argumentCaptor.getValue().getAccountId(), "Should be correct account ID");
        assertEquals(new BigDecimal(42), argumentCaptor.getValue().getBalance(), "Should be correct balance for account");
    }

    @Test
    void shouldGetAccountBalanceAndValidateAccountId() {
        final Validator<String> accountIdValidator = mock(Validator.class);
        when(accountIdValidator.check(any(), anyString())).then(invocation -> {
            final Function1<String, Boolean> isAccountIdValid = invocation.getArgument(0);
            assertFalse(isAccountIdValid.invoke(null), "Account ID cannot be null");
            assertFalse(isAccountIdValid.invoke(""), "Account ID cannot be empty");
            assertTrue(isAccountIdValid.invoke("12345"), "Account ID can be a valid string");
            return accountIdValidator;
        });
        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(accountIdValidator);

        accountController.getBalance(ctx);
    }

    @Test
    void shouldGetAccountBalanceWithNotFoundWithWrongAccountId() throws Exception {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get()).thenReturn("1234567");

        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(bankManager.getAccountBalance("1234567")).thenReturn(new BigDecimal(42));

        when(ctx.status(404)).thenReturn(ctx);

        doThrow(new AccountNotFoundException("Message", "1234567"))
                .when(bankManager).getAccountBalance(eq("1234567"));

        accountController.getBalance(ctx);

        verify(ctx, times(1)).status(404);
        verify(ctx, times(1)).result("Account not found '1234567'");
    }

    @Test
    void shouldGetAccountBalanceWhenRepositoryExceptionIsCaught() throws Exception {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get()).thenReturn("1234567");

        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(bankManager.getAccountBalance("1234567")).thenReturn(new BigDecimal(42));

        when(ctx.status(500)).thenReturn(ctx);

        doThrow(new RepositoryException("Message"))
                .when(bankManager).getAccountBalance(eq("1234567"));

        accountController.getBalance(ctx);

        verify(ctx, times(1)).status(500);
        verify(ctx, times(1)).result("Unable to facilitate request");
    }

    @Test
    void shouldTransfer() throws Exception {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get())
                .thenReturn("1234567")
                .thenReturn("9999999");
        when(bigDecimalValidator.check(any(), any())).thenReturn(bigDecimalValidator);
        when(bigDecimalValidator.get()).thenReturn(new BigDecimal(50));

        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("targetAccountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("amount"), any(Class.class))).thenReturn(bigDecimalValidator);

        accountController.transfer(ctx);

        verify(bankManager).transfer(eq("1234567"), eq("9999999"), eq(new BigDecimal(50)));
        verify(ctx, times(1)).status(200);
    }

    @Test
    void shouldTransferAndValidateAccountId() {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(ctx.pathParam(eq("targetAccountId"), any(Class.class))).thenReturn(stringValidator);
        when(bigDecimalValidator.check(any(), any())).thenReturn(bigDecimalValidator);
        when(bigDecimalValidator.get()).thenReturn(new BigDecimal(50));
        when(ctx.pathParam(eq("amount"), any(Class.class))).thenReturn(bigDecimalValidator);

        final Validator<String> accountIdValidator = mock(Validator.class);
        when(accountIdValidator.check(any(), anyString())).then(invocation -> {
            final Function1<String, Boolean> isAccountIdValid = invocation.getArgument(0);
            assertFalse(isAccountIdValid.invoke(null), "Account ID cannot be null");
            assertFalse(isAccountIdValid.invoke(""), "Account ID cannot be empty");
            assertTrue(isAccountIdValid.invoke("12345"), "Account ID can be a valid string");
            return accountIdValidator;
        });
        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(accountIdValidator);

        accountController.transfer(ctx);
    }

    @Test
    void shouldTransferAndValidateTargetAccountId() {
        final String accountId = "xxx";

        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get()).thenReturn(accountId);
        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(bigDecimalValidator.check(any(), any())).thenReturn(bigDecimalValidator);
        when(bigDecimalValidator.get()).thenReturn(new BigDecimal(50));
        when(ctx.pathParam(eq("amount"), any(Class.class))).thenReturn(bigDecimalValidator);

        final Validator<String> targetAccountIdValidator = mock(Validator.class);
        when(targetAccountIdValidator.check(any(), anyString())).then(invocation -> {
            final Function1<String, Boolean> isAccountIdValid = invocation.getArgument(0);
            assertFalse(isAccountIdValid.invoke(null), "Target Account ID cannot be null");
            assertFalse(isAccountIdValid.invoke(""), "Target Account ID cannot be empty");
            assertTrue(isAccountIdValid.invoke("12345"), "Target Account ID can be a valid string");
            return targetAccountIdValidator;
        }).then(invocation -> {
            final Function1<String, Boolean> isAccountIdValid = invocation.getArgument(0);
            assertFalse(isAccountIdValid.invoke(accountId), "Target Account ID cannot be equal to Account ID");
            assertTrue(isAccountIdValid.invoke("12345"), "Target Account ID can be a valid string");
            return targetAccountIdValidator;
        });
        when(ctx.pathParam(eq("targetAccountId"), any(Class.class))).thenReturn(targetAccountIdValidator);

        accountController.transfer(ctx);
    }

    @Test
    void shouldTransferAndValidateAmount() {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("targetAccountId"), any(Class.class))).thenReturn(stringValidator);

        when(bigDecimalValidator.check(any(), any())).then(invocation -> {
            final Function1<BigDecimal, Boolean> amountIsValid = invocation.getArgument(0);
            assertFalse(amountIsValid.invoke(BigDecimal.valueOf(-1)), "Amount cannot be less than zero");
            assertFalse(amountIsValid.invoke(BigDecimal.ZERO), "Amount cannot be zero");
            assertTrue(amountIsValid.invoke(BigDecimal.ONE), "Amount can be above zero");
            return bigDecimalValidator;
        });
        when(bigDecimalValidator.get()).thenReturn(new BigDecimal(50));
        when(ctx.pathParam(eq("amount"), any(Class.class))).thenReturn(bigDecimalValidator);

        accountController.transfer(ctx);
    }

    @Test
    void shouldTransferWithNotFoundWithWrongAccountId() throws Exception {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get())
                .thenReturn("1234567")
                .thenReturn("9999999");
        when(bigDecimalValidator.check(any(), any())).thenReturn(bigDecimalValidator);
        when(bigDecimalValidator.get()).thenReturn(new BigDecimal(50));

        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("targetAccountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("amount"), any(Class.class))).thenReturn(bigDecimalValidator);

        when(ctx.status(404)).thenReturn(ctx);

        doThrow(new AccountNotFoundException("Message", "9999999"))
                .when(bankManager).transfer(eq("1234567"), eq("9999999"), eq(new BigDecimal(50)));

        accountController.transfer(ctx);

        verify(ctx, times(1)).status(404);
        verify(ctx, times(1)).result("Account not found '9999999'");
    }

    @Test
    void shouldTransferWhenRepositoryExceptionIsCaught() throws Exception {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get())
                .thenReturn("1234567")
                .thenReturn("9999999");
        when(bigDecimalValidator.check(any(), any())).thenReturn(bigDecimalValidator);
        when(bigDecimalValidator.get()).thenReturn(new BigDecimal(50));

        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("targetAccountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("amount"), any(Class.class))).thenReturn(bigDecimalValidator);

        when(ctx.status(500)).thenReturn(ctx);

        doThrow(new RepositoryException("Message"))
                .when(bankManager).transfer(eq("1234567"), eq("9999999"), eq(new BigDecimal(50)));

        accountController.transfer(ctx);

        verify(ctx, times(1)).status(500);
        verify(ctx, times(1)).result("Unable to facilitate request");
    }

    @Test
    void shouldTransferWhenAccountOperationExceptionIsCaught() throws Exception {
        when(stringValidator.check(any(), anyString())).thenReturn(stringValidator);
        when(stringValidator.get())
                .thenReturn("1234567")
                .thenReturn("1234567");
        when(bigDecimalValidator.check(any(), any())).thenReturn(bigDecimalValidator);
        when(bigDecimalValidator.get()).thenReturn(new BigDecimal(50));

        when(ctx.pathParam(eq("accountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("targetAccountId"), any(Class.class))).thenReturn(stringValidator);
        when(ctx.pathParam(eq("amount"), any(Class.class))).thenReturn(bigDecimalValidator);

        when(ctx.status(400)).thenReturn(ctx);

        doThrow(new AccountOperationException("Message", "1234567"))
                .when(bankManager).transfer(eq("1234567"), eq("1234567"), eq(new BigDecimal(50)));

        accountController.transfer(ctx);

        verify(ctx, times(1)).status(400);
        verify(ctx, times(1)).result("Invalid request state");
    }
}