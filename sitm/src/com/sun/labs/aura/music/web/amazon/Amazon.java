/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.web.amazon;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.Utilities;
import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class Amazon {
    private Commander amazonCommander;
    private final static List<String> EMPTY = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        String artist = "Weezer";
        String title = "Weezer";
        int pages = 5;

        if (args.length >= 1) {
            artist = args[0];
        }

        if (args.length >= 2) {
            title = args[1];
        }

        if (args.length >= 3) {
            pages = Integer.parseInt(args[2]);
        }

        Amazon amazon = new Amazon();
        System.out.println("Artist " + artist);
        System.out.println("Title " + title);
        System.out.println("Pages " + pages);
        List<String> reviews = amazon.lookupReviews(artist, title, pages);
        int count = 1;
        for (String s : reviews) {
            System.out.printf("================== Review %d ===================\n", count++);
            System.out.println(Utilities.detag(s));
            System.out.println();
        }
    }

    /** Creates a new instance of Amazon */
    public Amazon() throws IOException {
        amazonCommander = new Commander("amazon",
                "http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&SubscriptionId=1DADQWB5CC0NCDBK0X02", "");
        amazonCommander.setMinimumCommandPeriod(1000L);

    }

    /**
     * looks up reviews for the given release
     * @param asin the amazon id
     * @param maxRequests number of pages of reviews to retrieve, (note that it takes 1 second
     * to receive each page of reviews)
     * @return a  list of the reviews
     */
    // http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&Operation=ItemLookup&IdType=ASIN&ItemId=B000JK8OYU&SubscriptionId=1DADQWB5CC0NCDBK0X02&ResponseGroup=Review&ReviewPage=1
    public List<String> lookupReviews(String asin, int maxRequests)  throws IOException{
        String request = null;
        List<String> reviews = new ArrayList<String>();
        int maxPages = 1;
        int page = 1;


        // Get the editorial reviews
        while (page <= maxPages && page <= maxRequests) {

            if (page == 1) {
                request = "EditorialReview,Reviews";
            } else {
                request = "Reviews&ReviewPage=" + page;
            }

            Document doc = amazonCommander.sendCommand("&Operation=ItemLookup&IdType=ASIN&ResponseGroup=" + request + "&ItemId=" + asin);
            Element docElement = doc.getDocumentElement();
            String totalPagesString = XmlUtil.getElementContents(docElement, "TotalReviewPages");
            if (totalPagesString != null) {
                maxPages = Integer.parseInt(totalPagesString);
            }

            NodeList contentList = docElement.getElementsByTagName("Content");
            for (int i = 0; i < contentList.getLength(); i++) {
                Element content = (Element) contentList.item(i);
                reviews.add(content.getTextContent());
            }

            page++;
        }
        return reviews;
    }

    
    //http://webservices.amazon.com/onca/xml?Service=AWSECommerceService&Operation=ItemSearch&SearchIndex=Music&Artist=beatles&Title=revolver&SubscriptionId=1DADQWB5CC0NCDBK0X02&ResponseGroup=Small&Sort=salesrank
    //&Operation=ItemSearch&SearchIndex=Music&Artist=beatles&Title=revolver&ResponseGroup=Small&Sort=salesrank

    /**
     * Looks up an amazon asin for an artist/album
     * @param artist the artist
     * @param album the album
     * @return the asin (or null)
     */
    public String lookupASIN(String artist, String album) throws IOException {
        artist = URLEncoder.encode(artist, "UTF-8");
        album = URLEncoder.encode(album, "UTF-8");
        String query = "&Operation=ItemSearch&SearchIndex=Music&Artist=" + artist +
                       "&Title=" + album + "&ResponseGroup=Small&Sort=salesrank";
        Document doc = amazonCommander.sendCommand(query);
        Element docElement = doc.getDocumentElement();
        NodeList contentList = docElement.getElementsByTagName("ASIN");
        if (contentList.getLength() > 0) {
            return (String) contentList.item(0).getTextContent();
        }
        return null;
    }

    /**
     * looks up reviews for an artist / album
     * @param artist the artist
     * @param album the album
     * @param maxRequests number of pages of reviews to retrieve, (note that it takes 1 second
     * to receive each page of reviews)
     * @return a  list of the reviews
     */
    public List<String> lookupReviews(String album, String artist, int maxRequests) throws IOException {
        String asin = lookupASIN(album, artist);
        if (asin != null) {
            return lookupReviews(asin, maxRequests);
        } else {
            return EMPTY;
        }
    }
}

