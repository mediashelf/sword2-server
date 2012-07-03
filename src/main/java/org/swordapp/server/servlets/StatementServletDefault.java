
package org.swordapp.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.swordapp.server.StatementAPI;
import org.swordapp.server.StatementManager;

public class StatementServletDefault extends SwordServlet {

    private static final long serialVersionUID = 1L;

    private StatementManager sm;

    private StatementAPI statementApi;

    @Override
    public void init() throws ServletException {
        super.init();

        // load the container manager implementation
        sm = (StatementManager) loadImplClass("statement-impl", false);

        // initialise the underlying servlet processor
        statementApi = new StatementAPI(sm, config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        statementApi.get(req, resp);
    }
}
