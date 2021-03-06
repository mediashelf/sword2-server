
package org.swordapp.server;

import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatementAPI extends SwordAPIEndpoint {

    private final StatementManager sm;

    public StatementAPI(StatementManager sm, SwordConfiguration config) {
        super(config);
        this.sm = sm;
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
            // there may be some content negotiation going on
            Map<String, String> accept = getAcceptHeaders(req);
            String uri = getFullUrl(req);

            Statement statement = sm.getStatement(uri, accept, auth, config);

            // set the content type
            resp.setHeader("Content-Type", statement.getContentType());

            // set the last modified header
            // like: Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
            SimpleDateFormat sdf =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            Date lastModified =
                    statement.getLastModified() != null ? statement
                            .getLastModified() : new Date();
            resp.setHeader("Last-Modified", sdf.format(lastModified));

            // to set the content-md5 header we need to write the output to
            // a string and checksum it
            StringWriter writer = new StringWriter();
            statement.writeTo(writer);

            // write the content-md5 header
            String md5 =
                    ChecksumUtils.generateMD5(writer.toString().getBytes());
            resp.setHeader("Content-MD5", md5);

            resp.getWriter().append(writer.toString());
            resp.getWriter().flush();

        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordError se) {
            swordError(req, resp, se);
        } catch (NoSuchAlgorithmException e) {
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(403);
        }
    }
}
