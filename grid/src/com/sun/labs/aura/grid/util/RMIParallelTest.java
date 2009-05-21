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

package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ReverseScoredComparator;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 *
 */
public class RMIParallelTest extends ServiceAdapter {

    @ConfigInteger(defaultValue = 30)
    public static final String PROP_RUNS = "runs";

    protected int runs;

    @ConfigInteger(defaultValue = 16)
    public static final String PROP_NUM_REPS = "numReps";

    protected int numReps;

    @ConfigString(defaultValue = "artist-tag:metal")
    public static final String PROP_INITIAL_KEY = "initialKey";

    private String initialKey;

    @ConfigString(defaultValue = "taggedArtists")
    public static final String PROP_FIELD = "field";

    private String field;

    @ConfigInteger(defaultValue=0)
    public static final String PROP_PAUSE = "pause";

    private int pause;

    protected Map<String, List<Scored<String>>> m;

    private int prefixLen;

    protected Map<Replicant, List<Scored<String>>> repMap;

    protected DataStore ds;

    protected NanoWatch fsTime = new NanoWatch();

    protected NanoWatch getTime = new NanoWatch();

    protected ExecutorService executor;

    protected DocumentVector dv;

    protected String name;

    //
    // Set up the data.
    public RMIParallelTest() {
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public String serviceName() {
        return "RMITest";
    }

    protected void getData() throws AuraException, RemoteException {
        m = new HashMap();

        //
        // Fill in the map from prefixes to keys to get.
        for(Scored<Item> item : ds.findSimilar(initialKey, new SimilarityConfig(
                field, 100))) {
            DSBitSet bs = DSBitSet.parse(item.getItem().getKey().hashCode());
            bs.setPrefixLength(prefixLen);
            String prefix = bs.toString();
            List<Scored<String>> l = m.get(prefix);
            if(l == null) {
                l = new ArrayList();
                m.put(prefix, l);
            }
            l.add(new Scored<String>(item.getItem().getKey(), item.getScore()));
        }
    }

    protected List<Scored<String>> runFindSimilar() {
        return runFindSimilar(false);
    }

    protected List<Scored<String>> runFindSimilar(boolean getSizes) {
        fsTime.start();
        List<FindSimilarer> callers = new ArrayList();
        for(Replicant r : repMap.keySet()) {
            callers.add(new FindSimilarer(r, dv));
            if(callers.size() >= numReps) {
                break;
            }
        }
        List<Scored<String>> ret = new ArrayList();
        try {
            List<Future<List<Scored<String>>>> futures = executor.invokeAll(
                    callers);
            for(Future<List<Scored<String>>> f : futures) {
                ret.addAll(f.get());
            }
            fsTime.stop();
            logger.info(String.format(" fs took %.3f",
                    fsTime.getLastTimeMillis()));
            double max = 0;
            double maxRep = 0;
            double maxOH = 0;
            StringBuilder sb = new StringBuilder();
            for(FindSimilarer fs : callers) {
                sb.append(String.format(" %s took %.3f oh %.3f",
                        fs.prefix, fs.nw.getTimeMillis(), fs.overhead));
                max = Math.max(max, fs.nw.getTimeMillis());
                maxRep = Math.max(maxRep, fs.repTime);
                maxOH = Math.max(maxOH, fs.overhead);
                if(getSizes) {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(fs.result);
                        oos.flush();
                        logger.info(String.format(
                                "%s data size for fs (%d results) approx %d",
                                fs.prefix, fs.result.size(), bos.size()));
                    } catch(java.io.IOException ex) {
                    }
                }
            }
            logger.info(sb.toString());
            logger.info(String.format(" max: %.3f maxRep: %.3f maxOH: %.3f %.3f",
                    max, maxRep, maxOH, fsTime.getLastTimeMillis() - maxRep));
            return ret;
        } catch(InterruptedException ie) {
            logger.log(Level.SEVERE, "Interrupted exception", ie);
            return null;
        } catch(ExecutionException ex) {
            logger.log(Level.SEVERE, "Execution exception", ex);
            return null;
        }
    }

    protected List<Scored<Item>> runGet() {
        return runGet(false);
    }

