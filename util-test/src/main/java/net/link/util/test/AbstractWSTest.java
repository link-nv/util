/*
 * SafeOnline project.
 *
 * Copyright (c) 2006-2011 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 ******************************************************************************/

package net.link.util.test;

import static org.easymock.EasyMock.*;

import com.google.common.collect.ImmutableList;
import java.security.KeyPair;
import java.util.UUID;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
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

    protected long   applicationId;
    protected String testSubjectId;

    private   KeyPair                 clientKeyPair;
    protected CertificateChain        clientCertificateChain;
    private   WSSecurityConfiguration mockWSSecurityClientConfig;

    private   KeyPair                 serverKeyPair;
    protected CertificateChain        serverCertificateChain;
    private   WSSecurityConfiguration mockWSSecurityServerConfig;

    @Override
    protected void setUp()
            throws Exception {

        // Generic Data
        applicationId = 1234567890;
        testSubjectId = UUID.randomUUID().toString();

        // WS Security
        // - Client
        mockWSSecurityClientConfig = createMock( WSSecurityConfiguration.class );
        clientKeyPair = PkiTestUtils.generateKeyPair();
        clientCertificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( clientKeyPair, "CN=Test" ) );

        port = newClientPort();
        Binding binding = ((BindingProvider) port).getBinding();
        //noinspection RawUseOfParameterizedType
        binding.setHandlerChain( ImmutableList.<Handler>builder()
                                              .addAll( binding.getHandlerChain() )
                                              .add( new WSSecurityHandler( mockWSSecurityClientConfig ) )
                                              .build() );

        // - Server
        mockWSSecurityServerConfig = createMock( WSSecurityConfiguration.class );
        serverKeyPair = PkiTestUtils.generateKeyPair();
        serverCertificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( serverKeyPair, "CN=linkID" ) );

        jndiTestUtils.bindComponent( "java:comp/env/wsSecurityConfigurationServiceJndiName", "wsSecurityConfigurationServiceJndiName" );
        jndiTestUtils.bindComponent( "wsSecurityConfigurationServiceJndiName", mockWSSecurityServerConfig );

        // WS servlet
        webServiceTestManager = new WebServiceTestManager();
        webServiceTestManager.setUp( newPortImplementation() );
        webServiceTestManager.becomeEndpointOf( port );

        super.setUp();
    }

    @Override
    protected void setUpMocks()
            throws Exception {

        // WS Security
        expect( mockWSSecurityClientConfig.isCertificateChainTrusted( serverCertificateChain ) ).andStubReturn( true );
        expect( mockWSSecurityClientConfig.getIdentityCertificateChain() ).andStubReturn( clientCertificateChain );
        expect( mockWSSecurityClientConfig.getPrivateKey() ).andStubReturn( clientKeyPair.getPrivate() );
        expect( mockWSSecurityClientConfig.isOutboundSignatureNeeded() ).andStubReturn( true );
        expect( mockWSSecurityClientConfig.isInboundSignatureOptional() ).andStubReturn( false );
        expect( mockWSSecurityClientConfig.getMaximumAge() ).andStubReturn( new Duration( Long.MAX_VALUE ) );

        expect( mockWSSecurityServerConfig.isCertificateChainTrusted( clientCertificateChain ) ).andStubReturn( true );
        expect( mockWSSecurityServerConfig.getIdentityCertificateChain() ).andStubReturn( serverCertificateChain );
        expect( mockWSSecurityServerConfig.getPrivateKey() ).andStubReturn( serverKeyPair.getPrivate() );
        expect( mockWSSecurityServerConfig.isOutboundSignatureNeeded() ).andStubReturn( true );
        expect( mockWSSecurityServerConfig.isInboundSignatureOptional() ).andStubReturn( false );
        expect( mockWSSecurityServerConfig.getMaximumAge() ).andStubReturn( new Duration( Long.MAX_VALUE ) );

        // JAAS
        JaasTestUtils.initJaasLoginModule( DummyLoginModule.class );

        super.setUpMocks();
    }

    @Override
    protected void tearDown()
            throws Exception {

        super.tearDown();

        if (webServiceTestManager != null)
            webServiceTestManager.tearDown();
    }

    protected abstract T newClientPort();

    protected abstract T newPortImplementation();
}
