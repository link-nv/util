package net.link.util.common;

public enum KeyAlgorithm {

    DSA( "DSA" ),
    RSA( "RSA" );
    private final String jcaName;

    KeyAlgorithm(final String jcaName) {

        this.jcaName = jcaName;
    }

    /**
     * See Appendix A in the Java Cryptography Architecture API Specification &amp; Reference for information about standard algorithm
     * names.
     *
     * @return The standard name for this key algorithm.
     */
    public String getJCAName() {

        return jcaName;
    }
}
