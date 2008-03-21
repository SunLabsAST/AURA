/*
 * EvaluateClassifiers.java
 *
 * Created on October 19, 2006, 8:23 AM
 *
 */
package com.sun.labs.aura.util.classifiers;

import com.sun.kt.search.Log;
import com.sun.kt.search.Result;
import com.sun.kt.search.ResultSet;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;
import ngnova.classification.ClassifierModel;
import ngnova.classification.ExplainableClassifierModel;
import ngnova.engine.SearchEngineImpl;
import ngnova.indexer.partition.DiskPartition;
import ngnova.indexer.partition.InvFileDiskPartition;
import ngnova.retrieval.ResultSetImpl;
import ngnova.util.Getopt;

/**
 * Evaluates the performance of a set of classifiers on a set of test data.
 *
 * @author Stephen Green <stephen.green@sun.com>
 */
public class EvaluateClassifiers {

    /**
     * Creates a EvaluateClassifiers
     */
    public EvaluateClassifiers() {
    }

    protected static String logTag = "EC";

    public static Set<String> getKeySet(ResultSet r) throws Exception {
        Set<String> ret = new HashSet<String>();
        List l = r.getAllResults(false);
        for(Iterator i = l.iterator(); i.hasNext();) {
            ret.add(((Result) i.next()).getKey());
        }
        return ret;
    }

