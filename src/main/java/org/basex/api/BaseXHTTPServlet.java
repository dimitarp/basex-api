package org.basex.api;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.core.*;

/**
 * Base class for all servlets.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public abstract class BaseXHTTPServlet extends HttpServlet {

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init();

    final ServletContext ctx = config.getServletContext();

    final String basexhome = ctx.getInitParameter("basexhome");
    if(basexhome != null) {
      System.setProperty("org.basex.path", basexhome);
    }

    final String basexhttp = ctx.getInitParameter("basexhttp");
    if(basexhttp != null) {
      HTTPSession.context().mprop.set(MainProp.HTTPPATH, basexhttp);
    }
  }
}
