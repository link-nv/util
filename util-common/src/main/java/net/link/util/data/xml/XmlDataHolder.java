package net.link.util.data.xml;

import net.link.util.logging.Logger;
import net.link.util.InternalInconsistencyException;
import java.io.*;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.link.util.common.CertificateUtils;
import net.link.util.data.*;


public class XmlDataHolder<C> extends AbstractDataHolder<C> {

    static final Logger logger = Logger.get( XmlDataHolder.class );

    @SuppressWarnings("unchecked")
    @Override
    protected C loadData(final FileReader reader) {

        try {

            JAXBContext jc = JAXBContext.newInstance( dataType );
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            unmarshaller.setEventHandler( new ValidationEventHandler() {

                @Override
                public boolean handleEvent(final ValidationEvent event) {

                    return XmlDataHolder.this.handleEvent( event );
                }
            } );

            // add custom XML Adapters
            for (XmlAdapter xmlAdapter : getXmlAdapters()) {
                unmarshaller.setAdapter( xmlAdapter );
            }

            unmarshaller.setListener( new Unmarshaller.Listener() {

                @Override
                public void beforeUnmarshal(Object target, Object parent) {

                    XmlDataHolder.this.beforeUnmarshal( target, parent );
                }

                @Override
                public void afterUnmarshal(Object target, Object parent) {

                    XmlDataHolder.this.afterUnmarshal( target, parent );
                }
            } );

            return (C) unmarshaller.unmarshal( reader );
        }
        catch (JAXBException e) {
            throw new InternalInconsistencyException( "Failed to load data", e );
        }
    }

    @Override
    public void exportData(final Writer writer, final Object value)
            throws IOException {

        try {
            JAXBContext jc = JAXBContext.newInstance( dataType );
            Marshaller marshaller = jc.createMarshaller();

            // add custom XML Adapters
            for (XmlAdapter<?, ?> xmlAdapter : getXmlAdapters()) {
                marshaller.setAdapter( xmlAdapter );
            }

            // only want to output the data content, not the location
            marshaller.setListener( new Marshaller.Listener() {

                @Override
                public void beforeMarshal(Object source) {

                    XmlDataHolder.this.beforeMarshal( source );
                }

                @Override
                public void afterMarshal(Object source) {

                    XmlDataHolder.this.afterMarshal( source );
                }
            } );

            marshaller.marshal( value, writer );
        }
        catch (JAXBException e) {
            throw new InternalInconsistencyException( "Failed to load data", e );
        }
    }

    @Override
    protected Logger getLogger() {

        return logger;
    }

    /**
     * Override me to add some custom {@link XmlAdapter}.
     *
     * @return the XML adapters to add during marshalling/unmarshalling.
     */
    public List<XmlAdapter<?, ?>> getXmlAdapters() {

        return new LinkedList<XmlAdapter<?, ?>>();
    }

    // hooks

    /**
     * Override me for some customizations before unmarshalling
     *
     * @param target target
     * @param parent parent
     */

    public void beforeUnmarshal(Object target, Object parent) {

    }

    /**
     * Override me for some customizations after unmarshalling
     *
     * @param target target
     * @param parent parent
     */
    public void afterUnmarshal(Object target, Object parent) {

        if (target instanceof Data) {
            // load in data from Data.location
            Data data = (Data) target;
            if (null != data.location) {
                data.data = getData( data.location );
            }
        }

        if (target instanceof CertificateData) {

            // load X509Certificate from data
            CertificateData certificateData = (CertificateData) target;
            if (null != certificateData.data) {
                try {
                    certificateData.certificate = CertificateUtils.decodeCertificate( certificateData.data );
                }
                catch (CertificateException e) {
                    throw new InternalInconsistencyException( e );
                }
            }
        }
    }

    /**
     * Override me for some customizations before marshalling
     *
     * @param source source
     */
    public void beforeMarshal(Object source) {

        if (source instanceof Data) {
            // only want to output the data content, not the location
            Data data = (Data) source;
            data.location = null;
        }
    }

    /**
     * Override me for some customizations after marshalling
     *
     * @param source source
     */
    public void afterMarshal(Object source) {

    }

    /**
     * Override me  to handle validation errors.
     *
     * @param event the validation event
     *
     * @return whether or not unmarshalling should continue.
     */
    public boolean handleEvent(final ValidationEvent event) {

        logger.wrn( "Validation event: %s", event );
        return true;
    }
}
