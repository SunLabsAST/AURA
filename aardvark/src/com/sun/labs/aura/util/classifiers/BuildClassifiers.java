/*
 * BuildClassifiers.java
 *
 * Created on October 18, 2006, 3:35 PM
 */
package com.sun.labs.aura.util.classifiers;

import com.sun.kt.search.FieldFrequency;
import com.sun.kt.search.Log;
import com.sun.kt.search.Progress;
import com.sun.kt.search.ResultSet;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;
import ngnova.util.Getopt;
import ngnova.util.StopWatch;

/**
 * A class to build search engine classifiers for the aardvark data.
 */
public class BuildClassifiers {

    public static void main(String[] args) throws Exception {

        String flags = "a:c:d:e:f:t:v:";
        Getopt gopt = new Getopt(args, flags);

        //
        // Use the labs format logging.
        final Logger logger = Logger.getLogger("");
        for(Handler h : logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        Log.setLogger(logger);
        Log.setLevel(3);

        String logTag = "BUILD";

        //
        // Handle the options.
        int c;
        List<String> classes = new ArrayList<String>();
        String indexDir = null;
        String fieldName = null;
        String assignedField = null;
        String vectoredField = null;
        String engineName = "aardvark_search_engine";

        //
        // The number of top classes to build.
        int top = 100;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'a':
                    assignedField = gopt.optArg;
                    break;
                case 'c':
                    classes.add(gopt.optArg);
                    break;
                case 'd':
                    indexDir = gopt.optArg;
                    break;
                case 'e':
                    engineName = gopt.optArg;
                    break;
                case 'f':
                    fieldName = gopt.optArg;
                    break;
                case 't':
                    top = Integer.parseInt(gopt.optArg);
                    break;
                case 'v':
                    vectoredField = gopt.optArg;
                    break;
            }
        }

        if(indexDir == null || fieldName == null) {
            usage();
            return;
        }

        SearchEngine engine = SearchEngineFactory.getSearchEngine(indexDir,
                engineName);

        //
        // If there are no specific classes to build, we'll build the most frequent.
        if(classes.size() == 0) {
            classes = Util.getTopClasses(engine, fieldName, top);
        }

        //
        // Check for the field name to which we'll assign classification results.
        if(assignedField == null) {
            assignedField = "assigned-" + fieldName;
        }

        //
        // Now, build a class for each topic.
        logger.info("Training " + classes.size() + " classifiers");
        StopWatch sw = new StopWatch();
        for(String className : classes) {
            sw.reset();
            sw.start();
            try {
                String query =
                        String.format("aura-type = blogentry <and> %s = \"%s\"",
                        fieldName, className);
                ResultSet r = engine.search(query, "-score");
                if(r.size() > 0) {
                    logger.info(" Training " + className + ", " + r.size() +
                            " examples");
                    Progress p = new Progress() {

                        public void start(int steps) {
                        }

                        public void next(String s) {
                            logger.fine("Progress: " + s);
                        }

                        public void next() {
                        }

                        public void done() {
                        }
                    };
                    engine.trainClass(r, className, assignedField, vectoredField,
                            p);
                    sw.stop();
                    logger.info(" Trained " + className + ", " +
                            r.size() + " examples, " +
                            sw.getTime() + "ms");
                }
            } catch(Exception e) {

            }
        }

        //
        // Make sure our classifiers are flushed out.
        // engine.dumpClassifiers();
        engine.close();
    }

    private static void usage() {
        System.err.println("Usage:  com.sun.labs.aura.util.classifiers.BuildClassifiers -d <indexDir> " +
                "-f <field Name> -a <assign to field> -v <vectored field> [-c class]...");
    }
}
