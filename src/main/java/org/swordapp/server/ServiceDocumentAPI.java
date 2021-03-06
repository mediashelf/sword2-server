
package org.swordapp.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.model.Element;

public class ServiceDocumentAPI extends SwordAPIEndpoint {

    protected ServiceDocumentManager sdm;

    public ServiceDocumentAPI(ServiceDocumentManager sdm,
            SwordConfiguration config) {
        super(config);
        this.sdm = sdm;
    }

    @Override
    public void get(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // let the superclass prepare the request/response objects
        super.get(req, resp);

        // do the initial authentication
        AuthCredentials auth = null;
        try {
            auth = this.getAuthCredentials(req);
        } catch (SwordAuthException e) {
            if (e.isRetry()) {
                String s = "Basic realm=\"SWORD2\"";
                resp.setHeader("WWW-Authenticate", s);
                resp.setStatus(401);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e
                        .getMessage());
                return;
            }
        }

        try {
            String sdUri = getFullUrl(req);

            // delegate to the implementation to get the service document itself
            ServiceDocument serviceDocument =
                    sdm.getServiceDocument(sdUri, auth, config);
            addGenerator(serviceDocument, config);

            // set the content-type and write the service document to the output stream
            resp.setHeader("Content-Type", "application/atomserv+xml");
            serviceDocument.getAbderaService().writeTo(resp.getWriter());
        } catch (SwordError se) {
            // this is a SWORD level error, to be thrown to the client appropriately
            swordError(req, resp, se);
        } catch (SwordServerException e) {
            // this is something else, to be raised as an internal server error
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(403);
        } finally {
            // flush the output stream
            resp.getWriter().flush();
        }
    }

    protected void addGenerator(ServiceDocument doc, SwordConfiguration config) {
        Element generator = getGenerator(this.config);
        if (generator != null) {
            doc.getWrappedService().addExtension(generator);
        }
    }
}
