/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.sample;

import com.sun.labs.aura.util.AuraException;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.util.DataStoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Artist;

import com.sun.labs.aura.datastore.impl.DataStoreHead;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.datastore.impl.store.BerkeleyItemStore;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class LastFMLoader {
    int lineCount = 0;
    int attentionCount = 0;
    long createTime = 0;
    long readTime = 0;
    long putTime = 0;
    long attentionTime = 0;
    
    Logger log = Logger.getLogger(LastFMLoader.class.getName());
    
    // Don't really want this here, but java's lexical closures require it
    boolean finished = false;
    
    public LastFMLoader() {
    }

    public void setLogger(Logger log) {
        this.log = log;
    }
    
    /**
     * Loads a LastFM data dump of the form:
     *  username : artist name : musicbrainz artist id : listen count
     */
    public void loadLastFMUsers(Reader inReader, DataStore dataStore, String fieldSep) throws IOException, AuraException {
        BufferedReader input = null;
        if(inReader instanceof BufferedReader) {
            input = (BufferedReader)inReader;
        } else {
            input = new BufferedReader(inReader);
        }

        try {
            String line = null;
            List<Attention> attentions = new ArrayList<Attention>();
            long startTime = System.currentTimeMillis();
            long endTime;
            while((line = input.readLine()) != null) {
                lineCount++;
                
                String[] fields = line.split(fieldSep);
                if(fields.length != 4) {
                    log.severe(String.format("Field: Split to %d part%s: '%s'",
                                             fields.length,
                                             fields.length == 1 ? "" : "s",
                                             line));
                    continue;
                }
                endTime = System.currentTimeMillis();
                readTime += endTime - startTime;
                startTime = endTime;

                Item artist = (new Artist(fields[2], fields[1])).getItem();
                User user = StoreFactory.newUser(fields[0], fields[0]);

                dataStore.putUser(user);
                dataStore.putItem(artist);
                endTime = System.currentTimeMillis();
                putTime += endTime - startTime;
                startTime = endTime;

                int newAttentions = Integer.valueOf(fields[3]);
                int numAttentions = attentionCount + newAttentions;
                attentions.clear();
                for(; attentionCount < numAttentions; attentionCount++) {
                    attentions.add(StoreFactory.newAttention(user, artist, Attention.Type.VIEWED));
                }
                endTime = System.currentTimeMillis();
                createTime += endTime - startTime;
                startTime = endTime;
 
                dataStore.attend(attentions);
                endTime = System.currentTimeMillis();
                attentionTime += endTime - startTime;
                startTime = endTime;
            }
            //log.info("Loading: " + user.getName() + " -> " + artist.getName() + "(" + fields[3] + ")");
        } finally {
            input.close();
        }
    }
    
    /**
     * Logging is done in a TimerTask to make it more regular
     * 
     * @param logger
     * @return
     */
    public TimerTask getLoggerTimerTask() {
        final LastFMLoader loader = this;
        return new TimerTask() {
            int lastLineCount = loader.lineCount;
            int lastAttentionCount = loader.attentionCount;
            long firstTime = System.currentTimeMillis();
            long lastTime = firstTime;
            //DataStoreHead dataStoreHead = (DataStoreHead)dataStore;
        
            public void run() {
                int newLineCount = loader.lineCount;
                int newAttentionCount = loader.attentionCount;
                long newTime = System.currentTimeMillis();
                long totalTime = newTime - firstTime;
                
                loader.log.info(String.format("%3$03d:%2$02d:%1$02d: Line #: %4$06d (+%6$03d): Attentions: %5$d (+%7$03d) (%8$.2f/sec) (Avg: %9$.2f/sec) (r:%10$.2f%%/p:%11$.2f%%/c:%12$.2f%%/a:%13$.2f%%)",
                                              totalTime / 1000 % 60,
                                              totalTime / 60000 % 60,
                                              totalTime / 3600000 % 60,
                                              newLineCount,
                                              newAttentionCount,
                                              newLineCount - lastLineCount,
                                              newAttentionCount - lastAttentionCount,
                                              (float)(newAttentionCount - lastAttentionCount) / (newTime - lastTime) * 1000,
                                              (float)newAttentionCount / totalTime * 1000,
                                              (float)loader.readTime / totalTime * 100,
                                              (float)loader.putTime / totalTime * 100,
                                              (float)loader.createTime / totalTime * 100,
                                              (float)loader.attentionTime / totalTime * 100));
/*
                StringBuffer outputString = new StringBuffer("     Attention Times: ");
                try {
                    Set<PartitionCluster> clusters = dataStore.getPartitionClusters();
                    for(PartitionCluster cluster : clusters) {
                        try {
                            Replicant replicant = cluster.getReplicants().get(0);
                            long attentionTime = loader.attentionTime;
                            long timePutting = Integer.valueOf(replicant.getItem(BerkeleyItemStore.ATTENTION_PUT_TIME).getName());
                            outputString.append(String.format(" %s (put:%.2f%%)", replicant.getPrefix(),
                                                              (float)timePutting / attentionTime * 100));
                        } catch(AuraException ae) {
                            loader.log.severe(ae.toString());
                        } catch(RemoteException re) {
                            loader.log.severe(re.toString());
                        }
                    }
                } catch(RemoteException re) {
                    loader.log.severe(re.toString());
                }

                loader.log.info(outputString.toString());
*/
                lastTime = newTime;
                lastLineCount = newLineCount;
                lastAttentionCount = newAttentionCount;
            }
        };
    }

    public static void main(String[] args) throws IOException, AuraException, InterruptedException, RemoteException {
        String datadir = (System.getProperty("java.io.tmpdir") +
                          File.separator + "lastfm_test");
        
        //final DataStore dataStore = DataStoreFactory.getSimpleDataStore(datadir);
        DataStore dataStore = DataStoreFactory.getSimpleDataStore(datadir);
        LastFMLoader loader = new LastFMLoader();
        loader.log.info("DataStore: " + dataStore);

        Timer loggerTimer = new Timer();
        loggerTimer.scheduleAtFixedRate(loader.getLoggerTimerTask(), 0, 3000);
        
        /*
        if(args.length < 0) {
            String classPath = System.getProperty("java.class.path");
            loader.log.info("Starting java: " + classPath);
            String[] command = new String[] { "java", "-classpath", classPath, LastFMLoader.class.getName() };
            Process javaProcess = Runtime.getRuntime().exec(command);
            loader.log.info("Waiting for java");
            javaProcess.waitFor();
            BufferedReader out = new BufferedReader(new InputStreamReader(javaProcess.getErrorStream()));
            String line = null;
            while((line = out.readLine()) != null) {
                loader.log.info(line);
            }
            loader.log.info("Ended java: " + javaProcess.exitValue());
        }
        */
        try {
            for(String filename : args) {
                loader.log.info("Starting Loading: " + filename + ": " + new Date());
                loader.loadLastFMUsers(new FileReader(filename), dataStore, "<sep>");
                loader.log.info("Finished Loading: " + filename + ": " + new Date());
            }
        } finally {
            loggerTimer.cancel();
            loader.log.info("Exiting main");
        }
    }
}