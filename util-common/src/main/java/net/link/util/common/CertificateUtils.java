package net.link.util.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;


/**
 * <h2>{@link CertificateUtils}<br> <sub>[in short] (TODO).</sub></h2>
 *
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
}
