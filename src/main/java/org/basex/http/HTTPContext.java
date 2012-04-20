package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.data.DataText.*;
import static org.basex.http.HTTPText.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class bundles context-based information on a single HTTP operation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HTTPContext {
  /** Singleton database context. */
  private static Context context;

  /** Servlet request. */
  public final HttpServletRequest req;
  /** Servlet response. */
  public final HttpServletResponse res;
  /** Request method. */
  public final HTTPMethod method;

  /** Serialization parameters. */
  public String serialization = "";
  /** Result wrapping. */
  public boolean wrapping;

  /** Segments. */
  private final String[] segments;
  /** Full path. */
  private final String path;
  /** Current user session. */
  private Session session;
  /** User name. */
  private String user;
  /** Password. */
  private String pass;

  /**
   * Constructor.
   * @param rq request
   * @param rs response
   * @throws IOException I/O exception
   */
  public HTTPContext(final HttpServletRequest rq, final HttpServletResponse rs)
      throws IOException {

    req = rq;
    res = rs;
    method = HTTPMethod.get(rq.getMethod());

    // set UTF8 as default encoding (can be overwritten)
    res.setCharacterEncoding(UTF8);
    segments = toSegments(req.getPathInfo());
    path = join(0);

    user = System.getProperty(DBUSER);
    pass = System.getProperty(DBPASS);

    // set session-specific credentials
    final String auth = req.getHeader(AUTHORIZATION);
    if(auth != null) {
      final String[] values = auth.split(" ");
      if(values[0].equals(BASIC)) {
        final String[] cred = Base64.decode(values[1]).split(":", 2);
        if(cred.length != 2) throw new LoginException(NOPASSWD);
        user = cred[0];
        pass = cred[1];
      } else {
        throw new LoginException(WHICHAUTH, values[0]);
      }
    }
  }

  /**
   * Returns all query parameters.
   * @return parameters
   */
  public Map<String, String[]> params() {
    final Map<String, String[]> params = new HashMap<String, String[]>();
    final Map<?, ?> map = req.getParameterMap();
    for(final Entry<?, ?> s : map.entrySet()) {
      final String key = s.getKey().toString();
      final String[] vals = s.getValue() instanceof String[] ?
          (String[]) s.getValue() : new String[] { s.getValue().toString() };
      params.put(key, vals);
    }
    return params;
  }

  /**
   * Returns the content type of a request (without an optional encoding).
   * @return content type
   */
  public String contentType() {
    final String ct = req.getContentType();
    return ct != null ? ct.replaceFirst(";.*", "") : null;
  }

  /**
   * Initializes the output. Sets the expected encoding and content type.
   * @param sprop serialization properties
   */
  public void initResponse(final SerializerProp sprop) {
    // set encoding
    final String encoding = sprop.get(SerializerProp.S_ENCODING);
    res.setCharacterEncoding(encoding);

    // set content type
    String type = sprop.get(SerializerProp.S_MEDIA_TYPE);
    if(type.isEmpty()) {
      // determine content type dependent on output method
      final String mt = sprop.get(SerializerProp.S_METHOD);
      if(mt.equals(M_RAW)) {
        type = APP_OCTET;
      } else if(mt.equals(M_XML)) {
        type = APP_XML;
      } else if(eq(mt, M_JSON, M_JSONML)) {
        type = APP_JSON;
      } else if(eq(mt, M_XHTML, M_HTML5, M_HTML)) {
        type = TEXT_HTML;
      } else {
        type = TEXT_PLAIN;
      }
    }
    res.setContentType(type + MimeTypes.CHARSET + encoding);
  }

  /**
   * Returns the path depth.
   * @return path depth
   */
  public int depth() {
    return segments.length;
  }

  /**
   * Returns the complete path.
   * @return path depth
   */
  public String path() {
    return path;
  }

  /**
   * Returns a single path segment.
   * @param i index
   * @return segment
   */
  public String segment(final int i) {
    return segments[i];
  }

  /**
   * Returns the database path (i.e., all path entries except for the first).
   * @return path depth
   */
  public String dbpath() {
    return join(1);
  }

  /**
   * Returns the addressed database (i.e., the first path entry), or {@code null}
   * if the root directory was specified.
   * @return database
   */
  public String db() {
    return depth() == 0 ? null : segments[0];
  }

  /**
   * Returns an array with all accepted content types.
   * if the root directory was specified.
   * @return database
   */
  public String[] produces() {
    final String[] acc = req.getHeader("Accept").split("\\s*,\\s*");
    for(int a = 0; a < acc.length; a++) {
      if(acc[a].indexOf(';') != -1) acc[a] = acc[a].replaceAll("\\w*;.*", "");
    }
    return acc;
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message info message
   * @throws IOException I/O exception
   */
  public void status(final int code, final String message) throws IOException {
    if(session != null) session.close();
    res.setStatus(code);
    if(code == SC_UNAUTHORIZED) res.setHeader(WWW_AUTHENTICATE, BASIC);
    if(message != null) res.getOutputStream().write(token(message));
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message info message
   * @throws IOException I/O exception
   */
  public void sendError(final int code, final String message) throws IOException {
    if(code == SC_UNAUTHORIZED) res.setHeader(WWW_AUTHENTICATE, BASIC);
    if(message == null) res.sendError(code);
    else res.sendError(code, message);
  }

  /**
   * Updates the credentials.
   * @param u user
   * @param p password
   */
  public void credentials(final String u, final String p) {
    user = u;
    pass = p;
  }

  /**
   * Creates a new session instance. By default, a {@link LocalSession}
   * instance will be generated. A {@link ClientSession} will be created
   * if "client" has been chosen an operation mode.
   * @return database session
   * @throws IOException I/O exception
   */
  public Session session() throws IOException {
    if(user == null || user.isEmpty() || pass == null || pass.isEmpty())
      throw new LoginException(NOPASSWD);

    if(session == null) session = CLIENT.equals(System.getProperty(DBMODE)) ?
        new ClientSession(context(), user, pass) :
        new LocalSession(context(), user, pass);
    return session;
  }

  /**
   * Closes an open database session.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    if(session != null) session.close();
  }

  /**
   * Returns the database context.
   * @return context;
   */
  public Context context() {
    return context;
  }

  // STATIC METHODS =====================================================================

  /**
   * Initializes the HTTP context.
   * @return context;
   */
  public static synchronized Context init() {
    if(context == null) context = new Context();
    return context;
  }

  /**
   * Initializes the servlet context, based on the servlet context.
   * Parses all context parameters and passes them on to the database context.
   * @param sc servlet context
   */
  static synchronized void init(final ServletContext sc) {
    // skip process if context has already been initialized
    if(context != null) return;

    // set servlet path as home directory
    final String path = sc.getRealPath("/");
    System.setProperty(Prop.PATH, path);

    // parse all context parameters
    final HashMap<String, String> map = new HashMap<String, String>();
    // store default web root
    map.put(MainProp.HTTPPATH[0].toString(), path);

    final Enumeration<?> en = sc.getInitParameterNames();
    while(en.hasMoreElements()) {
      final String key = en.nextElement().toString();
      if(!key.startsWith(Prop.DBPREFIX)) continue;

      // only consider parameters that start with "org.basex."
      String val = sc.getInitParameter(key);
      if(eq(key, DBUSER, DBPASS, DBMODE, DBVERBOSE)) {
        // store servlet-specific parameters as system properties
        System.setProperty(key, val);
      } else {
        // prefix relative paths with absolute servlet path
        if(key.endsWith("path") && !new File(val).isAbsolute()) {
          val = path + File.separator + val;
        }
        // store remaining parameters (without project prefix) in map
        map.put(key.substring(Prop.DBPREFIX.length()).toUpperCase(Locale.ENGLISH), val);
      }
    }
    context = new Context(map);
  }

  /**
   * Converts the path to a string array, containing the single segments.
   * @param path path, or {@code null}
   * @return path depth
   */
  public static String[] toSegments(final String path) {
    final StringList sl = new StringList();
    if(path != null) {
      final TokenBuilder tb = new TokenBuilder();
      for(int s = 0; s < path.length(); s++) {
        final char ch = path.charAt(s);
        if(ch == '/') {
          if(tb.isEmpty()) continue;
          sl.add(tb.toString());
          tb.reset();
        } else {
          tb.add(ch);
        }
      }
      if(!tb.isEmpty()) sl.add(tb.toString());
    }
    return sl.toArray();
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Joins the path.
   * @param s segment to start with
   * @return joined path
   */
  private String join(final int s) {
    final TokenBuilder tb = new TokenBuilder();
    for(int p = s; p < segments.length; p++) {
      if(!tb.isEmpty()) tb.add('/');
      tb.add(segments[p]);
    }
    return tb.toString();
  }
}
