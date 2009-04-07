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

package com.sun.labs.aura.util.classifiers;

import com.sun.labs.minion.Log;
import com.sun.labs.minion.Result;
import com.sun.labs.minion.SearchEngine;
import com.sun.labs.minion.SearchEngineFactory;
import com.sun.labs.minion.classification.ClassifierModel;
import com.sun.labs.minion.classification.ExplainableClassifierModel;
import com.sun.labs.minion.engine.SearchEngineImpl;
import com.sun.labs.minion.util.Getopt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author stgreen
 */
public class DescribeClassifiers {
    
    protected double score;
    
    protected Result r;
    
    public DescribeClassifiers(Result r, double score) {
        this.r = r;
        this.score = score;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String flags 	 = "c:d:f:n:o:";
        Getopt gopt 	 = new Getopt(args, flags);
        
        //
        // Set up the logging for the search engine.  We'll send everything
        // to the standard output, except for errors, which will go to
        // standard error.  We'll set the level at 3, which is pretty
        // verbose.
        Log log = Log.getLog();
        log.setStream(System.err);
        log.setLevel(3);
        
        String logTag = "DC";
        
        //
        // Handle the options.
        int c;
        List<String> classes = new ArrayList<String>();
        List<String> ignore = new ArrayList<String>();
        String indexDir = null;
        String fieldName = null;
        int topClasses = -1;
        while ((c = gopt.getopt()) != -1) {
            switch (c) {
                case 'c':
                    classes.add(gopt.optArg);
                    break;
                case 'd':
                    indexDir = gopt.optArg;
                    break;
                case 'f':
                    fieldName = gopt.optArg;
                    break;
                case 'n':
                    ignore.add(gopt.optArg);
                    break;
                case 'o':
                    topClasses = Integer.parseInt(gopt.optArg);
                    break;
            }
        }
        
        if(indexDir == null || fieldName == null) {
            usage();
            return;
        }
        
        SearchEngine engine =
                SearchEngineFactory.getSearchEngine(indexDir,
                "aardvark_search_engine");
        
        //
        // See whether we want the top n classes.
        if(topClasses > 0) {
            classes = Util.getTopClasses(engine, fieldName, topClasses);
        }
        
        //
        // If there are no specific classes to build, we'll build them all.
        if(classes.size() == 0) {
            for(Iterator i = engine.getFieldIterator(fieldName); i.hasNext(); ) {
                classes.add((String) i.next());
            }
        }
        
        classes.removeAll(ignore);
        
        System.out.println("---+!! Classifier descriptions for " + fieldName);
        System.out.println("\n%TOC%\n");
        
        for(String className : classes) {
            
            ClassifierModel cm =
                    ((SearchEngineImpl) engine).getClassifierManager().getClassifier(className);
            
            ExplainableClassifierModel ecm = (ExplainableClassifierModel) cm;
            if(ecm == null) {
                log.log(logTag, 0, "No classifier for: " + className);
                continue;
            }
            
            System.out.println("---++ Classifier " + className);
            System.out.println(ecm.describe());
            
        }
        engine.close();
    }
    
    private static void usage() {
        System.err.println("Usage:  com.sun.labs.rsi.EvaluateClassifiers -d <indexDir> " +
                "-f <field Name> [-c class]...");
    }
    
}
