package net.link.util.common;

import be.fedict.trust.linker.*;
import be.fedict.trust.policy.AlgorithmPolicy;
import be.fedict.trust.revocation.RevocationData;
import java.security.cert.X509Certificate;
import java.util.Date;


/**
 * Created by wvdhaute
 * Date: 17/12/13
 * Time: 16:49
 */
public class LazyPublicKeyTrustLinker extends PublicKeyTrustLinker {

    @Override
    public TrustLinkerResult hasTrustLink(X509Certificate childCertificate, X509Certificate certificate, Date validationDate, RevocationData revocationData,
                                          AlgorithmPolicy algorithmPolicy)
            throws TrustLinkerResultException, Exception {

        super.hasTrustLink( childCertificate, certificate, validationDate, revocationData, algorithmPolicy );

        return TrustLinkerResult.TRUSTED;
    }
}
