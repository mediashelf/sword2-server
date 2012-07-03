
package org.swordapp.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.swordapp.server.MediaResourceAPI;
import org.swordapp.server.MediaResourceManager;

public class MediaResourceServletDefault extends SwordServlet {

    private static final long serialVersionUID = 1L;

    protected MediaResourceManager mrm;

    protected MediaResourceAPI api;

    @Override
    public void init() throws ServletException {
        super.init();

        // load the Media Resource Manager
        mrm =
                (MediaResourceManager) loadImplClass("media-resource-impl",
                        false);

        // load the api
        api = new MediaResourceAPI(mrm, config);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.post(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.put(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        api.delete(req, resp);
    }
}
