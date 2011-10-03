package net.link.util.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.*;
import java.util.List;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link CertificateUtils}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>04 01, 2011</i> </p>
 *
 * @author lhunath
 */
public abstract class CertificateUtils {

    public static List<X509Certificate> toX509(Iterable<? extends Certificate> certificates) {

        return toX509( Iterables.toArray( certificates, Certificate.class ) );
    }

    public static List<X509Certificate> toX509(Certificate... certificates) {

        ImmutableList.Builder<X509Certificate> x509Certificates = ImmutableList.builder();
        for (Certificate certificate : certificates)
            x509Certificates.add( toX509( certificate ) );

        return x509Certificates.build();
    }

    public static X509Certificate toX509(Certificate certificate) {

        return (X509Certificate) certificate;
    }

    public static boolean isSelfSigned(X509Certificate certificate) {

        return certificate.getIssuerX500Principal().equals( certificate.getSubjectX500Principal() );
    }

    /**
     * Decodes a given DER encoded X509 certificate.
     *
     * @param encodedCertificate certificate bytes to decode
     *
     * @return the X509 Certificate
     *
     * @throws CertificateException could not decode certificate.
     */
    @Nullable
    public static X509Certificate decodeCertificate(byte[] encodedCertificate)
            throws CertificateException {

        if (null == encodedCertificate)
            return null;
        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance( "X.509" );
        }
        catch (CertificateException e) {
            throw new InternalInconsistencyException( String.format( "cert factory error: %s", e.getMessage() ), e );
        }
        InputStream certInputStream = new ByteArrayInputStream( encodedCertificate );
        return (X509Certificate) certificateFactory.generateCertificate( certInputStream );
    }
}
