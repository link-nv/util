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
import java.security.cert.*;
import java.security.interfaces.DSAKeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;


@SuppressWarnings("UnusedDeclaration")
public class PkiTestUtils {

    public static final    String DEFAULT_ALIAS         = "default";
    protected static final int    RSA_KEYSIZE           = 1024;
    protected static final int    DSA_MODLEN            = 512;
    protected static final int    SERIALNUMBER_NUM_BITS = 128;

    private PkiTestUtils() {

        // empty
    }

    static {
        //noinspection NonFinalStaticVariableUsedInClassInitialization
        if (null == Security.getProvider( BouncyCastleProvider.PROVIDER_NAME ))
            Security.addProvider( new BouncyCastleProvider() );
    }

    public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        return generateKeyPair( "RSA" );
    }

    public static KeyPair generateKeyPair(String algorithm)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance( algorithm );
        SecureRandom random = new SecureRandom();
        if ("RSA".equals( keyPairGenerator.getAlgorithm() ))
            keyPairGenerator.initialize( new RSAKeyGenParameterSpec( RSA_KEYSIZE, RSAKeyGenParameterSpec.F4 ), random );
        else if (keyPairGenerator instanceof DSAKeyPairGenerator) {
            DSAKeyPairGenerator dsaKeyPairGenerator = (DSAKeyPairGenerator) keyPairGenerator;
            dsaKeyPairGenerator.initialize( DSA_MODLEN, false, random );
        }
        return keyPairGenerator.generateKeyPair();
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn, DateTime notBefore, DateTime notAfter,
                                                                @Nullable String signatureAlgorithm, boolean includeAuthorityKeyIdentifier, boolean caCert,
                                                                boolean timeStampingPurpose)
            throws IllegalStateException, IOException, CertificateException, OperatorCreationException {

        return generateCertificate( keyPair.getPublic(), dn, keyPair.getPrivate(), null, notBefore, notAfter, signatureAlgorithm, includeAuthorityKeyIdentifier,
                caCert, timeStampingPurpose, null );
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn)
            throws IllegalStateException, IOException, CertificateException, OperatorCreationException {

        DateTime now = new DateTime();
        DateTime future = now.plusYears( 10 );
        return generateSelfSignedCertificate( keyPair, dn, now, future, null, true, true, false );
    }

    public static X509Certificate generateCertificate(PublicKey subjectPublicKey, String subjectDn, PrivateKey issuerPrivateKey,
                                                      @Nullable X509Certificate issuerCert, DateTime notBefore, DateTime notAfter,
                                                      @Nullable String signatureAlgorithm, boolean includeAuthorityKeyIdentifier, boolean caCert,
                                                      boolean timeStampingPurpose, @Nullable URI ocspUri)
            throws IOException, CertificateException, OperatorCreationException {

        String finalSignatureAlgorithm = signatureAlgorithm;
        if (null == signatureAlgorithm)
            finalSignatureAlgorithm = "SHA512WithRSAEncryption";

        X509Principal issuerDN;
        if (null != issuerCert)
            issuerDN = new X509Principal( issuerCert.getSubjectX500Principal().toString() );
        else
            issuerDN = new X509Principal( subjectDn );

        // new bc 2.0 API
        X509Principal subject = new X509Principal( subjectDn );
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance( subjectPublicKey.getEncoded() );
        BigInteger serialNumber = new BigInteger( SERIALNUMBER_NUM_BITS, new SecureRandom() );

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder( X500Name.getInstance( issuerDN.toASN1Primitive() ), serialNumber,
                notBefore.toDate(), notAfter.toDate(), X500Name.getInstance( subject.toASN1Primitive() ), publicKeyInfo );

        // prepare signer
        ContentSigner signer = new JcaContentSignerBuilder( finalSignatureAlgorithm ).build( issuerPrivateKey );

        // add extensions
        certificateBuilder.addExtension( X509Extension.subjectKeyIdentifier, false, createSubjectKeyId( subjectPublicKey ) );
        PublicKey issuerPublicKey;
        if (null != issuerCert)
            issuerPublicKey = issuerCert.getPublicKey();
        else
            issuerPublicKey = subjectPublicKey;
        if (includeAuthorityKeyIdentifier)
            certificateBuilder.addExtension( X509Extension.authorityKeyIdentifier, false, createAuthorityKeyId( issuerPublicKey ) );

        certificateBuilder.addExtension( X509Extension.basicConstraints, false, new BasicConstraints( caCert ) );

        if (timeStampingPurpose)
            certificateBuilder.addExtension( X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage( KeyPurposeId.id_kp_timeStamping ) );

        if (null != ocspUri) {
            GeneralName ocspName = new GeneralName( GeneralName.uniformResourceIdentifier, new DERIA5String( ocspUri.toString() ) );
            AuthorityInformationAccess authorityInformationAccess = new AuthorityInformationAccess( X509ObjectIdentifiers.ocspAccessMethod, ocspName );
            certificateBuilder.addExtension( X509Extension.authorityInfoAccess, false, authorityInformationAccess );
        }

        // build
        return new JcaX509CertificateConverter().setProvider( "BC" ).getCertificate( certificateBuilder.build( signer ) );
    }

    public static X509Certificate generateTestSelfSignedCert(@Nullable URI ocspUri)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, OperatorCreationException, CertificateException {

        KeyPair keyPair = generateKeyPair();
        DateTime now = new DateTime();
        DateTime notBefore = now.minusDays( 1 );
        DateTime notAfter = now.plusDays( 1 );
        return generateCertificate( keyPair.getPublic(), "CN=Test", keyPair.getPrivate(), null, notBefore, notAfter, null, true, true, false, ocspUri );
    }

    public static KeyStore.PrivateKeyEntry generateKeyEntry(String dn)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, CertificateException, OperatorCreationException {

        KeyPair keyPair = generateKeyPair();
        return new KeyStore.PrivateKeyEntry( keyPair.getPrivate(), new Certificate[] { generateSelfSignedCertificate( keyPair, dn ) } );
    }

    public static X509Certificate loadCertificate(InputStream inputStream)
            throws CertificateException {

        CertificateFactory certificateFactory = CertificateFactory.getInstance( "X.509" );
        return (X509Certificate) certificateFactory.generateCertificate( inputStream );
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    public static X509Certificate loadCertificateFromResource(String resourceName)
            throws CertificateException {

        InputStream inputStream = PkiTestUtils.class.getResourceAsStream( resourceName );
        try {
            return loadCertificate( inputStream );
        }
        finally {
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
     */
    public static KeyStore persistInJKSKeyStore(File pkcs12keyStore, PrivateKey privateKey, Certificate certificate, String keyStorePassword,
                                                String keyEntryPassword)
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
     */
    public static KeyStore persistInPKCS12KeyStore(File pkcs12keyStore, PrivateKey privateKey, Certificate certificate, String keyStorePassword,
                                                   String keyEntryPassword)
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
     */
    public static KeyStore persistInKeyStore(File pkcs12keyStore, String keyStoreType, PrivateKey privateKey, Certificate certificate, String keyStorePassword,
                                             String keyEntryPassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        KeyStore keyStore = KeyStore.getInstance( keyStoreType );
        keyStore.load( null, keyStorePassword.toCharArray() );
        keyStore.setKeyEntry( DEFAULT_ALIAS, privateKey, keyEntryPassword.toCharArray(), new Certificate[] { certificate } );
        FileOutputStream keyStoreOut = new FileOutputStream( pkcs12keyStore );
        try {
            keyStore.store( keyStoreOut, keyStorePassword.toCharArray() );
        }
        finally {
            keyStoreOut.close();
        }

        return keyStore;
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    private static SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey)
            throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );
        return new SubjectKeyIdentifier( info );
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    private static AuthorityKeyIdentifier createAuthorityKeyId(PublicKey publicKey)
            throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );

        return new AuthorityKeyIdentifier( info );
    }
}
