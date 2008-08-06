/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.util.ReverseScoredComparator;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
public class RMIParallelTest extends ServiceAdapter {

    @ConfigInteger(defaultValue=30)
    public static final String PROP_RUNS = "runs";
    protected int runs;
    
    protected Map<String,List<Scored<String>>> m;
    
    protected Map<Replicant, List<Scored<String>>> repMap;
    
    protected ExecutorService executor;
    String[] prefixes = {
        "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000", "0000",
        "0001", "0001", "0001", "0001", "0001", "0001", "0001", "0001", "0001",
        "0010", "0010", "0010", "0010", "0010", "0010", "0011", "0011", "0011",
        "0011", "0100", "0100", "0101", "0101", "0101", "0101", "0101", "0101",
        "0101", "0101", "0110", "0110", "0110", "0110", "0110", "0110", "0111",
        "0111", "0111", "0111", "1000", "1000", "1000", "1000", "1000", "1000",
        "1000", "1000", "1000", "1001", "1001", "1001", "1010", "1010", "1010",
        "1010", "1010", "1010", "1010", "1010", "1010", "1010", "1010", "1011",
        "1011", "1011", "1011", "1011", "1011", "1011", "1011", "1011", "1100",
        "1100", "1100", "1100", "1100", "1100", "1100", "1101", "1101", "1101",
        "1101", "1101", "1101", "1110", "1110", "1110", "1110", "1110", "1111",
        "1111", "1111"
    };
    
    String[] keys = {
        "artist-tag:alternativemetal", "artist-tag:metalcore",
        "artist-tag:melodicdeathmetal", "artist-tag:hardcore",
        "artist-tag:emocore", "artist-tag:britpop", "artist-tag:posthardcore",
        "artist-tag:poprock", "artist-tag:hiphop", "artist-tag:metal",
        "artist-tag:metal", "artist-tag:heavy", "artist-tag:emo",
        "artist-tag:classicrock", "artist-tag:extrememetal",
        "artist-tag:gothicrock", "artist-tag:hard",
        "artist-tag:femalefrontedmetal", "artist-tag:progressivemetal",
        "artist-tag:electronic", "artist-tag:swedishmetal",
        "artist-tag:finnishmetal", "artist-tag:goth", "artist-tag:80smetal",
        "artist-tag:realmetal", "artist-tag:newwave", "artist-tag:truemetal",
        "artist-tag:ska", "artist-tag:nordicmetal", "artist-tag:industrialrock",
        "artist-tag:symphonicmetal", "artist-tag:indie", "artist-tag:80s",
        "artist-tag:trashmetal", "artist-tag:progressive", "artist-tag:neometal",
        "artist-tag:gothmetal", "artist-tag:melodicblackmetal",
        "artist-tag:heavymetal", "artist-tag:hardrock", "artist-tag:american",
        "artist-tag:90s", "artist-tag:speedmetal", "artist-tag:heavyrock",
        "artist-tag:alternative", "artist-tag:vikingmetal",
        "artist-tag:groovemetal", "artist-tag:melodicdeath", "artist-tag:grunge",
        "artist-tag:punk", "artist-tag:powermetal", "artist-tag:industrialmetal",
        "artist-tag:postgrunge", "artist-tag:doommetal", "artist-tag:stonerrock",
        "artist-tag:love", "artist-tag:altrock", "artist-tag:thrashmetal",
        "artist-tag:deathmetal", "artist-tag:folkmetal", "artist-tag:numetal",
        "artist-tag:progressiverock", "artist-tag:melodicmetal",
        "artist-tag:screamo", "artist-tag:indierock", "artist-tag:notemo",
        "artist-tag:80srock", "artist-tag:modernrock", "artist-tag:aggressive",
        "artist-tag:hardnheavy", "artist-tag:singersongwriter",
        "artist-tag:rock", "artist-tag:gothicmetal", "artist-tag:classicmetal",
        "artist-tag:angry", "artist-tag:pop", "artist-tag:malevocalists",
        "artist-tag:epic", "artist-tag:germanmetal", "artist-tag:rap",
        "artist-tag:gothic", "artist-tag:scandinavianmetal",
        "artist-tag:epicmetal", "artist-tag:rapmetal", "artist-tag:classic",
        "artist-tag:glamrock", "artist-tag:femalevocalists",
        "artist-tag:punkrock", "artist-tag:usa", "artist-tag:newmetal",
        "artist-tag:00s", "artist-tag:british", "artist-tag:dance",
        "artist-tag:blackmetal", "artist-tag:thrash", "artist-tag:death",
        "artist-tag:crossover", "artist-tag:experimental",
        "artist-tag:industrial", "artist-tag:hairmetal",
        "artist-tag:electronica"        
    };
    
