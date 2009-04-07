/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.search.music.web.apml;

import com.sun.labs.search.music.web.Utilities;
import com.sun.labs.search.music.web.XmlUtil;
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

                        key = Utilities.XMLUnescape(key);
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
