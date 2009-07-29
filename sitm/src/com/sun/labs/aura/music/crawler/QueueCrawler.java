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

package com.sun.labs.aura.music.crawler;

import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mailletf
 */
public abstract class QueueCrawler extends ConcurrentCrawler implements QueueCrawlerInterface {

    protected Logger logger;
    protected String CRAWLER_STATE_FILE;
    protected final String crawlerName;
    protected final static int FLUSH_COUNT = 10;
    private int MODIFS_COUNT = 0;

    protected BlockingQueue<QueuedItem> crawlQueue;

    public static final Comparator<QueuedItem> PRIORITY_ORDER = new Comparator<QueuedItem>() {
        @Override
        public int compare(QueuedItem o1, QueuedItem o2) {
            return o1.getPriority() - o2.getPriority();
        }
    };

    public QueueCrawler(String crawlerName, String stateFileName) {
        this.crawlerName = crawlerName;
        CRAWLER_STATE_FILE = stateFileName;
    }


    /**
     * Creates the directory for the state file if necessary
     */
    protected void createStateFileDirectory() throws IOException {
        File stateDirFile = new File(stateDir);
        if (!stateDirFile.exists()) {
            if (!stateDirFile.mkdirs()) {
                throw new IOException("Can't create state file directory");
            }
        }
    }

    protected synchronized void incrementModCounter() {
        incrementModCounter(1);
    }

    protected synchronized void incrementModCounter(int nbr) {
        MODIFS_COUNT+=nbr;
        if (MODIFS_COUNT>=FLUSH_COUNT) {
            saveState();
            MODIFS_COUNT=0;
        }
    }

    /**
     * Loads the previously saveed priority queue
     */
    protected void loadState() {

        ObjectInputStream ois = null;
        try {
            File stateFile = new File(stateDir, CRAWLER_STATE_FILE);
            FileInputStream fis = new FileInputStream(stateFile);
            ois = new ObjectInputStream(fis);

            ArrayList<QueuedItem> newState = (ArrayList<QueuedItem>) ois.readObject();
            if (newState != null) {
                crawlQueue.clear();
                crawlQueue.addAll(newState);
                logger.info(crawlerName+"Crawler: restored discovery queue with " + crawlQueue.size() + " entries");
            }


        } catch (IOException ex) {
            logger.info(crawlerName+"Crawler: could not find crawler state file ("+stateDir+","+CRAWLER_STATE_FILE+")");
        } catch (ClassNotFoundException ex) {
            logger.warning(crawlerName+"Crawler: Bad format in feedscheduler statefile " + ex);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Saves the priority queue to a file.
     */
    protected synchronized void saveState() {
        FileOutputStream fos = null;
        //DebuggingObjectOutputStream oos = null;
        try {
            ArrayList<QueuedItem> aL = new ArrayList<QueuedItem>();
            aL.addAll(crawlQueue);
            File stateFile = new File(stateDir, CRAWLER_STATE_FILE);
            fos = new FileOutputStream(stateFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(aL);
            oos.close();
            logger.fine(crawlerName+"Crawler: Saved state with " + crawlQueue.size() + " entries");
        } catch (IOException ex) {
            logger.warning(crawlerName+"Crawler: Can't save the state of the crawler " + ex);
            ex.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
            }
        }
    }


    
    /** the directory for the crawler.state */
    @ConfigString()
    public final static String PROP_STATE_DIR = "crawlerStateDir";
    protected String stateDir;
    @ConfigInteger(defaultValue = 60 * 60 * 24 * 7 * 2)
    public final static String PROP_UPDATE_RATE = "updateRateInSeconds";
    protected int updateRateInSeconds;
    @ConfigInteger(defaultValue = 1 * 60, range = {1, 60 * 60 * 24 * 365})
    public final static String PROP_NEW_CRAWL_PERIOD = "newCrawlPeriod";
    protected int newCrawlPeriod;

}