    protected List<Scored<Item>> runGet(boolean getSizes) {
        getTime.start();
        List<Getter> callers = new ArrayList();
        int currPause = 0;
        for(Map.Entry<Replicant, List<Scored<String>>> e : repMap.entrySet()) {
            Getter g = new Getter(e.getKey(), e.getValue());
            if(pause > 0) {
                g.setPause(currPause);
                currPause += pause;
            }
            callers.add(g);
            if(callers.size() >= numReps) {
                break;
            }
        }
        List<Scored<Item>> ret = new ArrayList();
        try {
            List<Future<List<Scored<Item>>>> futures = executor.invokeAll(
                    callers);
            for(Future<List<Scored<Item>>> f : futures) {
                ret.addAll(f.get());
            }
            getTime.stop();
            logger.info(String.format(" get took %.3f",
                    getTime.getLastTimeMillis()));
            double max = 0;
            double maxRep = 0;
            double maxOH = 0;
            StringBuilder sb = new StringBuilder();
            for(Getter g : callers) {
                sb.append(String.format(" %s took %.3f oh %.3f",
                        g.prefix, g.nw.getTimeMillis(), g.overhead));
                max = Math.max(max, g.nw.getTimeMillis());
                maxRep = Math.max(maxRep, g.repTime);
                maxOH = Math.max(maxOH, g.overhead);
                if(getSizes) {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(g.result);
                        oos.flush();
                        logger.info(String.format(
                                "%s data size for get (%d items) approx %d",
                                g.prefix, g.result.size(), bos.size()));
                    } catch(java.io.IOException ex) {
                    }
                }
            }
            logger.info(sb.toString());
            logger.info(String.format(" max: %.3f maxRep: %.3f maxOH: %.3f %.3f",
                    max, maxRep, maxOH, getTime.getLastTimeMillis() - maxRep));
            return ret;
        } catch(InterruptedException ie) {
            logger.log(Level.SEVERE, "Interrupted exception", ie);
            return null;
        } catch(ExecutionException ex) {
            logger.log(Level.SEVERE, "Execution exception", ex);
            return null;
        }
    }

    public void start() {
        try {
            logger.info("Running: " + name);
            runFindSimilar(true);
            runGet(true);
            for(int i = 0; i < runs; i++) {
                logger.info("Get Run " + (i + 1));
                List<Scored<Item>> items = runGet();
            }
            for(int i = 0; i < runs; i++) {
                logger.info("FindSimilar Run " + (i + 1));
                List<Scored<String>> keys = runFindSimilar();
            }
            logger.info(String.format("Average fs time: %.3fms",
                    fsTime.getAvgTimeMillis()));
            logger.info(String.format("Average get time: %.3fms",
                    getTime.getAvgTimeMillis()));
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Exception during runs", ex);
        }
    }

    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        initialKey = ps.getString(PROP_INITIAL_KEY);
        field = ps.getString(PROP_FIELD);
        runs = ps.getInt(PROP_RUNS);
        numReps = ps.getInt(PROP_NUM_REPS);
        pause = ps.getInt(PROP_PAUSE);
        name = ps.getInstanceName();
        ds = (DataStore) cm.lookup(com.sun.labs.aura.datastore.DataStore.class,
                null);

        logger.info(String.format("DataStore: %s", ds));

        //
        // Make the map from replicants to the data to look up.
        repMap = new HashMap();
        List<Component> reps = cm.lookupAll(
                com.sun.labs.aura.datastore.impl.Replicant.class, null);
        prefixLen = (int) (Math.log(reps.size()) / Math.log(2));

        //
        // Get the similarity data.
        try {
            getData();
        } catch(Exception ex) {
            throw new PropertyException(ex, ps.getInstanceName(), "dataStore",
                    "Error getting tag data");
        }

        logger.info("prefix length: " + prefixLen);
        SimilarityConfig config = new SimilarityConfig(field, 100);
        for(Component c : reps) {
            Replicant rep = (Replicant) c;
            try {
                DocumentVector rdv = rep.getDocumentVector(initialKey, config).get();
                if(rdv != null) {
                    logger.info(String.format(
                            "Got document vector for %s from %s",
                            initialKey, rep.getPrefix().toString()));
                    dv = rdv;
                }
                String prefix = rep.getPrefix().toString();
                List l = m.get(prefix);
                repMap.put(rep, m.get(prefix));
            } catch(Exception ex) {
                throw new PropertyException(ex, ps.getInstanceName(), "REPS",
                        "Error getting replicants");
            }
        }
    }

    class CallerBase {
        protected Replicant r;

        protected String prefix;

        protected NanoWatch nw = new NanoWatch();

        protected double overhead;

        protected double repTime;

        protected int pause;

        public void setPause(int pause) {
            this.pause = pause;
        }
        
    }

    class Getter extends CallerBase implements Callable<List<Scored<Item>>> {

        protected List<Scored<String>> keys;

        protected List<Scored<Item>> result;

        public Getter(Replicant r, List<Scored<String>> keys) {
            this.r = r;
            this.keys = keys;
            try {
                prefix = r.getPrefix().toString();
            } catch(RemoteException rx) {
                logger.severe("Error getting prefix");
            }
        }

        public List<Scored<Item>> call() throws Exception {
            nw.start();
            if(pause > 0) {
                try {
                    Thread.sleep(pause);
                } catch (InterruptedException ie) {
                }
            }
            result = r.getScoredItems(keys);
            nw.stop();
            repTime = result.size() > 0 ? result.get(0).time : 0;
            overhead = nw.getTimeMillis() - repTime;
            return result;
        }
    }

    class FindSimilarer  extends CallerBase implements Callable<List<Scored<String>>> {

        private DocumentVector dv;

        private SimilarityConfig config;

        protected List<Scored<String>> result;

        public FindSimilarer(Replicant r, DocumentVector dv) {
            this.r = r;
            this.dv = dv;
            try {
                prefix = r.getPrefix().toString();
            } catch(RemoteException rx) {
                logger.severe("Error getting prefix");
            }
            config = new SimilarityConfig("taggedArtists", 100);
        }

        @Override
        public List<Scored<String>> call() throws Exception {
            nw.start();
            result = r.findSimilar(new MarshalledObject<DocumentVector>(dv), new MarshalledObject<SimilarityConfig>(config)).get();
            nw.stop();
            repTime = result.size() > 0 ? result.get(0).time : 0;
            overhead = nw.getTimeMillis() - repTime;
            return result;
        }
    }
}
