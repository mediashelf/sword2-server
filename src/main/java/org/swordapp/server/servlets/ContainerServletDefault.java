
package org.swordapp.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.swordapp.server.ContainerAPI;
import org.swordapp.server.ContainerManager;
import org.swordapp.server.StatementManager;

public class ContainerServletDefault extends SwordServlet {

    private static final long serialVersionUID = 1L;

    private ContainerManager cm;

    private ContainerAPI api;

    private StatementManager sm;

    @Override
    public void init() throws ServletException {
        super.init();

        // load the container manager implementation
        cm = (ContainerManager) loadImplClass("container-impl", false);

        // load the container manager implementation
        sm = (StatementManager) loadImplClass("statement-impl", false);

        // initialise the underlying servlet processor
        api = new ContainerAPI(cm, sm, config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.get(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.head(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.put(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.post(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.delete(req, resp);
    }
}
