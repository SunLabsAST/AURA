/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ReverseScoredComparator;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 */
public class RMISerialTest extends RMIParallelTest {
    
    //
    // Set up the data.
    public RMISerialTest() {
        super();
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
    
    protected List<Scored<Item>> runGet() {
        List<Scored<Item>> ret = new ArrayList<Scored<Item>>();
        NanoWatch nw = new NanoWatch();
        nw.start();
        int nr = 0;
        for(Map.Entry<Replicant,List<Scored<String>>> e : repMap.entrySet()) {
            NanoWatch rnw = new NanoWatch();
            Replicant r = e.getKey(); 
            try {
                String prefix = r.getPrefix().toString();
                rnw.start();
                logger.info(String.format("gIs %s call", prefix));
                List<Scored<Item>> l = r.getItems(e.getValue());
                rnw.stop();
                double overhead = l.size() > 0 ? (rnw.getTimeMillis() -
                        l.get(0).time) : 0;
                logger.info(String.format("gIs %s serial return overhead: %.3fms",
                        prefix,
                        overhead));
                ret.addAll(l);
            } catch(RemoteException rx) {
                logger.severe("Remote exception: " + rx);
            } catch (AuraException ax) {
                logger.severe("Aura exception: " + ax);
            }
            if(++nr > numReps) {
                break;
            }
        }
        Collections.sort(ret, ReverseScoredComparator.COMPARATOR);
        nw.stop();
        logger.info(String.format("Serial get items took %.3f",
                nw.getTimeMillis()));
        return ret;
    }

    public void start() {
        for(int i = 0; i < runs; i++) {
            logger.info("Run " + (i+1));
            List<Scored<Item>> items = runGet();
            display(items);
        }
    }

    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        List<Component> components = cm.lookupAll(com.sun.labs.aura.datastore.impl.Replicant.class, null);
        runs = ps.getInt(PROP_RUNS);
        repMap = new HashMap();
        for(Map.Entry<String,List<Scored<String>>> e : m.entrySet()) {
            for(Component c : components) {
                Replicant rep = (Replicant) c;
                try {
                    String prefix = rep.getPrefix().toString();
                    if(prefix.equals(e.getKey())) {
                        repMap.put(rep, e.getValue());
                        break;
                    }
                } catch (RemoteException rx) {
                    
                }
            }
        }
    }
    
    class Getter implements Callable<List<Scored<Item>>> {
        
        private Replicant r;
        private List<Scored<String>> keys;
        
        private String prefix;
        
        public Getter(List<Scored<String>> keys, Replicant r) {
            this.keys = keys;
            this.r = r;
            try {
                prefix = r.getPrefix().toString();
            } catch (RemoteException rx) {
                logger.severe("Error getting prefix");
            }
        }

        public List<Scored<Item>> call() throws Exception {
            NanoWatch nw = new NanoWatch();
            nw.start();
            logger.info(String.format("gIs %s call", prefix));
            List<Scored<Item>> l = r.getItems(keys);
            nw.stop();
            double overhead = l.size() > 0 ? nw.getTimeMillis() - l.get(0).time : 0;
            logger.info(String.format("gIs %s return overhead: %.3fms", prefix, overhead));
           
            return l;
        }
        
    }

}
