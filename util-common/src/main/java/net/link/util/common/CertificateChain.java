package net.link.util.common;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.lyndir.lhunath.lib.system.util.ObjectUtils;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.security.auth.x500.X500Principal;
import org.jetbrains.annotations.NotNull;


/**
 * <h2>{@link CertificateChain}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>04 01, 2011</i> </p>
 *
 * @author lhunath
 */
public class CertificateChain implements Iterable<X509Certificate>, Serializable {

    private final LinkedList<X509Certificate> orderedCertificateChain = new LinkedList<X509Certificate>();

    public CertificateChain(X509Certificate... unorderedCertificateChain) {

        this( ImmutableList.copyOf( unorderedCertificateChain ) );
    }

    public CertificateChain(Collection<X509Certificate> unorderedCertificateChain) {

        if (unorderedCertificateChain.isEmpty() || unorderedCertificateChain.size() == 1)
            orderedCertificateChain.addAll( unorderedCertificateChain );

        else if (unorderedCertificateChain.size() == 2) {
            X509Certificate first = Iterables.get( unorderedCertificateChain, 0 );
            X509Certificate second = Iterables.get( unorderedCertificateChain, 1 );

            if (CertificateUtils.isSelfSigned( first )) {
                orderedCertificateChain.add( second );
                orderedCertificateChain.add( first );
            } else {
                orderedCertificateChain.add( first );
                orderedCertificateChain.add( second );
            }
        } else {
            // find self-signed root
            for (X509Certificate rootCertificateCandidate : unorderedCertificateChain) {
                if (CertificateUtils.isSelfSigned( rootCertificateCandidate )) {
                    orderedCertificateChain.add( rootCertificateCandidate );
                    break;
                }
            }

            // now go down
            X509Certificate parentCertificate = orderedCertificateChain.getFirst();
            while (true) {
                final X500Principal parentPrincipal = parentCertificate.getSubjectX500Principal();
                X509Certificate childCertificate = Iterables.find( unorderedCertificateChain, new Predicate<X509Certificate>() {
                    @Override
                    public boolean apply(final X509Certificate input) {

                        return input.getIssuerX500Principal().equals( parentPrincipal );
                    }
                } );
                if (childCertificate != null)
                    orderedCertificateChain.addFirst( parentCertificate = childCertificate );
                else if (unorderedCertificateChain.size() == orderedCertificateChain.size())
                    // No child found for parent & all unordered certificates have been used.  All done.
                    break;
                else
                    // No child found for parent & not all unordered certificates used.
                    throw new IllegalArgumentException( "Given certificate chain is missing some nodes or contains irrelevant nodes." //
                                                        + "\nNodes: " + ObjectUtils.describe( unorderedCertificateChain ) //
                                                        + "\nFailed at: " + ObjectUtils.describe( orderedCertificateChain ) );
            }
        }
    }

    public LinkedList<X509Certificate> getOrderedCertificateChain() {

        return orderedCertificateChain;
    }

    public boolean hasRootCertificate() {

        return getOrderedCertificateChain().size() > 1 && CertificateUtils.isSelfSigned( getOrderedCertificateChain().getLast() );
    }

    /**
     * @return The root {@link X509Certificate}.
     */
    @NotNull
    public X509Certificate getRootCertificate() {

        checkState( hasRootCertificate(), "This chain does not have a root certificate." );

        return getOrderedCertificateChain().getLast();
    }

    /**
     * @return Get the {@link X509Certificate} of the identity.
     */
    @NotNull
    public X509Certificate getIdentityCertificate() {

        checkState( !isEmpty(), "This chain does not have any certificates." );

        return getOrderedCertificateChain().getFirst();
    }

    public CertificateChain getIssuerCertificateChain() {

        LinkedList<X509Certificate> issuerOrderedCertificateChain = new LinkedList<X509Certificate>( getOrderedCertificateChain() );
        if (!CertificateUtils.isSelfSigned( issuerOrderedCertificateChain.getFirst() ))
            issuerOrderedCertificateChain.removeFirst();

        return new CertificateChain( issuerOrderedCertificateChain );
    }

    public boolean isEmpty() {

        return getOrderedCertificateChain().isEmpty();
    }

    @Override
    public Iterator<X509Certificate> iterator() {

        return getOrderedCertificateChain().iterator();
    }

    public X509Certificate[] toArray() {

        return getOrderedCertificateChain().toArray( new X509Certificate[getOrderedCertificateChain().size()] );
    }

    @Override
    public int hashCode() {

        return orderedCertificateChain.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof CertificateChain)
            return orderedCertificateChain.equals( ((CertificateChain) obj).getOrderedCertificateChain() );

        return orderedCertificateChain.equals( obj );
    }

    @Override
    public String toString() {

        return orderedCertificateChain.toString();
    }
}
