
package org.swordapp.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.swordapp.server.ServiceDocumentAPI;
import org.swordapp.server.ServiceDocumentManager;

public class ServiceDocumentServletDefault extends SwordServlet {

    private static final long serialVersionUID = 1L;

    protected ServiceDocumentManager sdm;

    protected ServiceDocumentAPI api;

    @Override
    public void init() throws ServletException {
        super.init();

        // load the service document implementation
        sdm =
                (ServiceDocumentManager) loadImplClass("service-document-impl",
                        false);

        // load the api
        api = new ServiceDocumentAPI(sdm, config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.get(req, resp);
    }
}
