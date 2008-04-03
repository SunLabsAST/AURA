/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.story;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(stream);
            Element docElement = doc.getDocumentElement();
            NodeList list = docElement.getElementsByTagName("story");

            System.out.println("Found " + list.getLength());
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
        } catch (SAXException e) {
            System.out.println("parse problem " + e);
        } catch (ParserConfigurationException e) {
            System.out.println("parse problem " + e);
        }
        return stories;
    }

    public static List<TagInfo> loadTagInfo(InputStream stream) throws IOException {
        List<TagInfo> infos = new ArrayList<TagInfo>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(stream);
            Element docElement = doc.getDocumentElement();
            NodeList list = docElement.getElementsByTagName("TagInfo");

            System.out.println("Found " + list.getLength());
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                Element tagInfoElement = (Element) node;

                TagInfo ti = new TagInfo();

                String name = tagInfoElement.getAttribute("name");
                float score = Float.parseFloat(tagInfoElement.getAttribute("score"));

                List<ScoredString> docTerms = loadTerms(tagInfoElement, "DocTerms", "DocTerm");
                List<ScoredString> topTerms = loadTerms(tagInfoElement, "TopTerms", "TopTerm");

                ti.setTagName(name);
                ti.setScore(score);
                ti.setDocTerms(docTerms);
                ti.setTopTerms(topTerms);

                infos.add(ti);
            }
        } catch (SAXException e) {
            System.out.println("parse problem " + e);
        } catch (ParserConfigurationException e) {
            System.out.println("parse problem " + e);
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
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(stream);
            Element statusElement = doc.getDocumentElement();

            String entries = getElementContents(statusElement, "entries");
            String feeds = getElementContents(statusElement, "feeds");
            String users = getElementContents(statusElement, "users");
            String taste = getElementContents(statusElement, "taste");
            String entriesPerMinute = getElementContents(statusElement, "entriesPerMinute");

            stats = new Stats(
                    Long.parseLong(entries),
                    Long.parseLong(feeds),
                    Long.parseLong(users),
                    Long.parseLong(taste),
                    Float.parseFloat(entriesPerMinute));
        } catch (SAXException e) {
            System.out.println("parse problem " + e);
            throw new IOException("Parsing problem " + e);
        } catch (ParserConfigurationException e) {
            System.out.println("parse problem " + e);
            throw new IOException("Can't load parser " + e);
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

    public static Element getFirstElement(Element element, String elementName) throws IOException {
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