    /**
     * Computes the intersection of two sets.
     */
    public static Set<String> and(Set<String> s1, Set<String> s2) {
        Set<String> ret = new HashSet<String>();
        for(String s : s1) {
            if(s2.contains(s)) {
                ret.add(s);
            }
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {

        String flags = "a:c:d:e:f:n:o:twh:q:";
        Getopt gopt = new Getopt(args, flags);

        //
        // Use the labs format logging.
        final Logger logger = Logger.getLogger("");
        for(Handler h : logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        Log.setLogger(logger);
        Log.setLevel(3);


        String logTag = "EVAL";

        //
        // Handle the options.
        int c;
        List<String> classes = new ArrayList<String>();
        List<String> ignore = new ArrayList<String>();
        String classDir = null;
        String testDir = null;
        String fieldName = null;
        String configFile = null;
        String autoClassField = null;
        String testField = "test";
        String testQuery = null;
        int top = -1;
        boolean sortBySetSize = false;
        boolean wikiOutput = false;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'a':
                    autoClassField = gopt.optArg;
                    break;
                case 'c':
                    classes.add(gopt.optArg);
                    break;
                case 'd':
                    classDir = gopt.optArg;
                    break;
                case 'e':
                    testDir = gopt.optArg;
                    break;
                case 'f':
                    fieldName = gopt.optArg;
                    break;
                case 'h':
                    testField = gopt.optArg;
                    break;
                case 'n':
                    ignore.add(gopt.optArg);
                    break;
                case 'o':
                    top = Integer.parseInt(gopt.optArg);
                    break;
                case 't':
                    sortBySetSize = true;
                    break;
                case 'w':
                    wikiOutput = true;
                    break;
                case 'q':
                    testQuery = gopt.optArg;
                    break;
            }
        }

        if(classDir == null || testDir == null || fieldName == null) {
            usage();
            return;
        }

        SearchEngine classEngine =
                SearchEngineFactory.getSearchEngine(classDir,
                "aardvark_search_engine");
        
        SearchEngine testEngine =
                SearchEngineFactory.getSearchEngine(testDir,
                "aardvark_search_engine");

        //
        // If there are no specific classes to build, we'll build them all.
        if(classes.size() == 0) {
            classes = Util.getTopClasses(classEngine, fieldName, top);
        }

        classes.removeAll(ignore);

        if(autoClassField == null) {
            autoClassField = "assigned-" + fieldName;
        }

        //
        // For each topic, we'll count the number of documents from the
        // test set that were correctly and incorrectly classified.
        String inTest = "split = " + testField;
        String inTrain = testQuery == null ? "split = train" : testQuery;

        //
        // Get the number of topics and test documents.
        int nTopics = classes.size();
        ResultSetImpl testSet = (ResultSetImpl) classEngine.search(inTest, "-score");
        ResultSetImpl trainSet =
                (ResultSetImpl) classEngine.search(inTrain, "-score");
        int nTestDocs = testSet.size();

        logger.info(nTopics + " topics, " + nTestDocs + " test docs");

        //
        // We're going to build a contingency table per class that we're
        // evaluating.
        ContingencyTable tables[] = new ContingencyTable[nTopics];
        int p = 0;

        Set<String> evaled = new HashSet<String>();

        //
        // We want to run the partitions directly.  It's an imperfect world,
        // eh?
        for(String className : classes) {

            tables[p] = new ContingencyTable();

            //
            // Get the number of training/testing documents in this topic.
            try {
            tables[p].train = classEngine.search(
                    String.format("%s = \"%s\"", fieldName, className),
                    "-score").size();
            tables[p].test = testEngine.search(
                    String.format("%s = \"%s\"", fieldName, className),
                    "-score").size();
            } catch (Exception e) {
                logger.info("Error for class: " + className);
                continue;
            }

            logger.info(className + " " + 
                    tables[p].train + " " +
                    tables[p].test);

            ClassifierModel cm =
                    ((SearchEngineImpl) classEngine).getClassifierManager().
                    getClassifier(className);
            
            if(cm == null) {
                logger.info("No classifier for " + className);
                continue;
            }

            for(DiskPartition dp : ((SearchEngineImpl) testEngine).getPM().
                    getActivePartitions()) {
                float[] f = cm.classify(dp, dp.getMaxDocumentID());
                for(int i = 0; i < f.length; i++) {
                    List<String> vals = ((InvFileDiskPartition) dp).getSavedFieldData(fieldName, i);
                    boolean inClass = vals.contains(className);
                    if(f[i] > 0) {
                        if(inClass) {
                            tables[p].a++;
                        } else {
                            tables[p].b++;
                        }
                    } else if(f[i] < 0) {
                        if(inClass) {
                            tables[p].c++;
                        }
                    }
                }
            }

            evaled.add(className);

            //
            // Remember the name of the topic.
            tables[p].name = className;
            p++;
        }

        logger.info("Evaluated " + p + " classes\n");
        ngnova.util.Util.sort(tables, 0, p, new Comparator<ContingencyTable>() {
            public int compare(ContingencyTable o1, ContingencyTable o2) {
                return (o1.test + o1.train) - (o2.test + o2.train);
            }
        });

        //
        // We may only want to print the top n...
        printTables(trainSet.size(), testSet.size(), tables, p, top, wikiOutput);

        classEngine.close();
        testEngine.close();
    }

    public static void printTables(
            int nTrain, int nTest,
            ContingencyTable[] tables,
            int p, int top, boolean wikiOutput) {

        ContingencyTable micro = new ContingencyTable();

        float tr = 0;
        float tp = 0;
        if(wikiOutput) {
            System.out.println(
                    "| *Name* | *Rank* | *nTrain* | *nTest* | *a* | *b* | *a+b* | *c* | *Recall* | *Precision* | *F1* |");
        } else {
            System.out.format(
                    "%-35s%7s%7s%7s%7s%7s%7s%7s%10s%10s%10s\n",
                    "Name", "Rank", "nTrain", "nTest", "a", "b", "a+b", "c",
                    "Recall", "Precision", "F1");
        }

        int stop = 0;
        if(top > 0) {
            stop = p - top;
        }
        int n = 0;
        if(stop < 0) {
            stop = 0;
        }

        for(int j = p - 1; j >= stop; j--) {

            //
            // Collect the data for micro averaging.
            micro.add(tables[j]);

            float recall = tables[j].recall();
            if(!Float.isNaN(recall)) {
                tr += recall;
            }
            float precision = tables[j].precision();
            if(!Float.isNaN(precision)) {
                tp += precision;
            }

            float f1 = tables[j].f1();

            System.out.format(
                    wikiOutput ? "|%-35s|%7d|%7d|%7d|%7d|%7d|%7d|%7d|%10.1f|%10.1f|%10.1f|\n"
                    : "%-35s%7d%7d%7d%7d%7d%7d%7d%10.1f%10.1f%10.1f\n",
                    tables[j].name,
                    n + 1,
                    tables[j].train,
                    tables[j].test,
                    tables[j].a,
                    tables[j].b,
                    tables[j].a + tables[j].b,
                    tables[j].c,
                    Float.isNaN(recall) ? 0 : recall * 100,
                    Float.isNaN(precision) ? 0 : precision * 100,
                    Float.isNaN(f1) ? 0 : f1 * 100);

            n++;
        }

        System.out.format(
                wikiOutput ? "|%-35s|%7d|%7d|%7d|%7d|%7d|%7d|%7d|%10.1f|%10.1f|%10.1f|\n"
                : "%-35s%7d%7d%7d%7d%7d%7d%7d%10.1f%10.1f%10.1f\n",
                "Micro Average",
                n + 1,
                nTrain,
                nTest,
                micro.a,
                micro.b,
                0,
                micro.c,
                micro.recall() * 100,
                micro.precision() * 100,
                micro.f1() * 100);

        tr /= n;
        tp /= n;
        System.out.println();
        if(wikiOutput) {
            System.out.format("%-40s%10.1f\n", "   * Macro Average Recall: ",
                    tr * 100);
            System.out.format("%-40s%10.1f\n", "   * Macro Average Precision: ",
                    tp * 100);

        } else {
            System.out.format("%-40s%10.1f\n", "Macro Average Recall: ", tr *
                    100);
            System.out.format("%-40s%10.1f\n", "Macro Average Precision: ", tp *
                    100);
        }
    }

    private static void usage() {
        System.err.println("Usage:  com.sun.labs.rsi.EvaluateClassifiers -d <indexDir> " +
                "-f <field Name> [-c class]...");
    }
}
