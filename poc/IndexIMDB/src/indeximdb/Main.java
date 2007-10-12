/*
 * Main.java
 * 
 * Created on Oct 3, 2007, 4:13:53 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package indeximdb;

import com.sun.kt.search.FieldInfo;
import com.sun.kt.search.Log;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.EnumSet;
import java.util.Properties;
import ngnova.util.Getopt;

/**
 *
 * @author ja151348
 */
public class Main {

    public static void usage() {
        System.out.println("Usage: indeximdb.Main [options]");
        System.out.println("  -i <indexDir>    Where the index lives");
        System.out.println("  -x <configFile>  The configuration file");
        System.out.println("  -l <logLevel>    Sets the log level");
        System.out.println("  -p               Pre-live mode (sends long flag to engine)");
    }

    
    /**
     * Construct a search engine and a DB connection and start indexing
     * 
     * @param args the command line arguments
     * @throws java.lang.Exception for an error
     */
    public static void main(String[] args) throws Exception {
        //
        // Set up the logging level for the log system
        int logLevel = 3;
        Log log = Log.getLog();
        log.setStream(System.out);
        
        //
        // Make sure that our DB driver is present
        Class.forName("org.postgresql.Driver");
        
        //
        // Set up to parse the command line args
        String flags = "i:x:l:p";
        Getopt gopt = new Getopt(args, flags);
        String indexDir = null;
        String configFile = null;
        boolean longRun = false;
        
        int c;
        
        //
        // Handle the options.
        while ((c = gopt.getopt()) != -1) {
            switch (c) {                
                case 'i':
                    indexDir = gopt.optArg;
                    break;
                case 'x':
                    configFile = gopt.optArg;
                    break;
                case 'l':
                    logLevel = Integer.parseInt(gopt.optArg);
                    break;
                case 'p':
                    longRun = true;
                    break;
                default:
                    usage();
                    return;
            }
        }
        log.setLevel(logLevel);
        
        //
        // Check that we got all the options that we need
        if (indexDir == null) {
            usage();
            return;
        }
        
        //
        // Get a search engine
        SearchEngine engine;
        if (configFile != null) {
            engine = SearchEngineFactory.getSearchEngine(configFile, indexDir);
        } else {
            engine = SearchEngineFactory.getSearchEngine(indexDir);
        }

        //
        // Set up the field(s) that we'll store
        
        FieldInfo title = new FieldInfo("title",
                EnumSet.of(FieldInfo.Attribute.SAVED),
                FieldInfo.Type.STRING);
        engine.defineField(title);

        //
        // Get a database connection
        String dbURL = "jdbc:postgresql://search.east.sun.com/imdb";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        
        Connection con = DriverManager.getConnection(dbURL, props);
        
        //
        // Start indexing
        Indexer idx = new Indexer(engine, con);
        idx.run();
        
        //
        // When indexing is finished, shut things down
        engine.close();
        con.close();
    }

}
