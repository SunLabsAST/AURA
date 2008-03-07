/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import com.sun.labs.aura.aardvark.Stats;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

/**
 * Manages the set of feed crawling threads
 * @author plamere
 */
public class AardvarkShell implements AuraService, Configurable {

    private Logger logger;
    private DataStore dataStore;
    private CommandInterpreter shell;
    private Aardvark aardvark;

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        shell = new CommandInterpreter();
        shell.setPrompt("aardv% ");

        shell.add("users",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            dumpAllUsers();
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "shows the current users";
                    }
                });

        shell.add("user",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2) {
                                dumpAllUsers();
                            } else {
                                Item item = dataStore.getItem(args[1]);
                                if (item != null && item instanceof User) {
                                    dumpUser((User) item);
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

        shell.add("addStarredFeed",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 3) {
                                getHelp();
                            } else {
                                Item item = dataStore.getItem(args[1]);
                                String surl = args[2];
                                aardvark.addUserFeed((User) item, surl, Attention.Type.STARRED_FEED);
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
                                Item item = dataStore.getItem(args[1]);
                                if (item != null && item instanceof User) {
                                    recommend((User) item);
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
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
                            if (item != null && item instanceof BlogFeed) {
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
        shell.add("getLastAttn",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci,
                            String[] args) {
                        try {
                            if ((args.length < 3) || (args.length > 4)) {
                                getHelp();
                            } else {
                                try {
                                    String userKey = args[1];
                                    int count = Integer.parseInt(args[2]);
                                    Attention.Type type = null;
                                    if (args.length == 4) {
                                        type = Attention.Type.valueOf(args[3]);
                                    }

                                    User u = (User) dataStore.getUser(userKey);
                                    SortedSet<Attention> attns = null;
                                /**
                                 * TODO: removed pending support from the datasore
                                 * for retrieving user-based attention data
                                 */
                                /*
                                if (type == null) {
                                attns = dataStore.getLastAttention(u, count);
                                } else {
                                attns = dataStore.getLastAttention(u, type, count);
                                }
                                for (Attention attn : attns) {
                                System.out.println(attn.getItemID() +
                                " " +
                                attn.getType().toString() +
                                " " +
                                new Date(attn.getTimeStamp()));
                                }
                                 */
                                } catch (NumberFormatException e) {
                                    System.out.println("Error parsing args");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "getLastAttn <userID> <count> [<type>]";
                    }
                });
        shell.add("stats",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] arg1) {
                        try {
                            Stats stats = aardvark.getStats();
                            System.out.println("Stats: " + stats);
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
    //TODO:
    // we can't get all users from the datastore right now

    /*
    Set<User> users = dataStore.getAll(User.class);
    for (User user : users) {
    dumpItem(user);
    }
     * */
    }

    private void dumpUser(User user) throws AuraException, RemoteException {
        dumpItem(user);
    }

    private void recommend(User user) throws AuraException, RemoteException {
        SyndFeed feed = aardvark.getRecommendedFeed(user);
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
        logger = ps.getLogger();
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
}
