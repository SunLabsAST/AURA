/*
 * BuildClassifiers.java
 *
 * Created on October 18, 2006, 3:35 PM
 */
package com.sun.labs.aura.util.classifiers;

import com.sun.kt.search.Log;
import com.sun.kt.search.Progress;
import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultSet;
import com.sun.kt.search.ResultsFilter;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import ngnova.pipeline.StopWords;
import ngnova.util.Getopt;
import ngnova.util.StopWatch;

/**
 * A class to build search engine classifiers for the aardvark data.
 */
public class BuildClassifiers implements Runnable {
    
    private static Logger logger;
    
    private List<String> classes;
    
    private SearchEngine engine;
    
    private String vectoredField;
    
    private String assignedField;
    
    private String fieldName;
    
    private ResultsFilter lengthFilter;
    
    private StopWords stop;
    
    public BuildClassifiers(List<String> classes, 
            String vectoredField,
            String assignedField,
            String fieldName,
            SearchEngine e,
            ResultsFilter lengthFilter,
            StopWords stop) {
        this.classes = classes;
        this.engine = e;
        this.vectoredField = vectoredField;
        this.assignedField = assignedField;
        this.fieldName = fieldName;
        this.lengthFilter = lengthFilter;
        this.stop = stop;
    }
    
    public void run() {
        //
        // Now, build a class for each topic.
        logger.info(Thread.currentThread().getName() + " training " + classes.size() + " classifiers");
        StopWatch sw = new StopWatch();
        for(String className : classes) {
            if(stop.isStop(className)) {
                logger.info("Ignoring class: " + className);
                continue;
            }
            sw.reset();
            sw.start();
            try {
                String query =
                        String.format("aura-type = blogentry <and> %s = \"%s\"",
                        fieldName, className);
                ResultSet r = engine.search(query, "-score");
                r.setResultsFilter(lengthFilter);
                if(r.size() > 0) {
                    logger.info(Thread.currentThread().getName() + 
                            " training " + className + ", " + r.size() +
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
                    logger.info(Thread.currentThread().getName() + 
                            " trained " + className + ", " +
                            r.size() + " examples, " +
                            sw.getTime() + "ms");
                } else {
                    logger.info("No training examples for " + className);
                }
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Exception", e);

            }
        }
        
    }

    public static void main(String[] args) throws Exception {

        String flags = "a:c:d:e:k:f:r:s:t:v:";
        Getopt gopt = new Getopt(args, flags);

        //
        // Use the labs format logging.
        logger = Logger.getLogger("");
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
        int numChars = 200;
        int numThreads = 1;
        StopWords sw = new StopWords();

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
                case 'k':
                    numChars = Integer.parseInt(gopt.optArg);
                    break;
                case 'r':
                    numThreads = Integer.parseInt(gopt.optArg);
                    break;
                case 's':
                    sw.addFile(gopt.optArg);
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
        // A results filter for content longer than 200 chars.
        final String fooField = vectoredField;
        final int fooChars = numChars;
        ResultsFilter lengthFilter = new ResultsFilter() {
            public boolean filter(ResultAccessor ra) {
                String v = (String) ra.getSingleFieldValue(fooField);
                return v != null && v.toString().length() >= fooChars;
            }
        };
        
        List<Thread> lt = new ArrayList<Thread>();
        int size = classes.size() / numThreads;
        if(classes.size() % numThreads != 0) {
            size++;
        }
        for(int i = 0, start = 0; i < numThreads; i++, start += size) {
            BuildClassifiers bc = new BuildClassifiers(classes.subList(start, Math.min(start+size, classes.size())), 
                    vectoredField, assignedField, fieldName, engine, lengthFilter, sw);
            Thread t = new Thread(bc);
            t.setName("BC-" + i);
            t.start();
            lt.add(t);
        }
        
        for(Thread t : lt) {
            try {
                t.join();
            } catch (InterruptedException ie) {
                
            }
        }

        //
        // Make sure our classifiers are flushed out.
        engine.close();
    }

    private static void usage() {
        System.err.println("Usage:  com.sun.labs.aura.util.classifiers.BuildClassifiers -d <indexDir> " +
                "-f <field Name> -a <assign to field> -v <vectored field> [-c class]...");
    }
}
