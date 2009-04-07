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

package com.sun.labs.aura.aardvark.dashboard.story;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
<story score=".54">
<title> Cleveland beats Truman! </title>
<description> In a stunning upset, Cleveland beats Truman to become the 34th president of the United States of America </description>
<url>http://cnn.com/breaking_news.rss</url>
<imageUrl>http://cnn.com/truman.jpg</imageUrl>
<class name="politics" score=".32"/>
<class name="presidents" score=".32"/>
</story>
 * @author plamere
 */
public class Util {

    public static List<Story> loadStories(InputStream stream) throws IOException {
        List<Story> stories = new ArrayList<Story>();
        Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(stream);
            Element docElement = doc.getDocumentElement();
            NodeList list = docElement.getElementsByTagName("story");

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                Element storyElement = (Element) node;

                Story story = new Story();

                float score = Float.parseFloat(storyElement.getAttribute("score"));
                String title = getElementContents(storyElement, "title");
                String source = getElementContents(storyElement, "source");
                String description = getElementContents(storyElement, "description");
                String url = getElementContents(storyElement, "url");
                String pulltime = getElementContents(storyElement, "pulltime");
                String length = getElementContents(storyElement, "length");
                String imageUrl = getElementContents(storyElement, "imageUrl");


                story.setTitle(title);
                story.setDescription(description);
                story.setUrl(url);
                story.setSource(source);
                story.setImageUrl(imageUrl);
                story.setScore(score);
                story.setPulltime(Long.parseLong(pulltime));
                story.setLength(Integer.parseInt(length));

                {
                    Element tagRoot = getFirstElement(storyElement, "tags");
                    if (tagRoot != null) {
                        NodeList tags = tagRoot.getElementsByTagName("tag");
                        if (tags != null) {
                            for (int j = 0; j < tags.getLength(); j++) {
                                Element tagNode = (Element) tags.item(j);
                                String tagName = tagNode.getTextContent();
                                float tagScore = Float.parseFloat(tagNode.getAttribute("score"));
                                story.addTags(new ScoredString(tagName, tagScore));
                            }
                        }
                    }
                }

                {
                    Element tagRoot = getFirstElement(storyElement, "autotags");
                    if (tagRoot != null) {
                        NodeList autotags = tagRoot.getElementsByTagName("autotag");
                        if (autotags != null) {
                            for (int j = 0; j < autotags.getLength(); j++) {
                                Element tagNode = (Element) autotags.item(j);
                                String tagName = tagNode.getTextContent();
                                float tagScore = Float.parseFloat(tagNode.getAttribute("score"));
                                story.addAutotags(new ScoredString(tagName, tagScore));
                            }
                        }
                    }
                }

                {
                    Element tagRoot = getFirstElement(storyElement, "topterms");
                    if (tagRoot != null) {
                        NodeList topterms = tagRoot.getElementsByTagName("topterm");
                        if (topterms != null) {
                            for (int j = 0; j < topterms.getLength(); j++) {
                                Element termNode = (Element) topterms.item(j);
                                String termName = termNode.getTextContent();
                                float termScore = Float.parseFloat(termNode.getAttribute("score"));
                                story.addTopTerms(new ScoredString(termName, termScore));
                            }
                        }
                    }
                }

                /*
                NodeList classList = storyElement.getElementsByTagName("class");
                for (int j = 0; j < classList.getLength(); j++) {
                Element classNode = (Element) classList.item(j);
                String className = classNode.getTextContent();
                float classScore = Float.parseFloat(classNode.getAttribute("score"));
                story.addTags(new ScoredString(className, classScore));
                }
                 */

                stories.add(story);
            }
        } catch (SAXParseException e) {
            System.out.println("loadStories: parse problem at line " + e.getLineNumber() + " col " + e.getColumnNumber());
            if (doc != null) {
                dumpDocument(doc);
            }
        } catch (SAXException e) {
            System.out.println("loadStories: General Sax exception " + e);
            if (doc != null) {
                dumpDocument(doc);
            }
        } catch (ParserConfigurationException e) {
            System.out.println("loadStories: parse problem " + e);
        }
        return stories;
    }

    public static List<TagInfo> loadTagInfo(InputStream stream) throws IOException {
        List<TagInfo> infos = new ArrayList<TagInfo>();
        Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(stream);
            Element docElement = doc.getDocumentElement();
            NodeList list = docElement.getElementsByTagName("TagInfo");

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                Element tagInfoElement = (Element) node;

                TagInfo ti = new TagInfo();

                String name = tagInfoElement.getAttribute("name");
                float score = Float.parseFloat(tagInfoElement.getAttribute("score"));

                List<ScoredString> docTerms = loadTerms(tagInfoElement, "DocTerms", "DocTerm");
                List<ScoredString> topTerms = loadTerms(tagInfoElement, "TopTerms", "TopTerm");
                List<ScoredString> simTags = loadTerms(tagInfoElement, "SimTags", "SimTag");

                ti.setTagName(name);
                ti.setScore(score);
                ti.setDocTerms(docTerms);
                ti.setTopTerms(topTerms);
                ti.setSimTags(simTags);

                infos.add(ti);
            }
        } catch (SAXParseException e) {
            System.out.println("loadTagInfo: parse problem at line " + e.getLineNumber() + " col " + e.getColumnNumber());
            if (doc != null) {
                dumpDocument(doc);
            }
        } catch (SAXException e) {
            System.out.println("loadTagInfo: General Sax exception " + e);
            if (doc != null) {
                dumpDocument(doc);
            }
        } catch (ParserConfigurationException e) {
            System.out.println("loadTagInfo: parse problem " + e);
        }
        return infos;
    }


    private static List<ScoredString> loadTerms(Element root, String listName, String itemNodeName) throws IOException {
        List<ScoredString> termList = new ArrayList<ScoredString>();
        Element listRoot = getFirstElement(root, listName);
        if (listRoot != null) {
            NodeList items = listRoot.getElementsByTagName(itemNodeName);
            if (items != null) {
                for (int j = 0; j < items.getLength(); j++) {
                    Element itemNode = (Element) items.item(j);
                    String itemName = itemNode.getAttribute("name");
                    float itemScore = Float.parseFloat(itemNode.getAttribute("score"));
                    termList.add(new ScoredString(itemName, itemScore));
                }
            }
        }
        return termList;
    }

    /*
    <entries>115577</entries>
    <feeds>8338</feeds>
    <users>1</users>
    <taste>81</taste>
    <entriesPerMinute>0.0</entriesPerMinute>
     */
    public static Stats loadStats(InputStream stream) throws IOException {
        Stats stats = null;
        ;
        Document doc = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(stream);
            Element statusElement = doc.getDocumentElement();

            String entries = getElementContents(statusElement, "entries");
            String feeds = getElementContents(statusElement, "feeds");
            String users = getElementContents(statusElement, "users");
            String taste = getElementContents(statusElement, "taste");
            String pulls = getElementContents(statusElement, "pulls");
            String entriesPerMinute = getElementContents(statusElement, "entriesPerMinute");

            stats = new Stats(
                    Long.parseLong(entries),
                    Long.parseLong(feeds),
                    Long.parseLong(pulls),
                    Long.parseLong(users),
                    Long.parseLong(taste),
                    Float.parseFloat(entriesPerMinute));
        } catch (SAXParseException e) {
            System.out.println("loadStats: parse problem at line " + e.getLineNumber() + " col " + e.getColumnNumber());
            if (doc != null) {
                dumpDocument(doc);
            }
        } catch (SAXException e) {
            System.out.println("loadStats: General Sax exception " + e);
            if (doc != null) {
                dumpDocument(doc);
            }
        } catch (ParserConfigurationException e) {
            System.out.println("loadStats: parse problem " + e);
        }
        return stats;
    }

    public static String getElementContents(Element element, String elementName) throws IOException {
        Element first = getFirstElement(element, elementName);
        if (first != null) {
            return first.getTextContent();
        } else {
            return null;
        }
    }

    static void dumpDocument(Document document) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(document);
            Result result = new StreamResult(System.out);

            // Write the DOM document to the file
            // Get Transformer
            Transformer xformer =
                    TransformerFactory.newInstance().newTransformer();
            // Write to a file

            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");
            xformer.setOutputProperty(
                    "{http://xml.apache.org/xalan}indent-amount", "4");

            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            System.out.println("TransformerConfigurationException: " + e);
        } catch (TransformerException e) {
            System.out.println("TransformerException: " + e);
        }
    }

    static Element getFirstElement(Element element, String elementName) throws IOException {
        NodeList list = element.getElementsByTagName(elementName);
        if (list.getLength() >= 1) {
            Element subElement = (Element) list.item(0);
            return subElement;
        } else {
            return null;
        }
    }

    public static void openInBrowser(final String url) {
        // BUG get the browser path from the environment

        Thread t = new Thread() {

            @Override
            public void run() {
                String[] cmds = {"open", url};
                try {
                    Runtime.getRuntime().exec(cmds);
                } catch (IOException ioe) {
                    System.err.println("Couldn't open browser for " + url);
                }
            }
        };
        t.setName("run-browser");
        t.start();
    }
}
