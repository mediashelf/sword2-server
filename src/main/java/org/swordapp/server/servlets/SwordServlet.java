
package org.swordapp.server.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.swordapp.server.SwordConfiguration;

public class SwordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(SwordServlet.class);

    protected SwordConfiguration config;

    @Override
    public void init() throws ServletException {
        // load the configuration implementation
        config = (SwordConfiguration) loadImplClass("config-impl", false);
    }

    protected Object loadImplClass(String paramName, boolean allowNull)
            throws ServletException {
        String className = getServletContext().getInitParameter(paramName);
        if (className == null) {
            if (allowNull) {
                return null;
            } else {
                log.fatal("'" + paramName +
                        "' init parameter not set in Servlet context");
                throw new ServletException("'" + paramName +
                        "' init parameter not set in Servlet context");
            }
        } else {
            try {
                Object obj = Class.forName(className).newInstance();
                log.info("Using " + className + " as '" + paramName + "'");
                return obj;
            } catch (Exception e) {
                log.fatal("Unable to instantiate class from '" + paramName +
                        "': " + className);
                throw new ServletException(
                        "Unable to instantiate class from '" + paramName +
                                "': " + className, e);
            }
        }
    }
}
