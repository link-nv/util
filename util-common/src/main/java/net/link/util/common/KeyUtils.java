/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.common;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Enumeration;
import net.link.util.InternalInconsistencyException;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;


/**
 * Utility class to load keystore key material.
 *
 * @author fcorneli
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class KeyUtils {

    protected static final int RSA_KEYSIZE           = 1024;
    protected static final int DSA_MODLEN            = 512;
    protected static final int SERIALNUMBER_NUM_BITS = 128;

    static {
        //noinspection NonFinalStaticVariableUsedInClassInitialization
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
                keyStorePassword == null? null: keyStorePassword.toCharArray(), keyEntryPassword == null? null: keyEntryPassword.toCharArray() );
    }

    public static PrivateKeyEntry loadPrivateKeyEntry(String keystoreType, InputStream keyStoreInputStream, String keyStorePassword, String keyEntryPassword,
                                                      String alias) {

        if (alias != null)
            return loadPrivateKeyEntry( keystoreType, keyStoreInputStream, //
                    keyStorePassword == null? null: keyStorePassword.toCharArray(), keyEntryPassword == null? null: keyEntryPassword.toCharArray(), alias );

        return loadFirstPrivateKeyEntry( keystoreType, keyStoreInputStream, //
                keyStorePassword == null? null: keyStorePassword.toCharArray(), keyEntryPassword == null? null: keyEntryPassword.toCharArray() );
    }

    public static ImmutableMap<String, X509Certificate> loadCertificates(String keystoreType, InputStream keyStoreInputStream, String keyStorePassword,
                                                                         Predicate<String> ignoreAlias) {

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
            throw new InternalInconsistencyException( "could not get aliases", e );
        }
        String alias = null;
        while (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
            try {
                if (keyStore.isKeyEntry( alias ))
                    break;
            }
            catch (KeyStoreException e) {
                throw new InternalInconsistencyException( e );
            }

            alias = null;
        }
        if (alias == null)
            throw new InternalInconsistencyException( "no private key found in keystore" );

        /* Get the private key entry. */
        try {
            return (PrivateKeyEntry) keyStore.getEntry( alias, new KeyStore.PasswordProtection( keyEntryPassword ) );
        }
        catch (UnrecoverableEntryException e) {
            throw new InternalInconsistencyException( "error retrieving key", e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( "error retrieving key", e );
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "error retrieving key", e );
        }
    }

    public static PrivateKeyEntry loadPrivateKeyEntry(String keystoreType, InputStream keyStoreInputStream, char[] keyStorePassword, char[] keyEntryPassword,
                                                      String alias) {

        /* Find the keystore. */
        KeyStore keyStore = loadKeyStore( keystoreType, keyStoreInputStream, keyStorePassword );
        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "could not get aliases", e );
        }
        if (!aliases.hasMoreElements())
            throw new InternalInconsistencyException( "keystore is empty" );

        try {
            if (!keyStore.isKeyEntry( alias ))
                throw new InternalInconsistencyException( String.format( "not key entry: %s", alias ) );
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "key store error", e );
        }

        /* Get the private key entry. */
        try {
            return (PrivateKeyEntry) keyStore.getEntry( alias, new KeyStore.PasswordProtection( keyEntryPassword ) );
        }
        catch (UnrecoverableEntryException e) {
            throw new InternalInconsistencyException( "error retrieving key", e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( "error retrieving key", e );
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "error retrieving key", e );
        }
    }

    public static KeyStore addEntry(KeyStore keyStore, KeyStore.Entry entry, char[] keyEntryPassword, String alias) {

        try {
            keyStore.setEntry( alias, entry, new KeyStore.PasswordProtection( keyEntryPassword ) );

            return keyStore;
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( String.format( "could not set new entry on keystore for alias: %s", alias ), e );
        }
    }

    public static ImmutableMap<String, X509Certificate> loadCertificates(String keystoreType, InputStream keyStoreInputStream, char[] keyStorePassword,
                                                                         Predicate<String> ignoreAlias) {

        return getCertificates( loadKeyStore( keystoreType, keyStoreInputStream, keyStorePassword ), ignoreAlias );
    }

    public static ImmutableMap<String, X509Certificate> getCertificates(KeyStore keyStore, Predicate<String> ignoreAlias) {

        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "could not enumerate aliases", e );
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
                throw new InternalInconsistencyException( String.format( "error retrieving certificate, alias=%s", alias ), e );
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
            throw new InternalInconsistencyException( e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (CertificateException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( e );
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
            throw new InternalInconsistencyException( "RSA not supported", e );
        }
    }

    public static PrivateKeyEntry generatePrivateKeyEntry(String dn) {

        return generatePrivateKeyEntry( KeyAlgorithm.RSA, dn );
    }

    public static PrivateKeyEntry generatePrivateKeyEntry(KeyAlgorithm keyAlgorithm, String dn) {

        KeyPair keyPair = generateKeyPair( keyAlgorithm );
        X509Certificate certificate = generateSelfSignedCertificate( keyPair, dn );
        return new PrivateKeyEntry( keyPair.getPrivate(), new Certificate[] { certificate } );
    }

    public static KeyPair generateKeyPair(String algorithm)
            throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance( algorithm );
        SecureRandom random = new SecureRandom();
        if ("RSA".equals( keyPairGenerator.getAlgorithm() ))
            try {
                keyPairGenerator.initialize( new RSAKeyGenParameterSpec( RSA_KEYSIZE, RSAKeyGenParameterSpec.F4 ), random );
            }
            catch (InvalidAlgorithmParameterException e) {
                throw new InternalInconsistencyException( "KeyGenParams incompatible with key generator.", e );
            }
        else if (keyPairGenerator instanceof DSAKeyPairGenerator) {
            DSAKeyPairGenerator dsaKeyPairGenerator = (DSAKeyPairGenerator) keyPairGenerator;
            dsaKeyPairGenerator.initialize( DSA_MODLEN, false, random );
        }

        return keyPairGenerator.generateKeyPair();
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn) {

        DateTime now = new DateTime();
        DateTime future = now.plusYears( 10 );
        return generateSelfSignedCertificate( keyPair, dn, now, future, null, true, false );
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String dn, DateTime notBefore, DateTime notAfter,
                                                                @Nullable String signatureAlgorithm, boolean caCert, boolean timeStampingPurpose) {

        return generateCertificate( keyPair.getPublic(), dn, keyPair.getPrivate(), null, notBefore, notAfter, signatureAlgorithm, caCert, timeStampingPurpose,
                null );
    }

    public static X509Certificate generateCertificate(PublicKey subjectPublicKey, String subjectDn, PrivateKey issuerPrivateKey,
                                                      @Nullable X509Certificate issuerCert, DateTime notBefore, DateTime notAfter, String inSignatureAlgorithm,
                                                      boolean caCert, boolean timeStampingPurpose, @Nullable URI ocspUri) {

        try {
            String signatureAlgorithm = inSignatureAlgorithm;
            if (null == signatureAlgorithm)
                signatureAlgorithm = String.format( "SHA1With%s", issuerPrivateKey.getAlgorithm() );

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
            ContentSigner signer = new JcaContentSignerBuilder( signatureAlgorithm ).build( issuerPrivateKey );
            certificateBuilder.addExtension( X509Extension.subjectKeyIdentifier, false, createSubjectKeyId( subjectPublicKey ) );
            PublicKey issuerPublicKey;
            if (null != issuerCert)
                issuerPublicKey = issuerCert.getPublicKey();
            else
                issuerPublicKey = subjectPublicKey;
            certificateBuilder.addExtension( X509Extension.authorityKeyIdentifier, false, createAuthorityKeyId( issuerPublicKey ) );

            certificateBuilder.addExtension( X509Extension.basicConstraints, false, new BasicConstraints( caCert ) );

            if (timeStampingPurpose)
                certificateBuilder.addExtension( X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage( KeyPurposeId.id_kp_timeStamping ) );

            if (null != ocspUri) {
                GeneralName ocspName = new GeneralName( GeneralName.uniformResourceIdentifier, ocspUri.toString() );
                AuthorityInformationAccess authorityInformationAccess = new AuthorityInformationAccess( X509ObjectIdentifiers.ocspAccessMethod, ocspName );
                certificateBuilder.addExtension( X509Extension.authorityInfoAccess, false, authorityInformationAccess );
            }

            // build
            return new JcaX509CertificateConverter().setProvider( "BC" ).getCertificate( certificateBuilder.build( signer ) );
        }
        catch (CertificateException e) {
            throw new InternalInconsistencyException( "X.509 is not supported.", e );
        }
        catch (OperatorCreationException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (CertIOException e) {
            throw new InternalInconsistencyException( e );
        }
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    private static SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey) {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );

            return new SubjectKeyIdentifier( info );
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( "Can't read from a ByteArrayInputStream?", e );
        }
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    private static AuthorityKeyIdentifier createAuthorityKeyId(PublicKey publicKey) {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream( publicKey.getEncoded() );
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo( (ASN1Sequence) new ASN1InputStream( bais ).readObject() );

            return new AuthorityKeyIdentifier( info );
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( "Can't read from a ByteArrayInputStream?", e );
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
            try {

                keyStore.store( keyStoreOut, keyStorePassword );
            }
            finally {
                keyStoreOut.close();
            }
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( "Key Store can't be created or stored.", e );
        }
        catch (CertificateException e) {
            throw new InternalInconsistencyException( "Certificate couldn't be stored.", e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( "KeyStores integrity algorithm not supported.", e );
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "PKCS12 KeyStores not supported or store does not support the key or certificate.", e );
        }
    }

    public static KeyStore newKeyStore(PrivateKey privateKey, X509Certificate certificate, char[] keyStorePassword, char[] keyEntryPassword) {

        try {
            KeyStore keyStore = newKeyStore();
            keyStore.setKeyEntry( "default", privateKey, keyEntryPassword, new Certificate[] { certificate } );

            return keyStore;
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "PKCS12 KeyStores not supported or store does not support the key or certificate.", e );
        }
    }

    public static KeyStore newKeyStore() {

        try {
            KeyStore keyStore = KeyStore.getInstance( "pkcs12" );
            keyStore.load( null, null );

            return keyStore;
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( "Key Store can't be created or stored.", e );
        }
        catch (CertificateException e) {
            throw new InternalInconsistencyException( "Certificate couldn't be stored.", e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( "KeyStores integrity algorithm not supported.", e );
        }
        catch (KeyStoreException e) {
            throw new InternalInconsistencyException( "PKCS12 KeyStores not supported or store does not support the key or certificate.", e );
        }
    }

    public static void extractCertificate(PrivateKeyEntry privateKeyEntry, File certificateFile) {

        Certificate certificate = privateKeyEntry.getCertificate();
        try {
            FileUtils.writeByteArrayToFile( certificateFile, certificate.getEncoded() );
        }
        catch (CertificateEncodingException e) {
            throw new InternalInconsistencyException( "error encoding certificate ", e );
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( "error writing out certificate ", e );
        }
    }
}
