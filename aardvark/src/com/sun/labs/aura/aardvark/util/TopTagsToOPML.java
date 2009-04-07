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

package com.sun.labs.aura.aardvark.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Take technorati top tags listings and turn them into an OPML file.
 */
public class TopTagsToOPML {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("tags.opml"), "utf-8"));

        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<opml version=\"1.1\">");
        pw.println("<head>");
        pw.println("<title>OPML Search for politics</title>");
        pw.println("<dateCreated>11/15/2007 9:15:55 AM</dateCreated>");
        pw.println("</head>");
        pw.println("<body>");
        pw.println("<outline text=\"OPML for top tags\">");


        int n = 0;
        for(int i = 0; i < args.length; i++) {
            DocumentBuilder b = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder();
            URL mlu = (new File(args[i])).toURI().toURL();
            Document d = b.parse(mlu.openStream());
            NodeList l = d.getElementsByTagName("tag");
            tagLoop: for(int j = 0; j < l.getLength(); j++) {
                String tag = l.item(j).getTextContent();
                for(int k = 0; k < tag.length(); k++) {
                    int ci = (int) tag.charAt(k);
                    if(ci > 256) {
                        continue tagLoop;
                    }
                }
                pw.printf("<outline title=\"%s Tag\" text=\"%s Tag\" htmlUrl=\"http://www.technorati.com/tag/%s\" " +
                        "xmlUrl=\"http://feeds.technorati.com/tag/%s\" type=\"rss\"/>\n",
                        tag, tag, tag, tag);
                n++;
            }
        }
        pw.println("</outline>");
        pw.println("</body>");
        pw.println("</opml>");
        pw.close();
        System.out.println("Wrote " + n + " tags");
    }
}
