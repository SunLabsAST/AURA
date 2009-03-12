/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.fb;

import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

/**
 *
 */
public class Util {
    /**
     * Generates the root path URL of the application
     *
     * @param req the request
     * @param relative if this should return a relative or absolute url
     *
     * @return the root path URL
     */
    public static String getRootPath(HttpServletRequest req,
            boolean relative) {
        String context = "";
        if (!relative) {
            int port = req.getServerPort();
            context = req.getScheme() + "://" + req.getServerName() +
                    (port != 80 ? ":" + port : "") +
                    req.getContextPath();
        }
        return context;
    }
    
    public static String xmlToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
