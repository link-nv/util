package net.link.util.data;

import java.security.cert.X509Certificate;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;


public class CertificateData extends Data {

    @JsonIgnore
    @XmlTransient
    public X509Certificate certificate;

    public CertificateData() {

    }

    public CertificateData(X509Certificate certificate) {

        this.certificate = certificate;
    }
}