    double[] scores = {
        0.25850751996040344, 0.21360158920288086, 0.20865803956985474,
        0.18998229503631592, 0.11779607087373734, 0.10583795607089996,
        0.10529313236474991, 0.10282887518405914, 0.08711230754852295,
        1.399999976158142, 0.3901471197605133, 0.2280050665140152,
        0.20778292417526245, 0.20648784935474396, 0.10811298340559006,
        0.10391222685575485, 0.09987305104732513, 0.08932939171791077,
        0.2049870789051056, 0.09743238985538483, 0.09439047425985336,
        0.09089867025613785, 0.09021870791912079, 0.08574504405260086,
        0.16481520235538483, 0.1154828742146492, 0.11169471591711044,
        0.09429747611284256, 0.0910370796918869, 0.08498496562242508,
        0.16592955589294434, 0.14370499551296234, 0.13335676491260529,
        0.12934884428977966, 0.10995218902826309, 0.10816676914691925,
        0.10768448561429977, 0.07994617521762848, 0.3256959915161133,
        0.31495168805122375, 0.1839022934436798, 0.16058188676834106,
        0.15696680545806885, 0.08280783146619797, 0.23164023458957672,
        0.10994719713926315, 0.10342172533273697, 0.08122500032186508,
        0.2427075058221817, 0.1959642767906189, 0.18155336380004883,
        0.16006314754486084, 0.142515629529953, 0.1286296397447586,
        0.12347717583179474, 0.12026442587375641, 0.1107969731092453,
        0.22107484936714172, 0.21009163558483124, 0.11139493435621262,
        0.23970337212085724, 0.1816878318786621, 0.17167548835277557,
        0.16144289076328278, 0.1333201378583908, 0.10879983752965927,
        0.0914406031370163, 0.08821894973516464, 0.08398014307022095,
        0.08002113550901413, 0.0798984095454216, 0.2515893578529358,
        0.20680901408195496, 0.16043581068515778, 0.12807533144950867,
        0.11580109596252441, 0.09903021901845932, 0.0858699157834053,
        0.0811903104186058, 0.07801517844200134, 0.14953091740608215,
        0.1386905312538147, 0.09938688576221466, 0.09097852557897568,
        0.08441482484340668, 0.08285405486822128, 0.08200269937515259,
        0.1759338676929474, 0.16057132184505463, 0.13549984991550446,
        0.1107710748910904, 0.09081736207008362, 0.08049558103084564,
        0.16122493147850037, 0.12865817546844482, 0.12751714885234833,
        0.10779841989278793, 0.0838690847158432, 0.17702840268611908,
        0.08575554192066193, 0.08304892480373383        
    };
    
    //
    // Set up the data.
    public RMIParallelTest() {
        m = new HashMap();
        for(int i = 0; i < prefixes.length; i++) {
            List<Scored<String>> l = m.get(prefixes[i]);
            if(l == null) {
                l = new ArrayList();
                m.put(prefixes[i], l);
            }
            l.add(new Scored<String>(keys[i], scores[i]));
        }
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
//        for(Scored<Item> item : l) {
//            logger.info("key: " + item.getItem().getKey());
//        }
    }
    
    protected List<Scored<Item>> runGet() {
        NanoWatch nw = new NanoWatch();
        nw.start();
        logger.info("Pool size: " + ((ThreadPoolExecutor) executor).getPoolSize());
        List<Callable<List<Scored<Item>>>> callers =
                new ArrayList();
        for(Map.Entry<Replicant,List<Scored<String>>> e : repMap.entrySet()) {
            callers.add(new Getter(e.getValue(), e.getKey()));
        }
        List<Scored<Item>> ret = new ArrayList();
        try {
            List<Future<List<Scored<Item>>>> futures = executor.invokeAll(
                    callers);
            int timesThrough = 0;
            while(futures.size() > 0) {
                timesThrough++;
                for(Iterator<Future<List<Scored<Item>>>> i = futures.iterator(); i.
                        hasNext();) {
                    Future<List<Scored<Item>>> f = i.next();
                    if(f.isCancelled()) {
                        i.remove();
                    } else if(f.isDone()) {
                        ret.addAll(f.get());
                        i.remove();
                    }
                }
            }
            Collections.sort(ret, ReverseScoredComparator.COMPARATOR);
            nw.stop();
            logger.info(String.format("Parallel get items took %.3f times through loop: %d", nw.getTimeMillis(), timesThrough));
            return ret;
        } catch (InterruptedException ie) {
            return null;
        } catch (ExecutionException ex) {
            return null;
        }
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
        Component[] components = cm.getComponentRegistry().lookup(com.sun.labs.aura.datastore.impl.Replicant.class, Integer.MAX_VALUE);
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
            logger.info(String.format("gIs %s parallel return overhead: %.3fms", prefix, overhead));
           
            return l;
        }
        
    }

}
