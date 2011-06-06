/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.common;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.security.*;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.interfaces.DSAKeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Enumeration;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.joda.time.DateTime;


/**
 * Utility class to load keystore key material.
 *
 * @author fcorneli
 */
public abstract class KeyUtils {

    static {
        if (null == Security.getProvider( BouncyCastleProvider.PROVIDER_NAME ))
            Security.addProvider( new BouncyCastleProvider() );
    }

    /**
     * Loads a private key entry from a input stream.
     * <p/>
     * <p> The supported types of keystores depend on the configured java security providers. Example: "pkcs12". </p>
     * <p/>
     * <p> A good alternative java security provider is <a href="http://www.bouncycastle.org/">Bouncy Castle</a>. </p>
     *
     * @param keystoreType the type of the keystore.
     */
    public static PrivateKeyEntry loadFirstPrivateKeyEntry(String keystoreType, InputStream keyStoreInputStream, String keyStorePassword,
                                                           String keyEntryPassword) {

        return loadFirstPrivateKeyEntry( keystoreType, keyStoreInputStream, //
                keyStorePassword == null? null: keyStorePassword.toCharArray(),
                keyEntryPassword == null? null: keyEntryPassword.toCharArray() );
    }

    public static PrivateKeyEntry loadPrivateKeyEntry(String keystoreType, InputStream keyStoreInputStream, String keyStorePassword,
                                                      String keyEntryPassword, String alias) {

        if (alias != null)
            return loadPrivateKeyEntry( keystoreType, keyStoreInputStream, //
                    keyStorePassword == null? null: keyStorePassword.toCharArray(),
                    keyEntryPassword == null? null: keyEntryPassword.toCharArray(), alias );

        return loadFirstPrivateKeyEntry( keystoreType, keyStoreInputStream, //
                keyStorePassword == null? null: keyStorePassword.toCharArray(),
                keyEntryPassword == null? null: keyEntryPassword.toCharArray() );
    }

    public static ImmutableMap<String, X509Certificate> loadCertificates(String keystoreType, InputStream keyStoreInputStream,
                                                                         String keyStorePassword, Predicate<String> ignoreAlias) {

        return loadCertificates( keystoreType, keyStoreInputStream, //
                keyStorePassword == null? null: keyStorePassword.toCharArray(), ignoreAlias );
    }

