package net.link.util.performance;

import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;


public class ProfilingPolicyContextHandler implements PolicyContextHandler {

    public static final String PROFILING_CONTEXT_KEY = "net.link.safeonline.profiling.context";

    private static ThreadLocal<ProfileData> profileData = new ThreadLocal<ProfileData>();

    public Object getContext(String key, Object data)
            throws PolicyContextException {

        if (false == key.equalsIgnoreCase( PROFILING_CONTEXT_KEY ))
            return null;
        return profileData.get();
    }

    public String[] getKeys()
            throws PolicyContextException {

        String[] keys = { PROFILING_CONTEXT_KEY };
        return keys;
    }

    public boolean supports(String key)
            throws PolicyContextException {

        return key.equalsIgnoreCase( PROFILING_CONTEXT_KEY );
    }

    public static synchronized ProfileData getProfileData() {

        return profileData.get();
    }

    public static synchronized void setProfileData(ProfileData data) {

        profileData.set( data );
    }

    public static synchronized void removeProfileData() {

        profileData.remove();
    }
}
