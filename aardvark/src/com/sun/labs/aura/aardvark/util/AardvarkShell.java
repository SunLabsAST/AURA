/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.util;

/**
 *
 * @author plamere
 */
import com.sun.kt.search.FieldFrequency;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.cluster.Cluster;
import com.sun.labs.aura.cluster.ClusterElement;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.StatService;
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
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        shell = new CommandInterpreter();
        shell.setPrompt("aardv% ");

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

        shell.add("dumpTagFrequencies",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            int n = 50;
                            if (args.length > 1) {
                                n = Integer.parseInt(args[1]);
                            }
                            dumpTagFrequencies(n);
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: dumpTagFrequencies <n> - shows top n (default 50) tag frequencies for entries";
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


        shell.add("attn",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2) {
                                return "Usage: attn user";
                            } else {
                                User user = aardvark.getUser(args[1]);
                                SortedSet<Attention> attns = aardvark.getLastAttentionData(user, null, 100);
                                for (Attention attn : attns) {
                                    System.out.printf("%8s %s at %s\n", attn.getType(), attn.getTargetKey(), new Date(attn.getTimeStamp()));
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: attn user - show attention data for a user";
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


        shell.add("recommend",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2) {
                                getHelp();
                            } else {
                                User user = aardvark.getUser(args[1]);
                                if (user != null) {
                                    recommend(user);
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
                            if (args.length != 2) {
                                getHelp();
                            } else {
                                User user = aardvark.getUser(args[1]);
                                if (user != null) {
                                    SyndFeed feed = aardvark.getRecommendedFeed(user);
                                    if (feed != null) {
                                        SyndFeedOutput output = new SyndFeedOutput();
                                        feed.setFeedType("atom_1.0");
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

        shell.add("dbExerciseWrite",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) throws Exception {
                        try {
                            if (args.length == 2) {
                                long timeStamp = System.currentTimeMillis();
                                int count = Integer.parseInt(args[1]);
                                for (int i = 0; i < count; i++) {
                                    String key = "key:" + timeStamp + "-" + i;
                                    Item item = StoreFactory.newItem(Item.ItemType.BLOGENTRY, key, key);
                                    item = dataStore.putItem(item);
                                }
                            } else {
                                getHelp();
                            }
                        } catch (Exception e) {
                            System.out.println("Error " + e);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "dbExercise count - exercise the database by repeated fetching items";
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
        shell.add("stats",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length > 2) {
                                return "Usage: stats [prefix]";
                            }

                            String prefix = args.length == 2 ? args[1] : "";
                            String[] counters = statService.getCounterNames();
                            Arrays.sort(counters);
                            System.out.printf("%20s %8s %8s %8s\n", "Stat", "counter", "average", "per min");
                            System.out.printf("%20s %8s %8s %8s\n", "----", "-------", "-------", "-------");
                            for (String counter : counters) {
                                if (counter.startsWith(prefix)) {
                                    long count = statService.get(counter);
                                    double avg = statService.getAverage(counter);
                                    double avgPerMin = statService.getAveragePerMinute(counter);
                                    System.out.printf("%20s %8d %8.3f %8.3f\n", counter, count, avg, avgPerMin);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error " + e);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "shows the current stats";
                    }
                });
        shell.add("query",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String query = stuff(args, 1);
                        List<Scored<Item>> items = dataStore.query(query, 10, null);
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

        shell.add("fs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String key = args[1];
                        List<Scored<Item>> items = dataStore.findSimilar(key, 10, null);
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
        shell.add("ffs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String field = args[1];
                        String key = args[2];
                        List<Scored<Item>> items = dataStore.findSimilar(key, field, 10, null);
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

    private void recommend(User user) throws AuraException, RemoteException {
        SyndFeed feed = aardvark.getRecommendedFeed(user);
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
        Set<Item> feedItems = dataStore.getAll(ItemType.FEED);
        long numFeeds = 0;
        for (Item feedItem : feedItems) {
            dumpItem(feedItem);
            numFeeds++;
        }
        System.out.println("Dumped " + numFeeds + " feeds");
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
                System.out.println("Entry " + entry.getName());
                System.out.println("   " + entry.getSyndEntry().getPublishedDate());
            }

        } finally {
            iter.close();
        }
    }

    private void dumpItem(Item item) throws AuraException, RemoteException {
        System.out.printf(" %d %s\n", dataStore.getAttentionForTarget(item.getKey()).size(), item.getKey());
    }

    private void dumpFeed(Item feedItem) throws AuraException, RemoteException {
        dumpItem(feedItem);
        BlogFeed feed = new BlogFeed(feedItem);
        System.out.println("   Pulls  : " + feed.getNumPulls());
        System.out.println("   Errors : " + feed.getNumErrors());
    }

    private void dumpAttentionData(List<Attention> attentionData) throws AuraException, RemoteException {
        for (Attention attention : attentionData) {
            Item source = dataStore.getItem(attention.getSourceKey());
            Item target = dataStore.getItem(attention.getTargetKey());
            String type = attention.getType().toString();
            System.out.printf("   %s(%s) -- %s -- %s(%s)\n", source.getKey(), source.getName(),
                    type, target.getKey(), target.getName());
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

    private void dumpTagFrequencies(int n) {

        try {
            List<FieldFrequency> tagFreqs = dataStore.getTopValues("tag", n,
                    true);
            for (FieldFrequency ff : tagFreqs) {
                System.out.printf("%d %s\n", ff.getFreq(), ff.getVal().toString().trim());
            }
        } catch (AuraException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        } catch (RemoteException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        }
    }

    private String stuff(String[] args, int p) {
        StringBuilder sb = new StringBuilder();
        for (int i = p; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(' ');
        }
        return sb.toString();
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

class TagAccumulator {

    private Map<String, Tag> tags = new HashMap<String, Tag>();

    void add(String stag) {
        add(stag, 1);
    }

    void add(String stag, int count) {
        stag = stag.toLowerCase();
        Tag tag = tags.get(stag);
        if (tag == null) {
            tag = new Tag(stag, count);
            tags.put(tag.getName(), tag);
        } else {
            tag.accum(count);
        }
    }

    void add(Tag tag) {
        add(tag.getName(), tag.getCount());
    }

    void dump() {
        List<Tag> tagList = new ArrayList<Tag>(tags.values());
        Collections.sort(tagList);
        Collections.reverse(tagList);
        int which = 0;
        for (Tag tag : tagList) {
            System.out.printf(" %d %d %s\n", ++which, tag.getCount(), tag.getName());
        }
    }
}
