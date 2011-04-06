/*
 * SafeOnline project.
 *
 * Copyright (c) 2006-2011 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 ******************************************************************************/

package net.link.util.test;

import static org.easymock.EasyMock.*;

import java.security.KeyPair;
import java.util.List;
import java.util.UUID;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import net.link.util.common.CertificateChain;
import net.link.util.test.pkix.PkiTestUtils;
import net.link.util.test.session.DummyLoginModule;
import net.link.util.test.session.JaasTestUtils;
import net.link.util.test.web.ws.WebServiceTestManager;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import org.joda.time.Duration;


/**
 * <i>04 06, 2011</i>
 *
 * @author lhunath
 */
public abstract class AbstractWSTest<T> extends AbstractUnitTests<T> {

    protected WebServiceTestManager webServiceTestManager;
    protected T                     port;

    protected long             applicationId;
    protected String           testSubjectId;
    protected CertificateChain clientCertificateChain;
    protected CertificateChain serverCertificateChain;

    @Override
    protected void _setUp()
            throws Exception {

        super._setUp();

        String wsSecurityConfigServiceJndiName = "wsSecurityConfigurationServiceJndiName";

        // Generic Data
        applicationId = 1234567890;
        testSubjectId = UUID.randomUUID().toString();

        // WS servlet
        webServiceTestManager = new WebServiceTestManager();
        webServiceTestManager.setUp( newPortImplementation() );
        webServiceTestManager.becomeEndpointOf( port = newClientPort() );
        //InjectionInstanceResolver.clearInstanceCache();

        // WS Security
        KeyPair clientKeyPair = PkiTestUtils.generateKeyPair();
        clientCertificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( clientKeyPair, "CN=Test" ) );
        WSSecurityConfiguration mockWSSecurityClientConfig = createMock( WSSecurityConfiguration.class );
        expect( mockWSSecurityClientConfig.isCertificateChainTrusted( clientCertificateChain ) ).andStubReturn( true );
        expect( mockWSSecurityClientConfig.getIdentityCertificateChain() ).andStubReturn( clientCertificateChain );
        expect( mockWSSecurityClientConfig.getPrivateKey() ).andStubReturn( clientKeyPair.getPrivate() );
        expect( mockWSSecurityClientConfig.isOutboundSignatureNeeded() ).andStubReturn( true );
        expect( mockWSSecurityClientConfig.isInboundSignatureOptional() ).andStubReturn( false );
        expect( mockWSSecurityClientConfig.getMaximumAge() ).andStubReturn( new Duration( Long.MAX_VALUE ) );

        KeyPair serverKeyPair = PkiTestUtils.generateKeyPair();
        serverCertificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( serverKeyPair, "CN=linkID" ) );
        WSSecurityConfiguration mockWSSecurityServerConfig = createMock( WSSecurityConfiguration.class );
        expect( mockWSSecurityServerConfig.isCertificateChainTrusted( serverCertificateChain ) ).andStubReturn( true );
        expect( mockWSSecurityServerConfig.getIdentityCertificateChain() ).andStubReturn( serverCertificateChain );
        expect( mockWSSecurityServerConfig.getPrivateKey() ).andStubReturn( serverKeyPair.getPrivate() );
        expect( mockWSSecurityServerConfig.isOutboundSignatureNeeded() ).andStubReturn( true );
        expect( mockWSSecurityServerConfig.isInboundSignatureOptional() ).andStubReturn( false );
        expect( mockWSSecurityServerConfig.getMaximumAge() ).andStubReturn( new Duration( Long.MAX_VALUE ) );

        // WS Security: Client config is added to client port.
        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityHandler( mockWSSecurityClientConfig );
        handlerChain.add( wsSecurityHandler );
        binding.setHandlerChain( handlerChain );

        // WS Security: Server config is found via JNDI.
        jndiTestUtils.bindComponent( "java:comp/env/wsSecurityConfigurationServiceJndiName", wsSecurityConfigServiceJndiName );
        jndiTestUtils.bindComponent( wsSecurityConfigServiceJndiName, mockWSSecurityServerConfig );

        // JAAS
        JaasTestUtils.initJaasLoginModule( DummyLoginModule.class );
    }

    protected void _tearDown()
            throws Exception {

        super._tearDown();

        if (webServiceTestManager != null)
            webServiceTestManager.tearDown();
    }

    protected abstract T newClientPort();

    protected abstract T newPortImplementation();
}
