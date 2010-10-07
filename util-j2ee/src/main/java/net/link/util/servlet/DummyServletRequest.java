package net.link.util.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
* <h2>{@link DummyServletRequest}<br>
* <sub>[in short] (TODO).</sub></h2>
*
* <p>
* <i>10 05, 2010</i>
* </p>
*
* @author lhunath
*/
@SuppressWarnings({ "ALL" })
public class DummyServletRequest implements HttpServletRequest {

    public String getAuthType() {

        throw new UnsupportedOperationException();
    }

    public Cookie[] getCookies() {

        throw new UnsupportedOperationException();
    }

    public long getDateHeader(String name) {

        throw new UnsupportedOperationException();
    }

    public String getHeader(String name) {

        throw new UnsupportedOperationException();
    }

    public Enumeration getHeaders(String name) {

        throw new UnsupportedOperationException();
    }

    public Enumeration getHeaderNames() {

        throw new UnsupportedOperationException();
    }

    public int getIntHeader(String name) {

        throw new UnsupportedOperationException();
    }

    public String getMethod() {

        throw new UnsupportedOperationException();
    }

    public String getPathInfo() {

        throw new UnsupportedOperationException();
    }

    public String getPathTranslated() {

        throw new UnsupportedOperationException();
    }

    public String getContextPath() {

        throw new UnsupportedOperationException();
    }

    public String getQueryString() {

        throw new UnsupportedOperationException();
    }

    public String getRemoteUser() {

        throw new UnsupportedOperationException();
    }

    public boolean isUserInRole(String role) {

        throw new UnsupportedOperationException();
    }

    public Principal getUserPrincipal() {

        throw new UnsupportedOperationException();
    }

    public String getRequestedSessionId() {

        throw new UnsupportedOperationException();
    }

    public String getRequestURI() {

        throw new UnsupportedOperationException();
    }

    public StringBuffer getRequestURL() {

        throw new UnsupportedOperationException();
    }

    public String getServletPath() {

        throw new UnsupportedOperationException();
    }

    public HttpSession getSession(boolean create) {

        throw new UnsupportedOperationException();
    }

    public HttpSession getSession() {

        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdValid() {

        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromCookie() {

        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromURL() {

        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromUrl() {

        throw new UnsupportedOperationException();
    }

    public Object getAttribute(String name) {

        throw new UnsupportedOperationException();
    }

    public Enumeration getAttributeNames() {

        throw new UnsupportedOperationException();
    }

    public String getCharacterEncoding() {

        throw new UnsupportedOperationException();
    }

    public void setCharacterEncoding(String env)
            throws UnsupportedEncodingException {

        throw new UnsupportedOperationException();
    }

    public int getContentLength() {

        throw new UnsupportedOperationException();
    }

    public String getContentType() {

        throw new UnsupportedOperationException();
    }

    public ServletInputStream getInputStream()
            throws IOException {

        throw new UnsupportedOperationException();
    }

    public String getParameter(String name) {

        throw new UnsupportedOperationException();
    }

    public Enumeration getParameterNames() {

        throw new UnsupportedOperationException();
    }

    public String[] getParameterValues(String name) {

        throw new UnsupportedOperationException();
    }

    public Map getParameterMap() {

        throw new UnsupportedOperationException();
    }

    public String getProtocol() {

        throw new UnsupportedOperationException();
    }

    public String getScheme() {

        throw new UnsupportedOperationException();
    }

    public String getServerName() {

        throw new UnsupportedOperationException();
    }

    public int getServerPort() {

        throw new UnsupportedOperationException();
    }

    public BufferedReader getReader()
            throws IOException {

        throw new UnsupportedOperationException();
    }

    public String getRemoteAddr() {

        throw new UnsupportedOperationException();
    }

    public String getRemoteHost() {

        throw new UnsupportedOperationException();
    }

    public void setAttribute(String name, Object o) {

        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String name) {

        throw new UnsupportedOperationException();
    }

    public Locale getLocale() {

        throw new UnsupportedOperationException();
    }

    public Enumeration getLocales() {

        throw new UnsupportedOperationException();
    }

    public boolean isSecure() {

        throw new UnsupportedOperationException();
    }

    public RequestDispatcher getRequestDispatcher(String path) {

        throw new UnsupportedOperationException();
    }

    public String getRealPath(String path) {

        throw new UnsupportedOperationException();
    }

    public int getRemotePort() {

        throw new UnsupportedOperationException();
    }

    public String getLocalName() {

        throw new UnsupportedOperationException();
    }

    public String getLocalAddr() {

        throw new UnsupportedOperationException();
    }

    public int getLocalPort() {

        throw new UnsupportedOperationException();
    }
}
