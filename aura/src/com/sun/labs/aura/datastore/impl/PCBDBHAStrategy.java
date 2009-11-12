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

package com.sun.labs.aura.datastore.impl;

import com.sleepycat.je.rep.NodeType;
import com.sleepycat.je.rep.ReplicationConfig;
import com.sleepycat.je.rep.ReplicationNode;
import com.sleepycat.je.rep.monitor.GroupChangeEvent;
import com.sleepycat.je.rep.monitor.Monitor;
import com.sleepycat.je.rep.monitor.MonitorChangeListener;
import com.sleepycat.je.rep.monitor.NewMasterEvent;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.FieldCapability;
import com.sun.labs.aura.datastore.Item.FieldType;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.AuraReplicantWriteException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.query.Element;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A strategy that implements support for multiple replicants using the BDB
 * JE HA support in the replicant implementation.  This class must listen
 * for events in the replication group, keeping track of which replicant
 * is the master and directing write events there.  For the time being,
 * it also needs to make sure that the other replicants keep their search
 * indexes up to date by keeping them informed about all the changes
 * in the database.
 * 
 */
public class PCBDBHAStrategy implements PCStrategy, MonitorChangeListener {

    /**
     * All the replicants for this partition / replication group
     */
    protected ArrayList<Replicant> replicants;

    /**
     * The current master (where to send the writes)
     */
    protected Replicant master;

    /**
     * The descriptive data for each replicant in "replicants".
     */
    protected HashMap<Replicant,RepHAInfo> repInfoMap;

    /**
     * The intended group size for this group
     */
    protected int groupSize;

    /**
     * The name of this node in the replication group (symbolic)
     */
    protected String nodeName;

    /**
     * The host and port for this node to listen on
     */
    protected String nodeHostPort;

    /**
     * A way to monitor events in the replication group
     */
    protected Monitor monitor;

    /**
     * A queue to hold on to item keys that have changed.  As we write items
     * we'll keep their keys in here so we can send them to the replicants
     * for reindexing.
     */
    protected ConcurrentLinkedQueue<RepAndKey> modItemKeys;

    /**
     * A Timer for running threads in this class
     */
    protected Timer timer;

    /**
     * For randomly picking a replicant, or other random needs
     */
    protected Random random = new Random();

    private Logger logger = null;

    public PCBDBHAStrategy(Logger logger, int groupSize, String nodeName, String nodeHost) {
        this.logger = logger;
        this.groupSize = groupSize;
        this.nodeName = nodeName;
        this.nodeHostPort = nodeHost;
        replicants = new ArrayList<Replicant>();
        repInfoMap = new HashMap<Replicant,RepHAInfo>();
        modItemKeys = new ConcurrentLinkedQueue<RepAndKey>();

        //
        // Start a timer for pushing item changes out to the search engine in
        // the slave replicants.
        timer = new Timer("PCHATimer", true);
        
    }

    public void addReplicant(Replicant replicant,
                             String repGroupName,
                             String repName,
                             String helperHostStr) throws AuraException {
        RepHAInfo info = new RepHAInfo(repGroupName, repName, helperHostStr);
        repInfoMap.put(replicant, info);
        replicants.add(replicant);
        if (replicants.size() == groupSize) {
            //
            // We're up to the right number of replicants.  We're ready to
            // go!  First, assemble the listener sockets for the whole group
            logger.info("Found required number of replicants");
            String helpers = "";
            for (Iterator<RepHAInfo> it = repInfoMap.values().iterator(); it.hasNext();) {
                RepHAInfo i = it.next();
                helpers += i.getNodeHostPort();
                if (it.hasNext()) {
                    helpers += ",";
                }
            }

            //
            // Define the config and create a monitor
            ReplicationConfig rconf = new ReplicationConfig();
            rconf.setGroupName(repGroupName);
            rconf.setNodeName(nodeName);
            rconf.setNodeType(NodeType.MONITOR);
            rconf.setNodeHostPort(nodeHostPort);
            rconf.setHelperHosts(helpers);

            monitor = new Monitor(rconf);

            //
            // Determine the current master by registering as part of the group.
            // This should change in the next build of HA - starting the
            // listener will invoke the new master event synchronously.
            ReplicationNode currMaster = monitor.register();
            master = getRepByNodeName(currMaster.getName());

            if (master == null) {
                //
                // No master was determined... this isn't good.
                throw new AuraException("Failed to determine master replica");
            }

            //
            // Start listening for change events... hopefully nothing changed
            // since registering.  I don't know that we could start listening
            // before registering.
            try {
                monitor.startListener(this);
            } catch (IOException e) {
                //
                // This is also bad.
                throw new AuraException("Failed to start monitor listener", e);
            }

            //
            // Start the timer to send changed items to the slave indexes
            timer.schedule(new IndexUpdater(), 0, 2 * 1000);
        }
    }

