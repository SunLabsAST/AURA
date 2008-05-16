/*
 * BuildClassifiers.java
 *
 * Created on October 18, 2006, 3:35 PM
 */
package com.sun.labs.aura.util.classifiers;

import com.sun.labs.minion.Log;
import com.sun.labs.minion.Progress;
import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.minion.ResultSet;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.SearchEngine;
import com.sun.labs.minion.SearchEngineFactory;
import com.sun.labs.minion.pipeline.StopWords;
import com.sun.labs.minion.util.Getopt;
import com.sun.labs.minion.util.StopWatch;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    public BuildClassifiers(List<String> classes, 
            String vectoredField,
            String assignedField,
            String fieldName,
            SearchEngine e,
            ResultsFilter lengthFilter) {
        this.classes = classes;
        this.engine = e;
        this.vectoredField = vectoredField;
        this.assignedField = assignedField;
        this.fieldName = fieldName;
        this.lengthFilter = lengthFilter;
    }
    
    public void run() {
        //
        // Now, build a class for each topic.
        logger.info(Thread.currentThread().getName() + " training " + classes.size() + " classifiers");
        StopWatch sw = new StopWatch();
        for(String className : classes) {
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

        String flags = "a:c:d:e:k:f:g:r:s:t:v:";
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
        Set<String> classes = new LinkedHashSet<String>();
        String indexDir = null;
        String fieldName = "tag";
        String assignedField = "autotag";
        String vectoredField = "content";
        String engineName = "aardvark_search_engine";
        int numChars = 200;
        int numThreads = 1;
        StopWords sw = new StopWords();
        sw.addFile("/com/sun/labs/aura/aardvark/resource/stopwords");
        int top = 500;
        String tagFile = null;
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
                case 'g':
                    tagFile = gopt.optArg;
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
        // Are there tags in a file?
        if(tagFile != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile), "utf-8"));
            String l;
            while((l = r.readLine()) != null) {
                classes.add(l);
            }
            r.close();
        }
        
        //
        // Do we want the most frequent?
        if(top > 0) {
            classes.addAll(Util.getTopClasses(engine, fieldName, top));
        }
        
        //
        // Throw out the weird ones.
        for(Iterator<String> i = classes.iterator(); i.hasNext(); ) {
            String cn = i.next();
            if(sw.isStop(cn)) {
                logger.info("Ignoring class named: " + cn);
                i.remove();
            }
        }

        //
        // A results filter for content longer than 200 chars.
        final String fooField = vectoredField;
        final int fooChars = numChars;
        ResultsFilter lengthFilter = new ResultsFilter() {
            private int nt;
            private int np;
            public boolean filter(ResultAccessor ra) {
                nt++;
                String v = (String) ra.getSingleFieldValue(fooField);
                boolean ret = v != null && v.toString().length() >= fooChars;
                if(ret) {
                    np++;
                }
                return ret;
            }

            public int getTested() {
                return nt;
            }

            public int getPassed() {
                return np;
            }
        };
        
        List<Thread> lt = new ArrayList<Thread>();
        
        //
        // Round robin the list so that the first thread doesn't get stuck 
        // with all the big ones.
        List[] tcl = new List[numThreads];
        for(int i = 0; i < numThreads; i++) {
            tcl[i] = new ArrayList<String>();
        }
        List<String> cl = new ArrayList<String>(classes);
        for(int i = 0; i < classes.size(); i++) {
            tcl[i % numThreads].add(cl.get(i));
        }
        for(int i = 0; i < numThreads; i++) {
            BuildClassifiers bc = 
                    new BuildClassifiers((List<String>) tcl[i], 
                    vectoredField, assignedField, fieldName, engine, lengthFilter);
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
