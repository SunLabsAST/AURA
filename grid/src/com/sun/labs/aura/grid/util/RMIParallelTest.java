/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
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
    public static final String PROP_INITIAL_TAG = "initialTag";

    private String initialTag;

    protected Map<String, List<Scored<String>>> m;

    private int prefixLen;

    protected Map<Replicant, List<Scored<String>>> repMap;

    protected DataStore ds;

    protected double totalTime;

    protected ExecutorService executor;

    //
    // Set up the data.
    public RMIParallelTest() {
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public String serviceName() {
        return "RMITest";
    }

    private void display(List<Scored<Item>> l) {
        if(l == null) {
            logger.info("No list!");
            return;
        }
        logger.info("Got " + l.size() + " items");
    }

    protected void getData() throws AuraException, RemoteException {
        m = new HashMap();
        SimilarityConfig config = new SimilarityConfig("taggedArtists", 100);
        for(Scored<Item> item : ds.findSimilar(initialTag, config)) {
            DSBitSet bs = DSBitSet.parse(item.getItem().getKey().hashCode());
            bs.setPrefixLength(prefixLen);
            String prefix = bs.toString();
            logger.info(String.format("key %s prefix %s", item.getItem().getKey(), prefix));
            List<Scored<String>> l = m.get(prefix);
            if(l == null) {
                l = new ArrayList();
                m.put(prefix, l);
            }
            l.add(new Scored<String>(item.getItem().getKey(), item.getScore()));
        }
    }

    protected List<Scored<Item>> runGet() {
        NanoWatch nw = new NanoWatch();
        nw.start();
        List<Callable<List<Scored<Item>>>> callers = new ArrayList();
        for(Map.Entry<Replicant, List<Scored<String>>> e : repMap.entrySet()) {
            callers.add(new Getter(e.getKey(), e.getValue()));
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
            Collections.sort(ret, ReverseScoredComparator.COMPARATOR);
            nw.stop();
            logger.info(String.format("Parallel get items took %.3f", nw.
                    getTimeMillis()));
            totalTime += nw.getTimeMillis();
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
            for(int i = 0; i < runs; i++) {
                logger.info("Run " + (i + 1));
                List<Scored<Item>> items = runGet();
                display(items);
            }
            logger.info(String.format("Average time to get items: %.3f", totalTime /
                    runs));
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Exception during runs", ex);
        }
    }

    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        initialTag = ps.getString(PROP_INITIAL_TAG);
        runs = ps.getInt(PROP_RUNS);
        numReps = ps.getInt(PROP_NUM_REPS);
        ds = (DataStore) cm.lookup(com.sun.labs.aura.datastore.DataStore.class,
                null);

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
        for(Component c : reps) {
            Replicant rep = (Replicant) c;
            try {
                String prefix = rep.getPrefix().toString();
                List l = m.get(prefix);
                logger.info(String.format("rep: " + prefix + " " + (l == null ? 0 : l.size())));
                repMap.put(rep, m.get(prefix));
            } catch(RemoteException rx) {
            }
        }
    }

    class Getter implements Callable<List<Scored<Item>>> {

        private Replicant r;

        private List<Scored<String>> keys;

        private String prefix;

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
            NanoWatch nw = new NanoWatch();
            nw.start();
            logger.info(String.format("gIs %s call", prefix));
            List<Scored<Item>> l = r.getScoredItems(keys);
            nw.stop();
            double overhead = l.size() > 0 ? nw.getTimeMillis() - l.get(0).time
                    : 0;
            logger.info(String.format("gIs %s %d items replicant time %.3fms parallel overhead: %.3fms",
                    prefix, l.size(), l.get(0).time, overhead));

            return l;
        }
    }
}
