package com.acmebank.api;

import com.acmebank.api.controller.AccountController;
import io.javalin.Javalin;
import io.javalin.core.validation.JavalinValidation;

import java.math.BigDecimal;

import static io.javalin.apibuilder.ApiBuilder.*;

public class HttpApi {

    private final AccountController accountController;

    public HttpApi(final AccountController accountController) {
        this.accountController = accountController;
    }

    public void registerDataTypes() {
        JavalinValidation.register(BigDecimal.class, BigDecimal::new);
    }

    public Javalin create() {
        return Javalin.create();
    }

    public void registerRoutes(final Javalin javalin) {
        javalin.routes(() -> {
            path("api/account", () -> {
                path("balance/:accountId", () -> {
                    get(accountController::getBalance);
                });
                path("/transfer/from/:accountId/to/:targetAccountId/amount/:amount", () -> {
                    post(accountController::transfer);
                });
            });
        });
    }

}
