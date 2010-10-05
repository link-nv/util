/*
 * SafeOnline project.
 *
 * Copyright 2005-2006 Frank Cornelis.
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.pkix;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.joda.time.DateTime;


public class PkiTestUtils {

    public static final String DEFAULT_ALIAS = "default";

    private PkiTestUtils() {

        // empty
    }

    static {
        if (null == Security.getProvider( BouncyCastleProvider.PROVIDER_NAME ))
            Security.addProvider( new BouncyCastleProvider() );
    }

    public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPair keyPair = generateKeyPair( "RSA" );
        return keyPair;
    }

    public static KeyPair generateKeyPair(String algorithm)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance( algorithm );
        SecureRandom random = new SecureRandom();
        if ("RSA".equals( keyPairGenerator.getAlgorithm() ))
            keyPairGenerator.initialize( new RSAKeyGenParameterSpec( 1024, RSAKeyGenParameterSpec.F4 ), random );
        else if (keyPairGenerator instanceof DSAKeyPairGenerator) {
            DSAKeyPairGenerator dsaKeyPairGenerator = (DSAKeyPairGenerator) keyPairGenerator;
            dsaKeyPairGenerator.initialize( 512, false, random );
        }
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn, DateTime notBefore, DateTime notAfter,
                                                                String signatureAlgorithm, boolean includeAuthorityKeyIdentifier,
                                                                boolean caCert, boolean timeStampingPurpose)
            throws InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, SignatureException, IOException,
                   CertificateException {

        X509Certificate certificate = generateCertificate( keyPair.getPublic(), dn, keyPair.getPrivate(), null, notBefore, notAfter,
                signatureAlgorithm, includeAuthorityKeyIdentifier, caCert, timeStampingPurpose, null );
        return certificate;
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn)
            throws InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, SignatureException, IOException,
                   CertificateException {

        DateTime now = new DateTime();
        DateTime future = now.plusYears( 10 );
        X509Certificate certificate = generateSelfSignedCertificate( keyPair, dn, now, future, null, true, true, false );
        return certificate;
    }

    public static X509Certificate generateCertificate(PublicKey subjectPublicKey, String subjectDn, PrivateKey issuerPrivateKey,
                                                      X509Certificate issuerCert, DateTime notBefore, DateTime notAfter,
                                                      String signatureAlgorithm, boolean includeAuthorityKeyIdentifier, boolean caCert,
                                                      boolean timeStampingPurpose, URI ocspUri)
            throws IOException, InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, SignatureException,
                   CertificateException {

        String finalSignatureAlgorithm = signatureAlgorithm;
        if (null == signatureAlgorithm)
            finalSignatureAlgorithm = "SHA512WithRSAEncryption";
        X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
        certificateGenerator.reset();
        certificateGenerator.setPublicKey( subjectPublicKey );
        certificateGenerator.setSignatureAlgorithm( finalSignatureAlgorithm );
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
        if (true == includeAuthorityKeyIdentifier)
            certificateGenerator.addExtension( X509Extensions.AuthorityKeyIdentifier, false, createAuthorityKeyId( issuerPublicKey ) );

        certificateGenerator.addExtension( X509Extensions.BasicConstraints, false, new BasicConstraints( caCert ) );

        if (timeStampingPurpose)
            certificateGenerator.addExtension( X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage( new DERSequence(
                    KeyPurposeId.id_kp_timeStamping ) ) );

        if (null != ocspUri) {
            GeneralName ocspName = new GeneralName( GeneralName.uniformResourceIdentifier, ocspUri.toString() );
            AuthorityInformationAccess authorityInformationAccess = new AuthorityInformationAccess( X509ObjectIdentifiers.ocspAccessMethod,
                    ocspName );
            certificateGenerator.addExtension( X509Extensions.AuthorityInfoAccess.getId(), false, authorityInformationAccess );
        }

        X509Certificate certificate = certificateGenerator.generate( issuerPrivateKey );

        /*
         * Make sure the default certificate provider is active.
         */
        CertificateFactory certificateFactory = CertificateFactory.getInstance( "X.509" );
        certificate = (X509Certificate) certificateFactory.generateCertificate( new ByteArrayInputStream( certificate.getEncoded() ) );

        return certificate;
    }

    public static X509Certificate generateTestSelfSignedCert(URI ocspUri)
            throws Exception {

        KeyPair keyPair = generateKeyPair();
        DateTime now = new DateTime();
        DateTime notBefore = now.minusDays( 1 );
        DateTime notAfter = now.plusDays( 1 );
        X509Certificate certificate = generateCertificate( keyPair.getPublic(), "CN=Test", keyPair.getPrivate(), null, notBefore, notAfter,
                null, true, true, false, ocspUri );
        return certificate;
    }

    public static KeyStore.PrivateKeyEntry generateKeyEntry(String dn)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, SignatureException, IOException, InvalidKeyException,
                   CertificateException {

        KeyPair keyPair = generateKeyPair();
        return new KeyStore.PrivateKeyEntry( keyPair.getPrivate(), new Certificate[] { generateSelfSignedCertificate( keyPair, dn ) } );
    }

    public static X509Certificate loadCertificate(InputStream inputStream)
            throws CertificateException {

        CertificateFactory certificateFactory = CertificateFactory.getInstance( "X.509" );
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate( inputStream );
        return certificate;
    }

    public static X509Certificate loadCertificateFromResource(String resourceName)
            throws CertificateException {

        InputStream inputStream = PkiTestUtils.class.getResourceAsStream( resourceName );
        try {
            X509Certificate certificate = loadCertificate( inputStream );
            return certificate;
        } finally {
            IOUtils.closeQuietly( inputStream );
        }
    }

    /**
     * Persist the given private key and corresponding certificate to a JKS keystore file.
     *
     * @param pkcs12keyStore   the file of the JKS keystore to write the key material to.
     * @param privateKey       the private key to persist.
     * @param certificate      the X509 certificate corresponding with the private key.
     * @param keyStorePassword the keystore password.
     * @param keyEntryPassword the keyentry password.
     *
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    public static KeyStore persistInJKSKeyStore(File pkcs12keyStore, PrivateKey privateKey, Certificate certificate,
                                                String keyStorePassword, String keyEntryPassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        return persistInKeyStore( pkcs12keyStore, "jks", privateKey, certificate, keyStorePassword, keyEntryPassword );
    }

    /**
     * Persist the given private key and corresponding certificate to a PKCS12 keystore file.
     *
     * @param pkcs12keyStore   the file of the PKCS12 keystore to write the key material to.
     * @param privateKey       the private key to persist.
     * @param certificate      the X509 certificate corresponding with the private key.
     * @param keyStorePassword the keystore password.
     * @param keyEntryPassword the keyentry password.
     *
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    public static KeyStore persistInPKCS12KeyStore(File pkcs12keyStore, PrivateKey privateKey, Certificate certificate,
                                                   String keyStorePassword, String keyEntryPassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        return persistInKeyStore( pkcs12keyStore, "pkcs12", privateKey, certificate, keyStorePassword, keyEntryPassword );
    }

    /**
     * Persist the given private key and corresponding certificate to a keystore file.
     *
     * @param pkcs12keyStore   The file of the keystore to write the key material to.
     * @param keyStoreType     The type of the key store format to use.
     * @param privateKey       The private key to persist.
     * @param certificate      The X509 certificate corresponding with the private key.
     * @param keyStorePassword The keystore password.
     * @param keyEntryPassword The keyentry password.
     *
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    public static KeyStore persistInKeyStore(File pkcs12keyStore, String keyStoreType, PrivateKey privateKey, Certificate certificate,
                                             String keyStorePassword, String keyEntryPassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        KeyStore keyStore = KeyStore.getInstance( keyStoreType );
        keyStore.load( null, keyStorePassword.toCharArray() );
        keyStore.setKeyEntry( DEFAULT_ALIAS, privateKey, keyEntryPassword.toCharArray(), new Certificate[] { certificate } );
        FileOutputStream keyStoreOut;
        keyStoreOut = new FileOutputStream( pkcs12keyStore );
        keyStore.store( keyStoreOut, keyStorePassword.toCharArray() );
        keyStoreOut.close();

        return keyStore;
    }

    private static SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey)
            throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );
        return new SubjectKeyIdentifier( info );
    }

    private static AuthorityKeyIdentifier createAuthorityKeyId(PublicKey publicKey)
            throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );

        return new AuthorityKeyIdentifier( info );
    }
}
