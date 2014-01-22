package net.link.util.ws.security.username;

import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 22/01/14
 * Time: 14:12
 */
public interface WSSecurityUsernameTokenCallback {

    /**
     * @return the username
     */
    String getUsername();

    /**
     * @return the password
     */
    String getPassword();

    /**
     * Whether a nonce must be added. Note that this is RECOMMENDED.
     * Also if true, a created timestamp will be added also
     *
     * @return must a nonce be added?.
     */
    boolean addNonce();

    /**
     * Use a password digest or plaintext.
     * <p/>
     * digest = Base64 ( SHA-1 ( nonce + created + password ) )
     */
    boolean isDigestPassword();

    /**
     * Call on validation of an incoming SOAP message with a username token header. Return the password for the specified username. The {@link
     * WSSecurityUsernameTokenHandler} will use that password to validate the digest of the password+nonce+created with the one in the SOAP header.
     *
     * @param username the username
     *
     * @return the password for that username or <code>null</code> if username is unknown
     */
    @Nullable
    String handle(String username);
}