    public static PrivateKeyEntry loadFirstPrivateKeyEntry(String keystoreType, InputStream keyStoreInputStream, char[] keyStorePassword,
                                                           char[] keyEntryPassword) {

        /* Find the keystore. */
        KeyStore keyStore = loadKeyStore( keystoreType, keyStoreInputStream, keyStorePassword );
        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "could not get aliases", e );
        }
        String alias = null;
        while (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
            try {
                if (keyStore.isKeyEntry( alias ))
                    break;
            }
            catch (KeyStoreException e) {
                throw new RuntimeException( e );
            }

            alias = null;
        }
        if (alias == null)
            throw new RuntimeException( "no private key found in keystore" );

        /* Get the private key entry. */
        try {
            return (PrivateKeyEntry) keyStore.getEntry( alias, new KeyStore.PasswordProtection( keyEntryPassword ) );
        }
        catch (Exception e) {
            throw new RuntimeException( "error retrieving key", e );
        }
    }

    public static PrivateKeyEntry loadPrivateKeyEntry(String keystoreType, InputStream keyStoreInputStream, char[] keyStorePassword,
                                                      char[] keyEntryPassword, String alias) {

        /* Find the keystore. */
        KeyStore keyStore = loadKeyStore( keystoreType, keyStoreInputStream, keyStorePassword );
        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "could not get aliases", e );
        }
        if (!aliases.hasMoreElements())
            throw new RuntimeException( "keystore is empty" );

        try {
            if (!keyStore.isKeyEntry( alias ))
                throw new RuntimeException( "not key entry: " + alias );
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "key store error", e );
        }

        /* Get the private key entry. */
        try {
            return (PrivateKeyEntry) keyStore.getEntry( alias, new KeyStore.PasswordProtection( keyEntryPassword ) );
        }
        catch (Exception e) {
            throw new RuntimeException( "error retrieving key", e );
        }
    }

    public static KeyStore addEntry(KeyStore keyStore, KeyStore.Entry entry, char[] keyEntryPassword, String alias) {

        try {
            keyStore.setEntry( alias, entry, new KeyStore.PasswordProtection( keyEntryPassword ) );

            return keyStore;
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "could not set new entry on keystore for alias: " + alias, e );
        }
    }

    public static ImmutableMap<String, X509Certificate> loadCertificates(String keystoreType, InputStream keyStoreInputStream,
                                                                         char[] keyStorePassword, Predicate<String> ignoreAlias) {

        return getCertificates( loadKeyStore( keystoreType, keyStoreInputStream, keyStorePassword ), ignoreAlias );
    }

    public static ImmutableMap<String, X509Certificate> getCertificates(KeyStore keyStore, Predicate<String> ignoreAlias) {

        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "could not enumerate aliases", e );
        }

        ImmutableMap.Builder<String, X509Certificate> certificates = ImmutableMap.builder();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (ignoreAlias != null && ignoreAlias.apply( alias ))
                continue;

            try {
                if (keyStore.isCertificateEntry( alias ))
                    certificates.put( alias, (X509Certificate) keyStore.getCertificate( alias ) );
            }
            catch (KeyStoreException e) {
                throw new RuntimeException( "error retrieving certificate, alias=" + alias, e );
            }
        }

        return certificates.build();
    }

    public static KeyStore loadKeyStore(String keystoreType, InputStream keyStoreInputStream, char[] keyStorePassword) {

        try {
            KeyStore keyStore = KeyStore.getInstance( keystoreType );
            keyStore.load( keyStoreInputStream, keyStorePassword );

            return keyStore;
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( e );
        }
        catch (CertificateException e) {
            throw new RuntimeException( e );
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( e );
        }
    }

    public static KeyPair generateKeyPair() {

        return generateKeyPair( KeyAlgorithm.RSA );
    }

    public static KeyPair generateKeyPair(KeyAlgorithm keyAlgorithm) {

        try {
            return generateKeyPair( keyAlgorithm.getJCAName() );
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( "RSA not supported", e );
        }
    }

    public static PrivateKeyEntry generatePrivateKeyEntry(String dn) {

        return generatePrivateKeyEntry( KeyAlgorithm.RSA, dn );
    }

    public static PrivateKeyEntry generatePrivateKeyEntry(KeyAlgorithm keyAlgorithm, String dn) {

        KeyPair keyPair = generateKeyPair( keyAlgorithm );
        X509Certificate certificate;
        try {
            certificate = generateSelfSignedCertificate( keyPair, dn );
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException( "Can't generate certificate from generated key", e );
        }

        return new PrivateKeyEntry( keyPair.getPrivate(), new Certificate[] { certificate } );
    }

    public static KeyPair generateKeyPair(String algorithm)
            throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance( algorithm );
        SecureRandom random = new SecureRandom();
        if ("RSA".equals( keyPairGenerator.getAlgorithm() ))
            try {
                keyPairGenerator.initialize( new RSAKeyGenParameterSpec( 1024, RSAKeyGenParameterSpec.F4 ), random );
            }
            catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException( "KeyGenParams incompatible with key generator.", e );
            }
        else if (keyPairGenerator instanceof DSAKeyPairGenerator) {
            DSAKeyPairGenerator dsaKeyPairGenerator = (DSAKeyPairGenerator) keyPairGenerator;
            dsaKeyPairGenerator.initialize( 512, false, random );
        }

        return keyPairGenerator.generateKeyPair();
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn)
            throws InvalidKeyException {

        DateTime now = new DateTime();
        DateTime future = now.plusYears( 10 );
        X509Certificate certificate;
        try {
            certificate = generateSelfSignedCertificate( keyPair, dn, now, future, null, true, false );
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( "Default signature algorithm not supported", e );
        }
        return certificate;
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn, DateTime notBefore, DateTime notAfter,
                                                                String signatureAlgorithm, boolean caCert, boolean timeStampingPurpose)
            throws InvalidKeyException, NoSuchAlgorithmException {

        return generateCertificate( keyPair.getPublic(), dn, keyPair.getPrivate(), null, notBefore, notAfter, signatureAlgorithm, caCert,
                timeStampingPurpose, null );
    }

    public static X509Certificate generateCertificate(PublicKey subjectPublicKey, String subjectDn, PrivateKey issuerPrivateKey,
                                                      X509Certificate issuerCert, DateTime notBefore, DateTime notAfter,
                                                      String inSignatureAlgorithm, boolean caCert, boolean timeStampingPurpose, URI ocspUri)
            throws InvalidKeyException, NoSuchAlgorithmException {

        String signatureAlgorithm = inSignatureAlgorithm;
        if (null == signatureAlgorithm)
            signatureAlgorithm = String.format( "SHA1With%s", issuerPrivateKey.getAlgorithm() );
        X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
        certificateGenerator.reset();
        certificateGenerator.setPublicKey( subjectPublicKey );
        certificateGenerator.setSignatureAlgorithm( signatureAlgorithm );
        certificateGenerator.setNotBefore( notBefore.toDate() );
        certificateGenerator.setNotAfter( notAfter.toDate() );
        X509Principal issuerDN;
        if (null != issuerCert)
            issuerDN = new X509Principal( issuerCert.getSubjectX500Principal().toString() );
        else
            issuerDN = new X509Principal( subjectDn );
        certificateGenerator.setIssuerDN( issuerDN );
        certificateGenerator.setSubjectDN( new X509Principal( subjectDn ) );
        certificateGenerator.setSerialNumber( new BigInteger( 128, new SecureRandom() ) );

        certificateGenerator.addExtension( X509Extensions.SubjectKeyIdentifier, false, createSubjectKeyId( subjectPublicKey ) );
        PublicKey issuerPublicKey;
        if (null != issuerCert)
            issuerPublicKey = issuerCert.getPublicKey();
        else
            issuerPublicKey = subjectPublicKey;
        certificateGenerator.addExtension( X509Extensions.AuthorityKeyIdentifier, false, createAuthorityKeyId( issuerPublicKey ) );

        certificateGenerator.addExtension( X509Extensions.BasicConstraints, false, new BasicConstraints( caCert ) );

        if (timeStampingPurpose)
            certificateGenerator.addExtension( X509Extensions.ExtendedKeyUsage, true,
                    new ExtendedKeyUsage( new DERSequence( KeyPurposeId.id_kp_timeStamping ) ) );

        if (null != ocspUri) {
            GeneralName ocspName = new GeneralName( GeneralName.uniformResourceIdentifier, ocspUri.toString() );
            AuthorityInformationAccess authorityInformationAccess = new AuthorityInformationAccess( X509ObjectIdentifiers.ocspAccessMethod,
                    ocspName );
            certificateGenerator.addExtension( X509Extensions.AuthorityInfoAccess.getId(), false, authorityInformationAccess );
        }

        X509Certificate certificate;
        try {
            certificate = certificateGenerator.generate( issuerPrivateKey );
        }
        catch (CertificateEncodingException e) {
            throw new RuntimeException( e );
        }
        catch (SignatureException e) {
            throw new RuntimeException( e );
        }

        /*
         * Make sure the default certificate provider is active.
         */
        try {
            return (X509Certificate) CertificateFactory.getInstance( "X.509" )
                                                       .generateCertificate( new ByteArrayInputStream( certificate.getEncoded() ) );
        }
        catch (CertificateException e) {
            throw new RuntimeException( "X.509 is not supported.", e );
        }
    }

    private static SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey) {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );

            return new SubjectKeyIdentifier( info );
        }
        catch (IOException e) {
            throw new RuntimeException( "Can't read from a ByteArrayInputStream?", e );
        }
    }

    private static AuthorityKeyIdentifier createAuthorityKeyId(PublicKey publicKey) {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );

            return new AuthorityKeyIdentifier( info );
        }
        catch (IOException e) {
            throw new RuntimeException( "Can't read from a ByteArrayInputStream?", e );
        }
    }

    /**
     * Persist the given private key and corresponding certificate to a PKCS12 keystore file.
     *
     * @param pkcs12keyStore   the file of the PKCS12 keystore to write the key material to.
     * @param privateKey       the private key to persist.
     * @param certificate      the X509 certificate corresponding with the private key.
     * @param keyStorePassword the keystore password.
     * @param keyEntryPassword the keyentry password.
     */
    public static void saveNewKeyStore(File pkcs12keyStore, PrivateKey privateKey, X509Certificate certificate, char[] keyStorePassword,
                                       char[] keyEntryPassword) {

        try {
            KeyStore keyStore = newKeyStore( privateKey, certificate, keyStorePassword, keyEntryPassword );

            FileOutputStream keyStoreOut = new FileOutputStream( pkcs12keyStore );
            keyStore.store( keyStoreOut, keyStorePassword );
            keyStoreOut.close();
        }
        catch (IOException e) {
            throw new RuntimeException( "Key Store can't be created or stored.", e );
        }
        catch (CertificateException e) {
            throw new RuntimeException( "Certificate couldn't be stored.", e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( "KeyStores integrity algorithm not supported.", e );
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "PKCS12 KeyStores not supported or store does not support the key or certificate.", e );
        }
    }

    public static KeyStore newKeyStore(PrivateKey privateKey, X509Certificate certificate, char[] keyStorePassword,
                                       char[] keyEntryPassword) {

        try {
            KeyStore keyStore = newKeyStore();
            keyStore.setKeyEntry( "default", privateKey, keyEntryPassword, new Certificate[] { certificate } );

            return keyStore;
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "PKCS12 KeyStores not supported or store does not support the key or certificate.", e );
        }
    }

    public static KeyStore newKeyStore() {

        try {
            KeyStore keyStore = KeyStore.getInstance( "pkcs12" );
            keyStore.load( null, null );

            return keyStore;
        }
        catch (IOException e) {
            throw new RuntimeException( "Key Store can't be created or stored.", e );
        }
        catch (CertificateException e) {
            throw new RuntimeException( "Certificate couldn't be stored.", e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( "KeyStores integrity algorithm not supported.", e );
        }
        catch (KeyStoreException e) {
            throw new RuntimeException( "PKCS12 KeyStores not supported or store does not support the key or certificate.", e );
        }
    }

    public static void extractCertificate(PrivateKeyEntry privateKeyEntry, File certificateFile) {

        Certificate certificate = privateKeyEntry.getCertificate();
        try {
            FileUtils.writeByteArrayToFile( certificateFile, certificate.getEncoded() );
        }
        catch (CertificateEncodingException e) {
            throw new RuntimeException( "error encoding certificate ", e );
        }
        catch (IOException e) {
            throw new RuntimeException( "error writing out certificate ", e );
        }
    }
}
