/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.common;

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


/**
 * W3C DOM utility class.
 *
 * @author fcorneli
 */
public abstract class DomUtils {

    /**
     * Parses the given string to a DOM object.
     *
     * @throws Exception
     */
    public static Document parseDocument(String documentString)
            throws Exception {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware( true );
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        StringReader stringReader = new StringReader( documentString );
        InputSource inputSource = new InputSource( stringReader );
        return domBuilder.parse( inputSource );
    }

    /**
     * Saves a DOM document to the given output file.
     *
     * @throws TransformerException
     */
    public static void saveDocument(Document document, File outputFile)
            throws TransformerException {

        Source source = new DOMSource( document );
        Result streamResult = new StreamResult( outputFile );
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( source, streamResult );
    }

    /**
     * Loads a DOM document from the given input stream.
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document loadDocument(InputStream documentInputStream)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware( true );
        DocumentBuilder documentBuilder;
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse( documentInputStream );
        return document;
    }

    /**
     * Transforms a DOM node (e.g. DOM element or DOM document) to a String.
     *
     * @throws TransformerException
     */
    public static String domToString(Node domNode)
            throws TransformerException {

        return domToString( domNode, false );
    }

    /**
     * Transforms a DOM node (e.g. DOM element or DOM document) to a String.
     *
     * @throws TransformerException
     */
    public static String domToString(Node domNode, boolean indent)
            throws TransformerException {

        Source source = new DOMSource( domNode );
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult( stringWriter );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
        transformer.setOutputProperty( OutputKeys.INDENT, indent? "yes": "no" );
        transformer.transform( source, result );

        return stringWriter.toString();
    }
}
