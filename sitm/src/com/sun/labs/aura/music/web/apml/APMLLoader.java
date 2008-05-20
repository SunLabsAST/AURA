/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.apml;

import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author plamere
 */
public class APMLLoader {

    private DocumentBuilder builder;

    public APMLLoader() throws IOException {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Can't load parser " + e);
        }
    }

    public APML loadAPML(URL url) throws IOException {
        Document document = null;
        InputStream is = url.openStream();

        synchronized (builder) {
            try {
                document = builder.parse(is);
            } catch (SAXException e) {
                throw new IOException("SAX Parse Error " + e);
            }
        }
        is.close();


        Element head = XmlUtil.getFirstElement(document.getDocumentElement(), "Head");
        String title = XmlUtil.getElementContents(head, "Title");
        Element body = XmlUtil.getFirstElement(document.getDocumentElement(), "Body");

        APML apml = new APML(title);

        String defaultProfile = body.getAttribute("defaultprofile");
        if (defaultProfile != null) {
            apml.setDefaultProfile(defaultProfile);
        }

        NodeList profileNodes = body.getElementsByTagName("Profile");

        for (int i = 0; i < profileNodes.getLength(); i++) {
            Element profileNode = (Element) profileNodes.item(i);
            Profile profile = loadProfile(profileNode);
            apml.addProfile(profile);
        }
        return apml;
    }

    private Profile loadProfile(Element profileNode) throws IOException {
        String name = profileNode.getAttribute("name");
        Concept[] implicitConcepts = loadConcepts(profileNode, "ImplicitData");
        Concept[] explicitConcepts = loadConcepts(profileNode, "ExplicitData");
        return new Profile(name, implicitConcepts, explicitConcepts);
    }

    private Concept[] loadConcepts(Element profileNode, String conceptName) throws IOException {
        List<Concept> clist = new ArrayList<Concept>();
        Node conceptBucket = XmlUtil.getDescendent(profileNode, conceptName);
        if (conceptBucket != null) {
            Node concepts = XmlUtil.getDescendent(conceptBucket, "Concepts");
            if (concepts != null) {
                List<Node> conceptList = XmlUtil.getDescendents(concepts, "Concept");
                if (conceptList != null) {
                    for (Node node : conceptList) {
                        Element conceptElement = (Element) node;
                        //           <Concept key="attention" value="0.99" from="GatheringTool.com" updated="2007-03-11T01:55:00Z" />
                        String key = conceptElement.getAttribute("key");
                        String value = conceptElement.getAttribute("value");

                        float fval = Float.parseFloat(value);
                        String from = conceptElement.getAttribute("from");
                        String updated = conceptElement.getAttribute("updated");
                        Concept concept = new Concept(key, fval, from, updated);
                        clist.add(concept);
                    }
                }
            }
        }
        return clist.toArray(new Concept[0]);
    }

    public static void main(String[] args) throws IOException {
        APMLLoader loader = new APMLLoader();
        APML apml = loader.loadAPML(new URL(args[0]));
        System.out.println("apml [" + apml + "]");
    }
}
