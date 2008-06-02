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
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.util.DataStoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Artist;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class LastFMLoader {
    Logger log;
    int lineCount = 0;
    int attentionCount = 0;
    
    // Don't really want this here, but java's lexical closures require it
    boolean finished = false;
    
    public LastFMLoader() {
        log = Logger.getLogger(LastFMLoader.class.getName());
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

                Item artist = (new Artist(fields[2], fields[1])).getItem();
                User user = StoreFactory.newUser(fields[0], fields[0]);

                dataStore.putUser(user);
                dataStore.putItem(artist);
                int numAttentions = attentionCount + Integer.valueOf(fields[3]);
                for(; attentionCount < numAttentions; attentionCount++) {
                    dataStore.attend(StoreFactory.newAttention(user, artist, Attention.Type.VIEWED));
                }
            }
            //log.info("Loading: " + user.getName() + " -> " + artist.getName() + "(" + fields[3] + ")");
        } finally {
            input.close();
        }
    }
    
    public static void main(String[] args) throws IOException, AuraException, InterruptedException {
        String datadir = (System.getProperty("java.io.tmpdir") +
                          File.separator + "lastfm_test");
        
        DataStore dataStore = DataStoreFactory.getSimpleDataStore(datadir);
        final LastFMLoader loader = new LastFMLoader();
        loader.log.info("DataStore: " + dataStore);

        TimerTask logger = new TimerTask() {
            int lastLineCount = loader.lineCount;
            int lastAttentionCount = loader.attentionCount;
            long firstTime = System.currentTimeMillis();
            long lastTime = firstTime;
        
            public void run() {
                int newLineCount = loader.lineCount;
                int newAttentionCount = loader.attentionCount;
                long newTime = System.currentTimeMillis();
                loader.log.info(String.format("%2$tY/%2$tm/%2$td %2$tk:%2$tM:%2$tS:%2$tL: Line #: %1$06d (+%4$03d): Attentions: %3$d (+%5$03d) (%6$.2f/sec) (Avg: %7$.2f/sec)",
                                              newLineCount, new Date(), newAttentionCount,
                                              newLineCount - lastLineCount,
                                              newAttentionCount - lastAttentionCount,
                                              (float)(newAttentionCount - lastAttentionCount) / (newTime - lastTime) * 1000,
                                              (float)newAttentionCount / (newTime - firstTime) * 1000));
                lastTime = newTime;
                lastLineCount = newLineCount;
                lastAttentionCount = newAttentionCount;
            }
        };
        
        Timer loggerTimer = new Timer();
        loggerTimer.scheduleAtFixedRate(logger, 0, 3000);
        
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
