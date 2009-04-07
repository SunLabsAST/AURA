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

package indeximdb;

import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SimpleIndexer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import ngnova.util.StopWatch;

/**
 *
 * @author ja151348
 */
public class Indexer {
    private SearchEngine engine;
    
    private Connection dbCon;
    
    protected static DecimalFormat form = new DecimalFormat("########0.00");
    
    public Indexer(SearchEngine e,
                   Connection dbCon) {
        engine = e;
        this.dbCon = dbCon;
    }
    
    public void run() throws Exception {
        //
        // We'll want to use a cursor to navigate the results.  Otherwise,
        // the client will try to cache the 13 million or so rows returned.
        // For postgres, we must turn off autocommit to allow cursors
        dbCon.setAutoCommit(false);
        
        Statement st = dbCon.createStatement();
        st.setFetchSize(100);
        System.out.println("Executing query to fetch title/actor data");
        StopWatch sw = new StopWatch();
        sw.start();
        ResultSet rs =
                st.executeQuery("SELECT title.id AS movID,name.name AS actName,title.title AS movTitle " +
                                "FROM name,title,cast_info " +
                                "WHERE cast_info.person_id=name.id " +
                                "AND cast_info.movie_id=title.id " +
                                "ORDER BY title");
        sw.stop();
        System.out.println("Query took " + (sw.getTime() / 1000) + "s");
        
        //
        // Keep track of our progress
        int numTitles = 0;
        int totalTitles = 0;
        
        //
        // Do the processing...
        SimpleIndexer indexer = engine.getSimpleIndexer();
        String prevTitle = "";
        while (rs.next()) {
            String currTitle = rs.getString("movTitle");
            if (!currTitle.equals(prevTitle)) {
                //
                //  If there is an existing document to finish, index it.
                if (!prevTitle.equals("")) {
                    indexer.endDocument();
                }
                
                
                //
                // Handle our progress tracker
                if (++numTitles >= 1000) {
                    sw.stop();
                    totalTitles += numTitles;
                    float tps = (numTitles / (sw.getTime() * (float)1000));
                    System.out.println("Processed " + totalTitles + " titles; "
                            + "Current speed: " + form.format(tps)
                            + " titles/sec");
                    numTitles = 0;
                    sw.reset();
                    sw.start();
                }

                
                //
                // Start a new document for this title
                indexer.startDocument(rs.getString("movID"));
                indexer.addField("title-id", rs.getInt("movID"));
                indexer.addField("title", currTitle);
                prevTitle = currTitle;
            }
            //
            // Add the current cast member to the current document
            indexer.addTerm(rs.getString("actName"));
        }
        
        //
        // Close the last document
        if (!prevTitle.equals("")) {
            indexer.endDocument();
        }
        
        //
        // Close the result set and statement
        rs.close();
        st.close();
    }
}
