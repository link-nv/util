package net.link.util.ws.security.username;

/**
 * Utility WS-Security username token callback.
 * Nonce, created timestamp and password digest type will be used by default
 */
public abstract class AbstractWSSecurityUsernameTokenCallback implements WSSecurityUsernameTokenCallback {

    public boolean addNonce() {

        return true;
    }

    public boolean isDigestPassword() {

        return true;
    }
}
