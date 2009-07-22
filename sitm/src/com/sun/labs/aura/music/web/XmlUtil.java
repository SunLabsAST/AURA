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

package com.sun.labs.aura.music.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {

    public static String getElementContents(Element element, String elementName) throws IOException {
        Element first = getFirstElement(element, elementName);
        if (first != null) {
            return first.getTextContent().trim();
        } else {
            return null;
        }
    }

    public static int getElementContentsAsInteger(Element element, String elementName) throws IOException {
        int results = 0;
        Element first = getFirstElement(element, elementName);
        if (first != null) {
            try {
                results = Integer.parseInt(first.getTextContent());
            } catch (NumberFormatException ex) {
            }
        }
        return results;
    }

    public static int getElementAttributeAsInteger(Element element, String elementName, String attributeName) throws IOException {
        int results = 0;
        Element first = getFirstElement(element, elementName);
        if (first != null) {
            try {
                results = Integer.parseInt(first.getAttribute(attributeName));
            } catch (NumberFormatException ex) {
            }
        }
        return results;
    }

    public static Element getFirstElement(Element element, String elementName) throws IOException {
        NodeList list = element.getElementsByTagName(elementName);
        if (list.getLength() >= 1) {
            Element subElement = (Element) list.item(0);
            return subElement;
        } else {
            return null;
        }
    }

    public static Node getDescendent(Node node, String nodeName) throws IOException {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(nodeName)) {
                return child;
            }
        }
        return null;
    }

    public static List<Node> getDescendents(Node node, String nodeName) throws IOException {
        List<Node> childList = new ArrayList<Node>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(nodeName)) {
                childList.add(child);
            }
        }
        return childList;
    }

    public static void dump(Node node) {
        System.out.println("Node: " + node.getNodeName());
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                Node n = nnm.item(i);
                System.out.println("   " + n.getNodeName() + ":" + n.getNodeValue());
            }
        }
    }

    public static String getDescendentText(Node node, String name) throws IOException {
        Node d = getDescendent(node, name);
        if (d != null) {
            return d.getTextContent().trim();
        }
        return null;
    }
}

