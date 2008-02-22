/*
 *  Copyright Expression year is undefined on line 4, column 30 in Templates/Licenses/license-default.txt. Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.util;

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.AardvarkService;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.aardvark.Stats;
import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class Shell {

    private CommandInterpreter shell;
    private DataStore dataStore;
    private FeedCrawler feedCrawler;
    private Aardvark aardvark;

    public Shell(String configFile) throws IOException {
        initComponents(configFile);

        Logger rl = Logger.getLogger("");
        for (Handler h : rl.getHandlers()) {
            h.setFormatter(new LabsLogFormatter());
        }

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
                                URL url = new URL(surl);
                                aardvark.addUserFeed((User) item, url, Attention.Type.STARRED_FEED);
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

        shell.add("crawlFeed",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2) {
                                getHelp();
                            } else {
                                String key = args[1];
                                Item item = dataStore.getItem(key);
                                if (item != null && item instanceof BlogFeed) {
                                    feedCrawler.crawlFeed((BlogFeed) item);
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: crawlFeed id = crawls a feed ";
                    }
                });

        shell.add("crawlStart",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 1) {
                                getHelp();
                            } else {
                                ((AardvarkService) feedCrawler).start();
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: crawlStart";
                    }
                });

        shell.add("crawlStop",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 1) {
                                getHelp();
                            } else {
                                ((AardvarkService) feedCrawler).stop();
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: crawlStop";
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

        shell.add("dbExerciseWrite",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
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
    }

    public void go() {
        shell.run();
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

    private void dumpItem(Item item) throws AuraException, RemoteException {
        System.out.printf(" %d %s\n", dataStore.getAttentionForTarget(item).size(), item.getKey());
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

    public void initComponents(String configFile) throws IOException {
        URL cu = getClass().getResource(configFile);
        if (cu == null) {
            cu = (new File(configFile)).toURI().toURL();
        }
        ConfigurationManager cm = new ConfigurationManager(cu);
        aardvark = (Aardvark) cm.lookup("aardvark");
        dataStore = (DataStore) cm.lookup("dataStore");
        feedCrawler = (FeedCrawler) cm.lookup("feedCrawler");
    }

    public static void main(String[] args) {
        try {
            Shell shell = new Shell(args[0]);
            shell.go();
        } catch (IOException ex) {
            System.err.println("Can't run shell " + ex);
        }
    }
}
