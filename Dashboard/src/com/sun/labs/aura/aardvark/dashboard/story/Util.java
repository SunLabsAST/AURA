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

    static List<Story> loadStories(InputStream stream) throws IOException {
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
                String imageUrl = getElementContents(storyElement, "imageUrl");
                NodeList classList = storyElement.getElementsByTagName("class");

                story.setTitle(title);
                story.setDescription(description);
                story.setUrl(url);
                story.setSource(source);
                story.setImageUrl(imageUrl);
                story.setScore(score);

                for (int j = 0; j < classList.getLength(); j++) {
                    Element classNode = (Element) classList.item(j);
                    String className = classNode.getTextContent();
                    float classScore = Float.parseFloat(classNode.getAttribute("score"));
                    story.addClassification(new Classification(className, classScore));
                }
                stories.add(story);
            }
        } catch (SAXException e) {
            System.out.println("parse problem " + e);
            throw new IOException("Parsing problem " + e);
        } catch (ParserConfigurationException e) {
            System.out.println("parse problem " + e);
            throw new IOException("Can't load parser " + e);
        }
        Collections.shuffle(stories);
        return stories;
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
}
