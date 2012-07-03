
package org.swordapp.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.swordapp.server.CollectionAPI;
import org.swordapp.server.CollectionDepositManager;
import org.swordapp.server.CollectionListManager;

public class CollectionServletDefault extends SwordServlet {

    private static final long serialVersionUID = 1L;

    protected CollectionListManager clm = null;

    protected CollectionDepositManager cdm;

    protected CollectionAPI api;

    @Override
    public void init() throws ServletException {
        super.init();

        // load the collection list manager implementation
        Object possibleClm = loadImplClass("collection-list-impl", true); // allow null
        clm = possibleClm == null ? null : (CollectionListManager) possibleClm;

        // load the deposit manager implementation
        cdm =
                (CollectionDepositManager) loadImplClass(
                        "collection-deposit-impl", false);

        // load the API
        api = new CollectionAPI(clm, cdm, config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.get(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.post(req, resp);
    }
}
