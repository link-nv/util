package net.link.util.common;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.*;
import org.jetbrains.annotations.NotNull;


/**
 * <h2>{@link CertificateChain}<br> <sub>[in short].</sub></h2>
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

        else {
            // pick a random cert
            X509Certificate nextCertificate = unorderedCertificateChain.iterator().next();

            // build towards the root
            while (true) {
                final X509Certificate currentCertificate = nextCertificate;
                orderedCertificateChain.addLast( currentCertificate );

                // we're at the self-signed root
                if (CertificateUtils.isSelfSigned( currentCertificate )) {
                    break;
                }

                try {
                    // find the parent
                    X509Certificate parent = Iterables.find( unorderedCertificateChain, new Predicate<X509Certificate>() {
                        @Override
                        public boolean apply(final X509Certificate input) {

                            return input.getSubjectX500Principal().equals( currentCertificate.getIssuerX500Principal() );
                        }
                    } );

                    nextCertificate = parent;
                }
                catch (NoSuchElementException ignored) {
                    // there is no parent so stop finding parents
                    break;
                }
            }

            // build towards the bottom
            nextCertificate = orderedCertificateChain.getFirst();
            while (true) {
                final X509Certificate currentCertificate = nextCertificate;

                try {
                    // find a child
                    X509Certificate child = Iterables.find( unorderedCertificateChain, new Predicate<X509Certificate>() {
                        @Override
                        public boolean apply(final X509Certificate input) {

                            return (input.getIssuerX500Principal().equals( currentCertificate.getSubjectX500Principal() ) && !CertificateUtils.isSelfSigned(
                                    input ));
                        }
                    } );

                    nextCertificate = child;
                    orderedCertificateChain.addFirst( child );
                }
                catch (NoSuchElementException ignored) {
                    // there is no child anymore so stop finding children
                    break;
                }
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
