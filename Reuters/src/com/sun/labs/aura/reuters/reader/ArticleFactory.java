package com.sun.labs.aura.reuters.reader;

import com.sun.labs.aura.reuters.Article;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *  A factory that reads XML and generates Articles.
 */
public class ArticleFactory extends DefaultHandler {

    XMLReader reader;

    Article art;

    StringBuilder chars;

    StringBuilder pars;

    private static Logger logger = Logger.getLogger(
            ArticleFactory.class.getName());

    Codes industries;

    Codes countries;

    Codes topics;

    Codes currCodes;

    Codes.CodeType currType;

    public ArticleFactory(Codes industries, Codes countries, Codes topics)
            throws SAXException {
        super();
        this.industries = industries;
        this.countries = countries;
        this.topics = topics;
        chars = new StringBuilder();
        pars = new StringBuilder();
        reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(this);
        reader.setEntityResolver(this);
        reader.setErrorHandler(this);
    }

    public Article getArticle(InputStream is) {
        try {
            art = null;
            reader.parse(new InputSource(is));
        } catch(IOException ex) {
            art = null;
            logger.log(Level.SEVERE, "Error reading input", ex);
        } catch(SAXException ex) {
            art = null;
            logger.log(Level.SEVERE, "Error parsing input", ex);
        } finally {
            return art;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        chars.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws
            SAXException {
        if(qName.equals("title")) {
            art.setTitle(chars.toString());
        } else if(qName.equals("headline")) {
            art.setHeadline(chars.toString());
        } else if(qName.equals("byline")) {
            art.setByline(chars.toString());
        } else if(qName.equals("p")) {
            pars.append(chars.toString());
        } else if(qName.equals("text")) {
            art.setText(pars.toString());
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        art = null;

        //
        // Clear out the collected paragraphs from the last document.
        pars.delete(0, pars.length());
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if(qName.equals("newsitem")) {

            String key = attributes.getValue("itemid");
            if(key == null) {
                logger.warning("Article with no ID: skipping");
                return;
            }
            try {
                art = new Article(key);
            } catch(AuraException ex) {
                art = null;
                logger.log(Level.SEVERE, "Error creating article!", ex);
            }
        } else if(qName.equals("p") && pars.length() > 0) {
            //
            // Add a blank line at the start of non-first paragraphs.
            pars.append("\n\n");
        } else if(qName.equals("codes")) {

            //
            // Set up for processing article codes.
            currType = Codes.CodeType.getCodeType(attributes.getValue("class"));
            switch(currType) {
                case COUNTRIES:
                    currCodes = countries;
                    break;
                case INDUSTRIES:
                    currCodes = industries;
                    break;
                case TOPICS:
                    currCodes = topics;
                    break;
                default:
                    currCodes = null;
            }
        } else if(qName.equals("code")) {
            if(currCodes != null) {
                String code = attributes.getValue("code");
                if(code != null) {
                    String val = currCodes.getValue(code);
                    if(val != null) {
                        switch(currType) {
                            case COUNTRIES:
                                art.addCountry(val);
                                break;
                            case INDUSTRIES:
                                art.addIndustry(val);
                                break;
                            case TOPICS:
                                art.addTopic(val);
                                break;
                        }
                    }
                }
            }
        } else if(qName.equals("dc")) {
            String el = attributes.getValue("element");
            if(el.equals("dc.date.published")) {
                String val = attributes.getValue("value");
                art.setPublished(val);
            }
        }

        //
        // Clear out our character buffer.
        chars.delete(0, chars.length());
    }
}