    /**
     * Given a BDB node name, return the matching replicant
     * @param nodeName the name to find
     * @return the replicant with the given name
     */
    protected Replicant getRepByNodeName(String nodeName) {
        Replicant rep = null;
        for (Map.Entry<Replicant,RepHAInfo> entry : repInfoMap.entrySet()) {
            if (entry.getValue().getNodeName().equals(nodeName)) {
                rep = entry.getKey();
                break;
            }
        }
        return rep;
    }

    /**
     * Gets any replicant in the group, useful for read-only querying.
     *
     * @return a replicant suitable for reading data
     */
    protected Replicant getReplicant() {
        if (replicants.isEmpty()) {
            return null;
        }
        return replicants.get(random.nextInt(replicants.size()));
    }

    protected boolean isReady() {
        if (replicants.size() == groupSize) {
            return true;
        }
        return false;
    }

    @Override
    public void notify(NewMasterEvent evt) {
        //
        // Simply assign the new master reference.
        master = getRepByNodeName(evt.getNodeName());

    }

    @Override
    public void notify(GroupChangeEvent evt) {
        //
        // This could be a way to handle restarting replicants, but I suspect
        // the grid process manager and the configuration system will do fine
        // for this.
    }

    @Override
    public void defineField(final String fieldName,
                            final FieldType fieldType,
                            final EnumSet<FieldCapability> caps)
            throws AuraException, RemoteException {
        Command<Void> cmd = new Command<Void>() {

            @Override
            public Void run(Replicant replicant)
                    throws AuraException, RemoteException {
                //
                // This will define the field in the master.
                replicant.defineField(fieldName, fieldType, caps);

                //
                // Now define it in the replicants that aren't the
                // the master for the benefit of the search engine
                String master = repInfoMap.get(replicant).getNodeName();
                for (Map.Entry<Replicant,RepHAInfo> entry :
                                                    repInfoMap.entrySet()) {
                    if (!entry.getValue().getNodeName().equals(master)) {
                        try {
                            entry.getKey().defineFieldSE(
                                    fieldName, fieldType, caps);
                        } catch (Throwable t) {
                            logger.severe(
                                    "Failed to update replica search engines: "
                                    + t.getMessage());
                        }
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return String.format("defineField(%s: %s)",
                                     fieldName, fieldType.toString());
            }

            @Override
            public int hashCode() {
                if (fieldName != null) {
                    return fieldName.hashCode();
                }
                return 0;
            }
        };
        invokeWriteCommand(cmd);
    }

    @Override
    public List<Item> getAll(final ItemType itemType)
            throws AuraException, RemoteException {
        Command<List<Item>> cmd = new Command<List<Item>>() {

            @Override
            public List<Item> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAll(itemType);
            }

            @Override
            public String getDescription() {
                return "getAll(" + (itemType != null? itemType.toString() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (itemType != null) {
                    return itemType.ordinal();
                }
                return 0;
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public DBIterator<Item> getAllIterator(final ItemType itemType)
            throws AuraException, RemoteException {
        Command<DBIterator<Item>> cmd = new Command<DBIterator<Item>>() {

            @Override
            public DBIterator<Item> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAllIterator(itemType);
            }

            @Override
            public String getDescription() {
                return "getAllIt(" + (itemType != null? itemType.toString() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (itemType != null) {
                    return itemType.ordinal();
                }
                return 0;
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public Item getItem(final String key)
            throws AuraException, RemoteException {
        Command<Item> cmd = new Command<Item>() {

            @Override
            public Item run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getItem(key);
            }

            @Override
            public String getDescription() {
                return "getItem(" + key + ")";
            }

            @Override
            public int hashCode() {
                if (key != null) {
                    return key.hashCode();
                }
                return 0;
            }
        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<Item>> getScoredItems(final List<Scored<String>> keys)
            throws AuraException, RemoteException {
        Command<List<Scored<Item>>> cmd = new Command<List<Scored<Item>>>() {

            @Override
            public List<Scored<Item>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getScoredItems(keys);
            }

            @Override
            public String getDescription() {
                return "getScoredItems(" + (keys != null ? keys.size() : "null") + ")";
            }

            @Override
            public int hashCode() {
                //
                // This may defeat the cache, but we'll just pick based on
                // the first key.
                if (keys != null && !keys.isEmpty()) {
                    String key = keys.get(0).getItem();
                    if (key != null) {
                        return key.hashCode();
                    }
                }
                //
                // I wonder if we should throw an exception here since really
                // the partition should never be queried for an empty list
                // of keys.  This just adds network congestion/latency.
                return 0;
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public Collection<Item> getItems(final Collection<String> keys)
            throws AuraException, RemoteException {
        Command<Collection<Item>> cmd = new Command<Collection<Item>>() {

            @Override
            public Collection<Item> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getItems(keys);
            }

            @Override
            public String getDescription() {
                return "getItems(" + (keys != null ? keys.size() : "null") + ")";
            }

            @Override
            public int hashCode() {
                //
                // Just pick a key to use since we'll be fetching
                // multiple and the cache won't come into play.
                if (keys != null && !keys.isEmpty()) {
                    String key = keys.iterator().next();
                    if (key != null) {
                        return key.hashCode();
                    }
                }
                return 0;
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public User getUserForRandomString(final String randStr)
            throws AuraException, RemoteException {
        Command<User> cmd = new Command<User>() {

            @Override
            public User run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getUserForRandomString(randStr);
            }

            @Override
            public String getDescription() {
                return "getUserForRandStr(" + randStr + ")";
            }

            @Override
            public int hashCode() {
                if (randStr != null) {
                    return randStr.hashCode();
                }
                return 0;
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public Item putItem(final Item item)
            throws AuraException, RemoteException {
        Command<Item> cmd = new Command<Item>() {

            @Override
            public Item run(Replicant replicant) throws AuraException, RemoteException {
                Item result = replicant.putItem(item);
                modItemKeys.add(new RepAndKey(
                        result.getKey(),
                        repInfoMap.get(replicant).getNodeName()));
                return result;
            }

            @Override
            public String getDescription() {
                return "putItem(" + (item != null ? item.getKey() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (item != null && item.getKey() != null) {
                    return item.getKey().hashCode();
                }
                return 0;
            }
        };
        return invokeWriteCommand(cmd);
    }

    @Override
    public void deleteItem(final String itemKey)
            throws AuraException, RemoteException {
        Command<Void> cmd = new Command<Void>() {

            @Override
            public Void run(Replicant replicant) throws AuraException, RemoteException {
                replicant.deleteItem(itemKey);

                //
                // Now delete it in the replicants that aren't the
                // the master for the benefit of the search engine
                String master = repInfoMap.get(replicant).getNodeName();
                for (Map.Entry<Replicant,RepHAInfo> entry :
                                                    repInfoMap.entrySet()) {
                    if (!entry.getValue().getNodeName().equals(master)) {
                        try {
                            entry.getKey().deleteItemSE(itemKey);
                        } catch (Throwable t) {
                            logger.severe(
                                    "Failed to update replica search engines: "
                                    + t.getMessage());
                        }
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "deleteItem(" + itemKey + ")";
            }

            @Override
            public int hashCode() {
                if (itemKey != null) {
                    return itemKey.hashCode();
                }
                return 0;
            }

        };
        invokeWriteCommand(cmd);
    }

    @Override
    public void deleteAttention(final List<Long> ids)
            throws AuraException, RemoteException {
        Command<Void> cmd = new Command<Void>() {

            @Override
            public Void run(Replicant replicant) throws AuraException, RemoteException {
                replicant.deleteAttention(ids);
                return null;
            }

            @Override
            public String getDescription() {
                return "deleteAttention(" + (ids != null? ids.size() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (ids != null && !ids.isEmpty()) {
                    return ids.get(0).hashCode();
                } else {
                    return 0;
                }
            }

        };
        invokeWriteCommand(cmd);
    }

    @Override
    public DBIterator<Item> getItemsAddedSince(final ItemType type,
                                               final Date timeStamp)
            throws AuraException, RemoteException {
        Command<DBIterator<Item>> cmd = new Command<DBIterator<Item>>() {

            @Override
            public DBIterator<Item> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getItemsAddedSince(type, timeStamp);
            }

            @Override
            public String getDescription() {
                String t = (type != null? type.toString() : "null");
                String d = (timeStamp != null? timeStamp.toString() : "null");
                return String.format("getItemsAddedSince(%s, %s)", t, d);
            }

            @Override
            public int hashCode() {
                if (type != null) {
                    return type.ordinal();
                }
                return 0;
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Item> getItems(final User user,
                               final Type attnType,
                               final ItemType itemType)
            throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Attention> getAttention(final AttentionConfig ac)
            throws AuraException, RemoteException {
        Command<List<Attention>> cmd = new Command<List<Attention>>() {

            @Override
            public List<Attention> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAttention(ac);
            }

            @Override
            public String getDescription() {
                return "getAttention(AttnConf)";
            }

            @Override
            public int hashCode() {
                //
                // define hashcode in AC?
                return ac.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public DBIterator<Attention> getAttentionIterator(final AttentionConfig ac)
            throws AuraException, RemoteException {
        Command<DBIterator<Attention>> cmd =
                new Command<DBIterator<Attention>>() {

            @Override
            public DBIterator<Attention> run(Replicant replicant)
                    throws AuraException, RemoteException {
                return replicant.getAttentionIterator(ac);
            }

            @Override
            public String getDescription() {
                return "getAttentionIterator(AttnConf)";
            }

            @Override
            public int hashCode() {
                return ac.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public Long getAttentionCount(final AttentionConfig ac)
            throws AuraException, RemoteException {
        Command<Long> cmd = new Command<Long>() {

            @Override
            public Long run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAttentionCount(ac);
            }

            @Override
            public String getDescription() {
                return "getAttentionCount(AttnConf)";
            }

            @Override
            public int hashCode() {
                return ac.hashCode();
            }
        };
        return invokeCommand(cmd);
    }

    @Override
    public Object processAttention(final AttentionConfig ac,
                                   final String script,
                                   final String language)
            throws AuraException, RemoteException {
        Command<Object> cmd = new Command<Object>() {

            @Override
            public Object run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.processAttention(ac, script, language);
            }

            @Override
            public String getDescription() {
                return "processAttn()";
            }

            @Override
            public int hashCode() {
                return ac.hashCode();
            }
        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Attention> getAttentionSince(final AttentionConfig ac,
                                             final Date timeStamp)
            throws AuraException, RemoteException {
        Command<List<Attention>> cmd = new Command<List<Attention>>() {

            @Override
            public List<Attention> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAttentionSince(ac, timeStamp);
            }

            @Override
            public String getDescription() {
                return "getAttentionSince(AttnConf, "
                        + timeStamp.toString() + ")";
            }

            @Override
            public int hashCode() {
                return ac.hashCode();
            }
        };
        return invokeCommand(cmd);
    }

    @Override
    public DBIterator<Attention> getAttentionSinceIterator(
                                            final AttentionConfig ac,
                                            final Date timeStamp)
            throws AuraException, RemoteException {
        Command<DBIterator<Attention>> cmd =
                new Command<DBIterator<Attention>>() {

            @Override
            public DBIterator<Attention> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAttentionSinceIterator(ac, timeStamp);
            }

            @Override
            public String getDescription() {
                return "getAttnSinceIterator(AttnConf, "
                        + timeStamp.toString() + ")";
            }

            @Override
            public int hashCode() {
                return ac.hashCode();
            }
        };
        return invokeCommand(cmd);
    }

    @Override
    public Long getAttentionSinceCount(final AttentionConfig ac,
                                       final Date timeStamp)
            throws AuraException, RemoteException {
        Command<Long> cmd = new Command<Long>() {

            @Override
            public Long run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAttentionSinceCount(ac, timeStamp);
            }

            @Override
            public String getDescription() {
                return "getAttnSinceCount(AttnConf, "
                        + timeStamp.toString() + ")";
            }

            @Override
            public int hashCode() {
                return ac.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Attention> getLastAttention(final AttentionConfig ac,
                                            final int count)
            throws AuraException, RemoteException {
        Command<List<Attention>> cmd = new Command<List<Attention>>() {

            @Override
            public List<Attention> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getLastAttention(ac, count);
            }

            @Override
            public String getDescription() {
                return "getLastAttention(AttnConf, " + count + ")";
            }

            @Override
            public int hashCode() {
                return ac.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public Attention attend(final Attention att)
            throws AuraException, RemoteException {
        Command<Attention> cmd = new Command<Attention>() {

            @Override
            public Attention run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.attend(att);
            }

            @Override
            public String getDescription() {
                return "attend(" + att.getType().toString() + ")";
            }

            @Override
            public int hashCode() {
                return att.hashCode();
            }
        };
        return invokeWriteCommand(cmd);
    }

    @Override
    public List<Attention> attend(final List<Attention> attns)
            throws AuraException, RemoteException {
        Command<List<Attention>> cmd = new Command<List<Attention>>() {

            @Override
            public List<Attention> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.attend(attns);
            }

            @Override
            public String getDescription() {
                return "attend(" + (attns != null? attns.size() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (attns != null && !attns.isEmpty()) {
                    return attns.get(0).hashCode();
                }
                return 0; // probably not relevant anyway for a write
            }

        };
        return invokeWriteCommand(cmd);
    }

    @Override
    public void removeAttention(final String srcKey,
                                final String targetKey,
                                final Type type)
            throws AuraException, RemoteException {
        Command<Void> cmd = new Command<Void>() {

            @Override
            public Void run(Replicant replicant) throws AuraException, RemoteException {
                replicant.removeAttention(srcKey, targetKey, type);
                return null;
            }

            @Override
            public String getDescription() {
                return String.format("rmAttn(%s, %s, %s)",
                        srcKey, targetKey, type.toString());
            }

            @Override
            public int hashCode() {
                if (srcKey == null || targetKey == null) {
                    return 0;
                }
                String combo = srcKey + targetKey;
                return combo.hashCode();
            }

        };
        invokeWriteCommand(cmd);
    }

    @Override
    public void removeAttention(final String itemKey)
            throws AuraException, RemoteException {
        Command<Void> cmd = new Command<Void>() {

            @Override
            public Void run(Replicant replicant) throws AuraException, RemoteException {
                replicant.removeAttention(itemKey);
                return null;
            }

            @Override
            public String getDescription() {
                return "removeAttention(" + itemKey + ")";
            }

            @Override
            public int hashCode() {
                return (itemKey != null? itemKey.hashCode() : 0);
            }

        };
        invokeWriteCommand(cmd);
    }

    @Override
    public void addItemListener(final ItemType itemType,
                                final ItemListener listener)
            throws AuraException, RemoteException {
        Command<Void> cmd = new Command<Void>() {

            @Override
            public Void run(Replicant replicant)
                    throws AuraException, RemoteException {
                replicant.addItemListener(itemType, listener);
                return null;
            }

            @Override
            public String getDescription() {
                return "addItemListener(" + (itemType != null? itemType.toString() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (itemType == null) {
                    return 0;
                }
                return itemType.ordinal();
            }

        };
        //
        // Run this as a write command since we want it to go to the master
        invokeWriteCommand(cmd);
    }

    @Override
    public void removeItemListener(final ItemType itemType,
                                   final ItemListener listener)
            throws AuraException, RemoteException {
        Command<Void> cmd = new Command<Void>() {

            @Override
            public Void run(Replicant replicant)
                    throws AuraException, RemoteException {
                replicant.removeItemListener(itemType, listener);
                return null;
            }

            @Override
            public String getDescription() {
                return "removeItemListener(" + (itemType != null? itemType.toString() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (itemType == null) {
                    return 0;
                }
                return itemType.ordinal();
            }

        };
        //
        // Run this as a write command since we want it to go to the master
        invokeWriteCommand(cmd);
    }

    @Override
    public long getItemCount(final ItemType itemType)
            throws AuraException, RemoteException {
        Command<Long> cmd = new Command<Long>() {

            @Override
            public Long run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getItemCount(itemType);
            }

            @Override
            public String getDescription() {
                return "getItemCount(" + (itemType != null? itemType.toString() : "null") + ")";
            }

            @Override
            public int hashCode() {
                if (itemType == null) {
                    return 0;
                }
                return itemType.ordinal();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<FieldFrequency> getTopValues(final String field,
                                             final int n,
                                             final boolean ignoreCase)
            throws AuraException, RemoteException {
        Command<List<FieldFrequency>> cmd =
                new Command<List<FieldFrequency>>() {

            @Override
            public List<FieldFrequency> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getTopValues(field, n, ignoreCase);
            }

            @Override
            public String getDescription() {
                return String.format("getTopValues(%s, %d, %b)",
                                     field, n, ignoreCase);
            }

            @Override
            public int hashCode() {
                if (field == null) {
                    return 0;
                }
                return field.hashCode();
            }
        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<String>> query(final String query,
                                      final String sort,
                                      final int n,
                                      final ResultsFilter rf)
            throws AuraException, RemoteException {
        Command<List<Scored<String>>> cmd =
                new Command<List<Scored<String>>>() {

            @Override
            public List<Scored<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.query(query, sort, n, rf);
            }

            @Override
            public String getDescription() {
                return String.format("query(%s, %s, %d, rf)",
                                     query, sort, n);
            }

            @Override
            public int hashCode() {
                if (query == null) {
                    return 0;
                }
                return query.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<String>> query(final Element query,
                                      final String sort,
                                      final int n,
                                      final ResultsFilter rf)
            throws AuraException, RemoteException {
        Command<List<Scored<String>>> cmd =
                new Command<List<Scored<String>>>() {

            @Override
            public List<Scored<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.query(query, sort, n, rf);
            }

            @Override
            public String getDescription() {
                if (query != null && query.getQueryElement() != null) {
                    return String.format("query(%s, %s, %d, rf)",
                                         query.getQueryElement().toString(),
                                         sort, n);
                }
                return "query(null)";
            }

            @Override
            public int hashCode() {
                if (query == null || query.getQueryElement() == null) {
                    return 0;
                }
                return query.getQueryElement().toString().hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<String>> getAutotagged(final String autotag,
                                              final int n)
            throws AuraException, RemoteException {
        Command<List<Scored<String>>> cmd =
                new Command<List<Scored<String>>>() {

            @Override
            public List<Scored<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getAutotagged(autotag, n);
            }

            @Override
            public String getDescription() {
                return String.format("getAutotagged(%s, %d)", autotag, n);
            }

            @Override
            public int hashCode() {
                if (autotag == null) {
                    return 0;
                }
                return autotag.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<String>> getTopAutotagTerms(final String autotag,
                                                   final int n)
            throws AuraException, RemoteException {
        Command<List<Scored<String>>> cmd =
                new Command<List<Scored<String>>>() {

            @Override
            public List<Scored<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getTopAutotagTerms(autotag, n);
            }

            @Override
            public String getDescription() {
                return String.format("getAutotagTerms(%s, %d)", autotag, n);
            }

            @Override
            public int hashCode() {
                if (autotag == null) {
                    return 0;
                }
                return autotag.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<String>> findSimilarAutotags(final String autotag,
                                                    final int n)
            throws AuraException, RemoteException {
        Command<List<Scored<String>>> cmd =
                new Command<List<Scored<String>>>() {

            @Override
            public List<Scored<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.findSimilarAutotags(autotag, n);
            }

            @Override
            public String getDescription() {
                return String.format("findSimilarAutotags(%s, %d)", autotag, n);
            }

            @Override
            public int hashCode() {
                if (autotag == null) {
                    return 0;
                }
                return autotag.hashCode();
            }
        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<String>> explainSimilarAutotags(final String a1,
                                                       final String a2,
                                                       final int n)
            throws AuraException, RemoteException {
        Command <List<Scored<String>>> cmd =
                new Command<List<Scored<String>>>() {

            @Override
            public List<Scored<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.explainSimilarAutotags(a1, a2, n);
            }

            @Override
            public String getDescription() {
                return String.format("explainSimilarAutotags(%s, %s, %d)", a1, a2, n);
            }

            @Override
            public int hashCode() {
                if (a1 == null) {
                    return 0;
                }
                return a1.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public WordCloud getTopTerms(final String key,
                                 final String field,
                                 final int n)
            throws AuraException, RemoteException {
        Command<WordCloud> cmd = new Command<WordCloud>() {

            @Override
            public WordCloud run(Replicant replicant)
                    throws AuraException, RemoteException {
                return replicant.getTopTerms(key, field, n);
            }

            @Override
            public String getDescription() {
                return String.format("getTopTerms(%s, %s, %d)", key, field, n);
            }

            @Override
            public int hashCode() {
                if (key == null) {
                    return 0;
                }
                return key.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Counted<String>> getTopTermCounts(final String key,
                                                  final String field,
                                                  final int n)
            throws AuraException, RemoteException {
        Command<List<Counted<String>>> cmd = new
                Command<List<Counted<String>>>() {

            @Override
            public List<Counted<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getTopTermCounts(key, field, n);
            }

            @Override
            public String getDescription() {
                return String.format("getDescription(%s, %s, %d)",
                                     key, field, n);
            }

            @Override
            public int hashCode() {
                if (key == null) {
                    return 0;
                }
                return key.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Counted<String>> getTermCounts(final String term,
                                               final String field,
                                               final int n,
                                               final ResultsFilter rf)
            throws AuraException, RemoteException {
        Command<List<Counted<String>>> cmd =
                new Command<List<Counted<String>>>() {

            @Override
            public List<Counted<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getTermCounts(term, field, n, rf);
            }

            @Override
            public String getDescription() {
                return String.format("getTermCounts(%s, %s, %d, rf)",
                                     term, field, n);
            }

            @Override
            public int hashCode() {
                if (term == null) {
                    return 0;
                }
                return term.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<Scored<String>> getExplanation(final String key,
                                               final String autoTag,
                                               final int n)
            throws AuraException, RemoteException {
        Command<List<Scored<String>>> cmd =
                new Command<List<Scored<String>>>() {

            @Override
            public List<Scored<String>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getExplanation(key, autoTag, n);
            }

            @Override
            public String getDescription() {
                return String.format("getExplanation(%s, %s, %d)",
                                     key, autoTag, n);
            }

            @Override
            public int hashCode() {
                if (key == null) {
                    return 0;
                }
                return key.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public MarshalledObject<DocumentVector> getDocumentVector(
                                    final String key,
                                    final SimilarityConfig config)
            throws RemoteException, AuraException {
        Command<MarshalledObject<DocumentVector>> cmd =
                new Command<MarshalledObject<DocumentVector>>() {

            @Override
            public MarshalledObject<DocumentVector> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getDocumentVector(key, config);
            }

            @Override
            public String getDescription() {
                return String.format("getDocumentVector(%s, simConf)", key);
            }

            @Override
            public int hashCode() {
                if (key == null) {
                    return 0;
                }
                return key.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public MarshalledObject<DocumentVector> getDocumentVector(
                                    final WordCloud cloud,
                                    final SimilarityConfig config)
            throws RemoteException, AuraException {
        Command<MarshalledObject<DocumentVector>> cmd =
                new Command<MarshalledObject<DocumentVector>>() {

            @Override
            public MarshalledObject<DocumentVector> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getDocumentVector(cloud, config);
            }

            @Override
            public String getDescription() {
                return String.format("getDocumentVector(%s, simConf)", cloud.toString());
            }

            @Override
            public int hashCode() {
                if (cloud == null) {
                    return 0;
                }
                return cloud.toString().hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public MarshalledObject<List<Scored<String>>> findSimilar(
                                    final MarshalledObject<DocumentVector> dv,
                                    final MarshalledObject<SimilarityConfig> config)
            throws AuraException, RemoteException {
        Command<MarshalledObject<List<Scored<String>>>> cmd =
                new Command<MarshalledObject<List<Scored<String>>>>() {

            @Override
            public MarshalledObject<List<Scored<String>>> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.findSimilar(dv, config);
            }

            @Override
            public String getDescription() {
                return "findSimilar(DV, simConf)";
            }

            @Override
            public int hashCode() {
                return dv.hashCode();
            }

        };
        return invokeCommand(cmd);
    }

    @Override
    public List<String> getSupportedScriptLanguages()
            throws AuraException, RemoteException {
        Command<List<String>> cmd = new Command<List<String>>() {

            @Override
            public List<String> run(Replicant replicant) throws AuraException, RemoteException {
                return replicant.getSupportedScriptLanguages();
            }

            @Override
            public String getDescription() {
                return "getSupportedScriptLanguages()";
            }

            @Override
            public int hashCode() {
                return 0;
            }
            
        };
        return invokeCommand(cmd);
    }

    @Override
    public void close()
            throws AuraException, RemoteException {
        //
        // Close down all the replicants
        for (Replicant rep : repInfoMap.keySet()) {
            try {
                //rep.close();
            } catch (Throwable t) {
                logger.severe(
                        "Failed to close replica: "
                        + t.getMessage());
            }
        }
    }

    /**
     * Simply execute the command, allowing any exceptions thrown to be
     * passed up to the DataStoreHead.
     * @param <R> the return type of the command
     * @param cmd the command to run
     * @return the result
     */
    protected <R> R invokeCommand(Command<R> cmd)
            throws AuraException, RemoteException {
        //
        // In order to help caching, we'll direct requests for the same data
        // to the same replicants.  The commands must all define a hashcode
        // based on the data they are accessing in order to do this.  Any
        // replicant can satisfy any query, but we can keep more objects in
        // cache and get more cache hits this way.
        int hashcode = Math.abs(cmd.hashCode());
        int repNum = hashcode % replicants.size();
/*        if (logger.isLoggable(Level.FINER)) {
            Replicant selected = replicants.get(repNum);
            RepHAInfo info = repInfoMap.get(selected);
            logger.finer("Invoking on replicant " + info.getNodeName());
        }*/
        return cmd.run(replicants.get(repNum));
    }

    /**
     * Executes a command, catching any AuraReplicantWriteException.  In the
     * case of an AuraReplicantWriteException, it gets the new master and retries
     * the operation.
     *
     * @param <R> the return type of the command
     * @param cmd the command to run
     * @return the result
     * @throws AuraException
     * @throws RemoteException
     */
    protected <R> R invokeWriteCommand(Command<R> cmd)
            throws AuraException, RemoteException {
        R result = null;
        int numRetries = 5;
        boolean failed = true;
        while (failed && numRetries-- > 0) {
            try {
                result = cmd.run(master);
            } catch (AuraReplicantWriteException e) {
                //
                // update the master and try again
                master = getRepByNodeName(monitor.getMasterNodeName());
                continue;
            }
            failed = false;
        }
        return result;
    }

    /**
     * Defines a command that can be run against a replicant
     * 
     * @param <R> the return type of the command
     */
    public abstract class Command<R> {
        /**
         * Run this command
         *
         * @return the result of the command
         */
        public abstract R run(Replicant replicant)
                throws AuraException, RemoteException;

        /**
         * Return a description of this command
         *
         * @return the description of this command
         */
        public abstract String getDescription();

        /**
         * Return a hashcode for this command based on the data it is
         * accessing.
         *
         * @return a hashcode
         */
        @Override
        public abstract int hashCode();

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Command<R> other = (Command<R>) obj;
            return true;
        }
    }

    /**
     * A simple struct class
     */
    public class RepHAInfo {
        protected String groupName;
        protected String nodeName;
        protected String nodeHostPort;

        public RepHAInfo(String groupName, String nodeName, String nodeHost) {
            this.groupName = groupName;
            this.nodeName = nodeName;
            this.nodeHostPort = nodeHost;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getNodeName() {
            return nodeName;
        }

        public String getNodeHostPort() {
            return nodeHostPort;
        }
    }

    public class RepAndKey {
        public String key;
        public String repName;
        public long timeStamp;

        public RepAndKey(String key, String repName) {
            this.key = key;
            this.repName = repName;
            timeStamp = System.currentTimeMillis();
        }
    }

    /**
     * This should be run every few seconds in order to flush out the
     * queue of modified item keys to the replicants that don't have
     * them.
     */
    public class IndexUpdater extends TimerTask {

        @Override
        public void run() {
            try {
                //
                // For now, we're going to do this the simple way and call
                // each replicant in serial.
                long flushTime = System.currentTimeMillis() - (4 * 1000);
                if (modItemKeys.isEmpty()) {
                    return;
                }

                //
                // Collect up all the keys that are in the right time window
                // and that have the same master.
                List<String> keysToSend = new ArrayList<String>();
                RepAndKey head = modItemKeys.peek();
                String master = head.repName;
                while (!modItemKeys.isEmpty()
                         && head.timeStamp <= flushTime
                         && head.repName.equals(master)) {
                    head = modItemKeys.poll();
                    keysToSend.add(head.key);
                }

                //
                // Now send to all the replicants that aren't the master
                for (Map.Entry<Replicant,RepHAInfo> entry : repInfoMap.entrySet()) {
                    if (!entry.getValue().getNodeName().equals(master)) {
                        try {
                            entry.getKey().indexItemsSE(keysToSend);
                        } catch (Throwable t) {
                            logger.severe(
                                    "Failed to update replica search engines: "
                                    + t.getMessage());
                        }
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Failed to send index update events", t);
            }
        }

    }
}
