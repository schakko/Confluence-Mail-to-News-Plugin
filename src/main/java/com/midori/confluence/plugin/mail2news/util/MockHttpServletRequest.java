package com.midori.confluence.plugin.mail2news.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.atlassian.confluence.plugins.sharepage.api.SharePageService;

/**
 * This class mocks the {@link HttpServletRequest}. {@link SharePageService}
 * access the current context of the servlet. The mail2news plug-in has no
 * servlet context because it runs as a Quartz job and so the servlet context is not available.
 * 
 * @author ckl
 * 
 */
public class MockHttpServletRequest extends HttpServletRequestWrapper {
	public MockHttpServletRequest() {
		super(new HttpServletRequest() {
			public void setCharacterEncoding(String arg0)
					throws UnsupportedEncodingException {
				// TODO Auto-generated method stub

			}

			public void setAttribute(String name, Object o) {
				// TODO Auto-generated method stub

			}

			public void removeAttribute(String name) {
				// TODO Auto-generated method stub

			}

			public boolean isSecure() {
				// TODO Auto-generated method stub
				return false;
			}

			public int getServerPort() {
				// TODO Auto-generated method stub
				return 0;
			}

			public String getServerName() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getScheme() {
				// TODO Auto-generated method stub
				return null;
			}

			public RequestDispatcher getRequestDispatcher(String path) {
				// TODO Auto-generated method stub
				return null;
			}

			public String getRemoteHost() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getRemoteAddr() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getRealPath(String path) {
				// TODO Auto-generated method stub
				return null;
			}

			public BufferedReader getReader() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			public String getProtocol() {
				// TODO Auto-generated method stub
				return null;
			}

			public String[] getParameterValues(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			public Enumeration getParameterNames() {
				// TODO Auto-generated method stub
				return null;
			}

			public Map getParameterMap() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getParameter(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			public Enumeration getLocales() {
				// TODO Auto-generated method stub
				return null;
			}

			public Locale getLocale() {
				// TODO Auto-generated method stub
				return null;
			}

			public ServletInputStream getInputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			public String getContentType() {
				// TODO Auto-generated method stub
				return null;
			}

			public int getContentLength() {
				// TODO Auto-generated method stub
				return 0;
			}

			public String getCharacterEncoding() {
				// TODO Auto-generated method stub
				return null;
			}

			public Enumeration getAttributeNames() {
				// TODO Auto-generated method stub
				return null;
			}

			public Object getAttribute(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isUserInRole(String role) {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isRequestedSessionIdValid() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isRequestedSessionIdFromUrl() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isRequestedSessionIdFromURL() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isRequestedSessionIdFromCookie() {
				// TODO Auto-generated method stub
				return false;
			}

			public Principal getUserPrincipal() {
				// TODO Auto-generated method stub
				return null;
			}

			public HttpSession getSession(boolean create) {
				// TODO Auto-generated method stub
				return null;
			}

			public HttpSession getSession() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getServletPath() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getRequestedSessionId() {
				// TODO Auto-generated method stub
				return null;
			}

			public StringBuffer getRequestURL() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getRequestURI() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getRemoteUser() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getQueryString() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getPathTranslated() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getPathInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getMethod() {
				// TODO Auto-generated method stub
				return null;
			}

			public int getIntHeader(String name) {
				// TODO Auto-generated method stub
				return 0;
			}

			public Enumeration getHeaders(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			public Enumeration getHeaderNames() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getHeader(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			public long getDateHeader(String name) {
				// TODO Auto-generated method stub
				return 0;
			}

			public Cookie[] getCookies() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getContextPath() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getAuthType() {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}

	public HttpSession getSession() {
		return new HttpSession() {

			public long getCreationTime() {
				// TODO Auto-generated method stub
				return 0;
			}

			public String getId() {
				// TODO Auto-generated method stub
				return null;
			}

			public long getLastAccessedTime() {
				// TODO Auto-generated method stub
				return 0;
			}

			public void setMaxInactiveInterval(int interval) {
				// TODO Auto-generated method stub

			}

			public int getMaxInactiveInterval() {
				// TODO Auto-generated method stub
				return 0;
			}

			public Object getAttribute(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			public Object getValue(String name) {
				// TODO Auto-generated method stub
				return null;
			}

			public Enumeration getAttributeNames() {
				// TODO Auto-generated method stub
				return null;
			}

			public ServletContext getServletContext() {
				return new ServletContext() {
					public void setAttribute(String name, Object object) {
						// TODO Auto-generated method stub

					}

					public void removeAttribute(String name) {
						// TODO Auto-generated method stub

					}

					public void log(String message, Throwable throwable) {
						// TODO Auto-generated method stub

					}

					public void log(Exception exception, String msg) {
						// TODO Auto-generated method stub

					}

					public void log(String msg) {
						// TODO Auto-generated method stub

					}

					public Enumeration getServlets() {
						// TODO Auto-generated method stub
						return null;
					}

					public Enumeration getServletNames() {
						// TODO Auto-generated method stub
						return null;
					}

					public String getServletContextName() {
						// TODO Auto-generated method stub
						return null;
					}

					public Servlet getServlet(String name)
							throws ServletException {
						// TODO Auto-generated method stub
						return null;
					}

					public String getServerInfo() {
						// TODO Auto-generated method stub
						return null;
					}

					public Set getResourcePaths(String arg0) {
						// TODO Auto-generated method stub
						return null;
					}

					public InputStream getResourceAsStream(String path) {
						// TODO Auto-generated method stub
						return null;
					}

					public URL getResource(String path)
							throws MalformedURLException {
						// TODO Auto-generated method stub
						return null;
					}

					public RequestDispatcher getRequestDispatcher(String path) {
						// TODO Auto-generated method stub
						return null;
					}

					public String getRealPath(String path) {
						return "";
					}

					public RequestDispatcher getNamedDispatcher(String name) {
						// TODO Auto-generated method stub
						return null;
					}

					public int getMinorVersion() {
						// TODO Auto-generated method stub
						return 0;
					}

					public String getMimeType(String file) {
						// TODO Auto-generated method stub
						return null;
					}

					public int getMajorVersion() {
						// TODO Auto-generated method stub
						return 0;
					}

					public Enumeration getInitParameterNames() {
						// TODO Auto-generated method stub
						return null;
					}

					public String getInitParameter(String name) {
						// TODO Auto-generated method stub
						return null;
					}

					public ServletContext getContext(String uripath) {
						// TODO Auto-generated method stub
						return null;
					}

					public Enumeration getAttributeNames() {
						// TODO Auto-generated method stub
						return null;
					}

					public Object getAttribute(String name) {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}

			public HttpSessionContext getSessionContext() {
				// TODO Auto-generated method stub
				return null;
			}

			public String[] getValueNames() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setAttribute(String name, Object value) {
				// TODO Auto-generated method stub

			}

			public void putValue(String name, Object value) {
				// TODO Auto-generated method stub

			}

			public void removeAttribute(String name) {
				// TODO Auto-generated method stub

			}

			public void removeValue(String name) {
				// TODO Auto-generated method stub

			}

			public void invalidate() {
				// TODO Auto-generated method stub

			}

			public boolean isNew() {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

	public String getRealPath(String s) {
		return "";
	}
}