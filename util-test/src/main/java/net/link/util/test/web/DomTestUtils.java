/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.web;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class DomTestUtils {

    private DomTestUtils() {

        // empty
    }

    public static Document parseDocument(String documentString)
            throws Exception {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware( true );
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        StringReader stringReader = new StringReader( documentString );
        InputSource inputSource = new InputSource( stringReader );
        return domBuilder.parse( inputSource );
    }

    public static void saveDocument(Document document, File outputFile)
            throws TransformerException {

        Source source = new DOMSource( document );
        Result streamResult = new StreamResult( outputFile );
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( source, streamResult );
    }

    public static Document loadDocument(InputStream documentInputStream)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware( true );
        DocumentBuilder documentBuilder;
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse( documentInputStream );
        return document;
    }

    public static String domToString(Node domNode)
            throws TransformerException {

        Source source = new DOMSource( domNode );
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult( stringWriter );
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        transformer.transform( source, result );
        return stringWriter.toString();
    }
}
