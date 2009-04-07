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

package com.sun.labs.aura.aardvark.util;

/**
 *
 * @author plamere
 */
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.ShellUtils;
import com.sun.labs.aura.service.StatService;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Manages the set of feed crawling threads
 * @author plamere
 */
public class AardvarkShell implements AuraService, Configurable {

    private DataStore dataStore;
    private CommandInterpreter shell;
    private Aardvark aardvark;
    private StatService statService;
    private Logger logger;
    private ShellUtils sutils;

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        shell = new CommandInterpreter();
        shell.setPrompt("aardv% ");
        sutils = new ShellUtils(shell, dataStore, statService);


        shell.add("user",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2) {
                                dumpAllUsers();
                            } else {
                                User user = aardvark.getUser(args[1]);
                                if (user != null) {
                                    dumpUser(user);
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: user  name = shows info for a user";
                    }
                });


        shell.add("dumpFeedLinkGraph",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            int count = 500;
                            if (args.length >= 2) {
                                count = Integer.parseInt(args[1]);
                            }
                            String regexp = null;
                            if (args.length >= 3) {
                                regexp = args[2];
                            }
                            dumpFeedLinkGraph(count, regexp);
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: dumpFeedLinkGraph [count] -  dumps the feed link graph";
                    }
                });

        shell.add("dumpStories",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 1 && args.length != 2) {
                                return getHelp();
                            } else {
                                int count = 500;
                                if (args.length >= 2) {
                                    count = Integer.parseInt(args[1]);
                                }
                                dumpStories(count);
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: dumpStories [count]- dump xml description of stories, suitable for the dashboard similator";
                    }
                });



        shell.add("addStarredFeed",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 3) {
                                getHelp();
                            } else {
                                User user = aardvark.getUser(args[1]);
                                if (user != null) {
                                    String surl = args[2];
                                    aardvark.addUserFeed(user, surl, Attention.Type.STARRED_FEED);
                                } else {
                                    return "Can't find user " + args[1];
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: addStarredFeed user  url = adds a starred item feed to a user";
                    }
                });


        shell.add("qe",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String query = sutils.stuff(args, 1);
                        List<Scored<Item>> items = dataStore.query(query, sutils.getHits(), 
                                new TypeFilter(Item.ItemType.BLOGENTRY));
                        for (Scored<Item> item : items) {
                            System.out.printf("%.3f ", item.getScore());
                            dumpItem(item.getItem());
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "Runs a query";
                    }
                });
        shell.add("recommend",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2 && args.length != 3) {
                                getHelp();
                            } else {
                                User user = aardvark.getUser(args[1]);
                                int count = args.length >= 3 ? Integer.parseInt(args[2]) : 20;
                                if (user != null) {
                                    recommend(user, count);
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: recommend  name = recommendations for a user";
                    }
                });

        shell.add("recommendFeed",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2 && args.length != 3) {
                                getHelp();
                            } else {
                                User user = aardvark.getUser(args[1]);
                                int count = args.length >= 3 ? Integer.parseInt(args[2]) : 20;
                                if (user != null) {
                                    SyndFeed feed = aardvark.getRecommendedFeed(user, count);
                                    if (feed != null) {
                                        feed.setLink("http://tastekeeper.com/feed");
                                        SyndFeedOutput output = new SyndFeedOutput();
                                        //feed.setFeedType("atom_1.0");
                                        feed.setFeedType("rss_2.0");
                                        // feed.setLink();
                                        String feedXML = output.outputString(feed);
                                        System.out.println(feedXML);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: recommend  name = recommendations for a user";
                    }
                });


        shell.add("feed",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) throws Exception {
                        if (args.length != 2) {
                            dumpAllFeeds();
                        } else {
                            String key = args[1];
                            Item item = dataStore.getItem(key);
                            if (item != null && item.getType() == ItemType.FEED) {
                                dumpFeed(item);
                            }
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: feed  id = shows info for a feed";
                    }
                });



        shell.add("feeds",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] arg1) {
                        try {
                            dumpAllFeeds();
                        } catch (Exception e) {
                            System.out.println("Error " + e);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "shows the current feeds";
                    }
                });

        shell.add("entries",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] arg1) {
                        try {
                            dumpLastEntries();
                        } catch (Exception e) {
                            System.out.println("Error " + e);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "dumps the entries added in the last 24 hours";
                    }
                });

        shell.add("entryTitles",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] arg1) {
                        try {
                            dumpEntryTitles(10000);
                        } catch (Exception e) {
                            System.out.println("Error " + e);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "dumps 10,000 entries";
                    }
                });

        shell.add("enrollUser",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        if (args.length != 2) {
                            return "Usage: addUser user-id";
                        }
                        try {
                            String id = args[1];
                            if (aardvark.getUser(id) != null) {
                                return "user " + id + " already exists";
                            } else {
                                User user = aardvark.enrollUser(id);
                                return "User " + user.getKey() + " created.";
                            }
                        } catch (Exception e) {
                            System.out.println("Error " + e);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "Enrolls a new user in the database";
                    }
                });

        shell.add("astats",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length > 2) {
                                return "Usage: astats";
                            }

                            System.out.println(aardvark.getStats());
                        } catch (Exception e) {
                            System.out.println("Error " + e);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "shows the current stats";
                    }
                });

        Thread t = new Thread() {

            public void run() {
                shell.run();
                shell = null;
            }
        };
        t.start();
    }

    private void dumpAllUsers() throws AuraException, RemoteException {
        for (Item item : dataStore.getAll(ItemType.USER)) {
            dumpUser((User) item);
        }
    }

    private void dumpUser(User user) throws AuraException, RemoteException {
        dumpItem(user);
    }

    private void recommend(User user, int count) throws AuraException, RemoteException {
        SyndFeed feed = aardvark.getRecommendedFeed(user, count);
        System.out.println("Feed " + feed.getTitle());
        for (Object syndEntryObject : feed.getEntries()) {
            SyndEntry syndEntry = (SyndEntry) syndEntryObject;
            String title = syndEntry.getTitle();
            String link = syndEntry.getLink();
            Date date = syndEntry.getPublishedDate();
            String sdate = "";
            if (date != null) {
                sdate = "(" + date.toString() + ")";
            }
            System.out.printf("  %s from %s %s\n", title, link, sdate);
        }

    }

    private void dumpAllFeeds() throws AuraException, RemoteException {
        List<Item> feedItems = dataStore.getAll(ItemType.FEED);
        List<Scored<Item>> scoredItems = new ArrayList();

        for (Item feed : feedItems) {
            AttentionConfig ac = new AttentionConfig();
            ac.setTargetKey(feed.getKey());
            scoredItems.add(new Scored<Item>(feed, dataStore.getAttentionCount(ac)));
        }

        long numFeeds = 0;

        Collections.sort(scoredItems, ScoredComparator.COMPARATOR);

        for (Scored<Item> scoredItem : scoredItems) {
            dumpScoredItem(scoredItem);
            numFeeds++;
        }
        System.out.println("Dumped " + numFeeds + " feeds");
    }

    private void dumpFeedLinkGraph(int topN, String regexp) throws AuraException, RemoteException, IOException {
        double MIN_WIDTH = .5;
        double MIN_HEIGHT = .3;
        double RANGE_WIDTH = 3;
        double RANGE_HEIGHT = 2;
        PrintWriter out = new PrintWriter("feedGraph.dot");
        out.println("digraph Feeds {");
        List<Item> feedItems = dataStore.getAll(ItemType.FEED);

        List<Scored<Item>> scoredItems = new ArrayList();
        for (Item feed : feedItems) {
            if (regexp == null || feed.getKey().matches(regexp)) {
                AttentionConfig ac = new AttentionConfig();
                ac.setTargetKey(feed.getKey());
                scoredItems.add(new Scored<Item>(feed, dataStore.getAttentionCount(ac)));
            }
        }

        if (scoredItems.size() == 0) {
            return;
        }

        Collections.sort(scoredItems, ScoredComparator.COMPARATOR);
        Collections.reverse(scoredItems);
        if (scoredItems.size() > topN) {
            scoredItems = scoredItems.subList(0, topN);
        }

        Set<String> validKeys = new HashSet();
        for (Scored<Item> item : scoredItems) {
            validKeys.add(item.getItem().getKey());
        }

        // recalculate the scores with only the valid keys
        List<Scored<Item>> prunedScoredItems = new ArrayList();
        for (Scored<Item> item : scoredItems) {
            Item tgt = item.getItem();
            int actualInputLinks = 0;
            AttentionConfig ac = new AttentionConfig();
            ac.setTargetKey(tgt.getKey());
            for (Attention attn : dataStore.getAttention(ac)) {
                if (attn.getType() == Attention.Type.LINKS_TO) {
                    Item src = dataStore.getItem(attn.getSourceKey());
                    if (src != null && validKeys.contains(src.getKey())) {
                        actualInputLinks++;
                    }
                }
            }
            if (actualInputLinks > 0) {
                prunedScoredItems.add(new Scored(tgt, actualInputLinks));
            }
        }

        Collections.sort(prunedScoredItems, ScoredComparator.COMPARATOR);
        Collections.reverse(prunedScoredItems);
        double maxScore = prunedScoredItems.get(0).getScore();
        double minScore = prunedScoredItems.get(prunedScoredItems.size() - 1).getScore();
        double rangeScore = maxScore - minScore;

        for (Scored<Item> item : prunedScoredItems) {
            Item tgt = item.getItem();
            AttentionConfig ac = new AttentionConfig();
            ac.setTargetKey(tgt.getKey());
            for (Attention attn : dataStore.getAttention(ac)) {
                if (attn.getType() == Attention.Type.LINKS_TO) {
                    Item src = dataStore.getItem(attn.getSourceKey());
                    if (src != null && validKeys.contains(src.getKey())) {
                        out.println("   " + formatNameForGraphviz(src.getName()) + " -> " + formatNameForGraphviz(tgt.getName()));
                    }
                }
            }
            double width = MIN_WIDTH + ((item.getScore() - minScore) / rangeScore) * RANGE_WIDTH;
            double height = MIN_HEIGHT + ((item.getScore() - minScore) / rangeScore) * RANGE_HEIGHT;
            out.printf("%s [width=%f height=%f]\n",
                    formatNameForGraphviz(tgt.getName()), width, height);
        }
        out.println("}");
        out.close();
    }

    private String formatNameForGraphviz(String name) {
        name = name.replaceAll("\"", "");
        name = name.replaceAll("\\s+", " ");
        return "\"" + name + "\"";
    }

    private void dumpLastEntries() throws AuraException, RemoteException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -2);
        Date date = cal.getTime();
        DBIterator<Item> iter = dataStore.getItemsAddedSince(ItemType.BLOGENTRY, date);
        try {
            while (iter.hasNext()) {
                Item item = iter.next();
                BlogEntry entry = new BlogEntry(item);
                System.out.println(entry.getKey());
                System.out.println(" " + entry.getName());
            }

        } finally {
            iter.close();
        }
    }

    private void dumpItem(Item item) throws AuraException, RemoteException {
        if (item == null) {
            System.out.println("null");
        } else {
            AttentionConfig ac = new AttentionConfig();
            ac.setTargetKey(item.getKey());
            System.out.printf(" %d %s\n", dataStore.getAttentionCount(ac),
                    item.getKey());
        }
    }

    private void dumpScoredItem(Scored<Item> scoredItem) throws AuraException, RemoteException {
        if (scoredItem == null) {
            System.out.println("null");
        } else {
            System.out.printf(" %.0f %s\n", scoredItem.getScore(),
                    scoredItem.getItem().getKey());
        }
    }

    private void dumpFeed(Item feedItem) throws AuraException, RemoteException {
        dumpItem(feedItem);
        BlogFeed feed = new BlogFeed(feedItem);
        System.out.println("   Pulls  : " + feed.getNumPulls());
        System.out.println("   Errors : " + feed.getNumErrors());
        System.out.println("   Authority: " + feed.getAuthority());
        AttentionConfig ac = new AttentionConfig();
        ac.setTargetKey(feedItem.getKey());
        DBIterator<Attention> it = dataStore.getAttentionIterator(ac);
        try {
            while (it.hasNext()) {
                Attention attn = it.next();
                System.out.println("   " + attn);
            }
        } finally {
            it.close();
        }
    }

    

    private void dumpEntryTitles(int count) {
        try {
            DBIterator<Item> iter = dataStore.getItemsAddedSince(ItemType.BLOGENTRY, new Date(0));

            try {
                while (count-- > 0 && iter.hasNext()) {
                    Item item = iter.next();
                    BlogEntry entry = new BlogEntry(item);
                    System.out.println(entry.getName());
                }
            } finally {
                iter.close();
            }
        } catch (AuraException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        } catch (RemoteException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        }
    }

    private void dumpStories(int count) {
        try {
            DBIterator<Item> iter = dataStore.getItemsAddedSince(ItemType.BLOGENTRY, new Date(0));

            try {
                System.out.println("<stories>");
                while (count-- > 0 && iter.hasNext()) {
                    Item item = iter.next();
                    BlogEntry entry = new BlogEntry(item);
                    dumpStory(entry);
                }
                System.out.println("</stories>");
            } finally {
                iter.close();
            }
        } catch (AuraException ex) {
            logger.severe("dumpStories " + ex);
        } catch (RemoteException ex) {
            logger.severe("dumpStories " + ex);
        }
    }

    void dumpStory(BlogEntry entry) throws AuraException, RemoteException {
        Item ifeed = dataStore.getItem(entry.getFeedKey());
        BlogFeed feed = new BlogFeed(ifeed);
        System.out.println("    <story score =\"1.0\">");

        dumpTag("        ", "source", feed.getName());
        dumpTag("        ", "imageUrl", feed.getImage());
        dumpTag("        ", "url", entry.getKey());
        dumpTag("        ", "title", entry.getTitle());
        if (entry.getContent() != null) {
            System.out.println("        <description>" + excerpt(filterHTML(entry.getContent()), 100) + "</description>");
        }

        List<Tag> tags = entry.getTags();
        if (tags.size() == 0) {
            tags = feed.getTags();
        }
        for (Tag tag : tags) {
            System.out.println("        <class score=\"1.0\">" + filterTag(tag.getName()) + "</class>");
        }
        System.out.println("    </story>");
    }

    private void dumpTag(String indent, String tag, String value) {
        if (value != null) {
            value = filterTag(value);
            System.out.println(indent + "<" + tag + ">" + value + "</" + tag + ">");
        }
    }

    private String filterTag(String s) {
        s = s.replaceAll("[^\\p{ASCII}]", "");
        s = s.replaceAll("\\&", "&amp;");
        s = s.replaceAll("\\<", "&lt;");
        s = s.replaceAll("\\>", "&gt;");

        return s;
    }

    private String filterHTML(String s) {
        s = detag(s);
        s = deentity(s);
        s = s.replaceAll("[^\\p{ASCII}]", "");
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("[\\<\\>\\&]", " ");
        return s;
    }

    private String detag(String s) {
        return s.replaceAll("\\<.*?\\>", "");
    }

    private String deentity(String s) {
        return s.replaceAll("\\&[a-zA-Z]+;", " ");
    }

    private String excerpt(String s, int maxWords) {
        StringBuilder sb = new StringBuilder();
        String[] words = s.split("\\s+");
        for (int i = 0; i < maxWords && i < words.length; i++) {
            sb.append(words[i] + " ");
        }

        if (maxWords < words.length) {
            sb.append("...");
        }
        return sb.toString().trim();
    }


    /**
     * Stops crawling the feeds
     */
    public void stop() {
        if (shell != null) {
            shell.close();
        }
    }

    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        aardvark = (Aardvark) ps.getComponent(PROP_AARDVARK);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        logger = ps.getLogger();
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    @ConfigComponent(type = Aardvark.class)
    public final static String PROP_AARDVARK = "aardvark";
    @ConfigComponent(type = StatService.class)
    public final static String PROP_STAT_SERVICE = "statService";
}
