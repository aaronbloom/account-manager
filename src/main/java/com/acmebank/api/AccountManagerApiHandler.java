package com.acmebank.api;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccountManagerApiHandler extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(AccountManagerApiHandler.class);

    private final AccountController accountController;

    public AccountManagerApiHandler(final AccountController accountController) {
        this.accountController = accountController;
    }

    @Override
    public void handle(final String target,
                       final Request baseRequest,
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws IOException {
        logger.info("Handling HTTP request on route {}", target);

        if (target.startsWith("/api/account/balance")) { // TODO - replace with match
            final String accountId = request.getParameter("id");
            if (StringUtil.isEmpty(accountId)) {
                logger.warn("HTTP request route on {}, invalid parameter 'id'", target);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                final String content = accountController.getBalance(accountId);

                response.getWriter().println(content);
                response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } else if(target.startsWith("/api/account/transfer")) {
            final String fromAccountId = request.getParameter("from");
            final String targetAccountId = request.getParameter("target");
            final String amount = request.getParameter("amount");

            final String content = accountController.transfer(fromAccountId, targetAccountId, amount);

            response.getWriter().println(content);
            response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            logger.info("Unknown HTTP request route {}", target);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        baseRequest.setHandled(true);
    }

}
