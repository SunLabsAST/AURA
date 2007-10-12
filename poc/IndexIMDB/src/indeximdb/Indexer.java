/*
 * Indexer.java
 * 
 * Created on Oct 3, 2007, 4:42:36 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indeximdb;

import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SimpleIndexer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import ngnova.util.StopWatch;

/**
 *
 * @author ja151348
 */
public class Indexer {
    private SearchEngine engine;
    
    private Connection dbCon;
    
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
                            + "Current speed: " + tps + " titles/sec");
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
