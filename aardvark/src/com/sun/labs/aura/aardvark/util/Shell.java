/*
 *  Copyright Expression year is undefined on line 4, column 30 in Templates/Licenses/license-default.txt. Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.util;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.Stats;
import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
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
    private ItemStore itemStore;
    private FeedCrawler feedCrawler;
    private Aardvark aardvark;

    public Shell() throws IOException {
        initComponents();

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
                                Item item = itemStore.get(args[1]);
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
                                Item item = itemStore.get(args[1]);
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
                                Item item = itemStore.get(args[1]);
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

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 2) {
                                dumpAllFeeds();
                            } else {
                                long id = Long.parseLong(args[1]);
                                Item item = itemStore.get(id);
                                if (item != null && item instanceof Feed) {
                                    dumpFeed((Feed) item);
                                }
                            }
                        } catch (NumberFormatException e) {
                            try {
                                Item item = itemStore.get(args[1]);
                                if (item != null && item instanceof Feed) {
                                    dumpFeed((Feed) item);
                                }
                            } catch (Exception ex) {
                                System.out.println("Error " + ex);
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
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
                                long id = Long.parseLong(args[1]);
                                Item item = itemStore.get(id);
                                if (item != null && item instanceof Feed) {
                                    feedCrawler.crawlFeed((Feed) item);
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
                                feedCrawler.start();
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
                                feedCrawler.stop();
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

        shell.add("aaStart",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 1) {
                                getHelp();
                            } else {
                                aardvark.startup();
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: aaStart";
                    }
                });

        shell.add("aaStop",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length != 1) {
                                getHelp();
                            } else {
                                aardvark.shutdown();
                            }
                        } catch (Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: aaStop";
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

        shell.add("dbExerciseRead",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length == 2) {
                                int count = Integer.parseInt(args[1]);
                                for (int i = 0; i < count; i++) {
                                    for (int j = 0; j < 1000; j++) {
                                        itemStore.get(j);
                                    }
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
        shell.add("dbExerciseWrite",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if (args.length == 2) {
                                long timeStamp = System.currentTimeMillis();
                                int count = Integer.parseInt(args[1]);
                                for (int i = 0; i < count; i++) {
                                    String key = "key:" + timeStamp + "-" + i;
                                    Item item = itemStore.newItem(Entry.class, key);
                                    item = itemStore.put(item);
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
                                    long userID = Long.parseLong(args[1]);
                                    int count = Integer.parseInt(args[2]);
                                    Attention.Type type = null;
                                    if (args.length == 4) {
                                        type =Attention.Type.valueOf(args[3]);
                                    }
                                    
                                    User u = (User) itemStore.get(userID);
                                    SortedSet<Attention> attns = null;
                                    if (type == null) {
                                        attns = itemStore.getLastAttention(u, count);
                                    } else {
                                        attns = itemStore.getLastAttention(u, type, count);
                                    }
                                    for (Attention attn : attns) {
                                        System.out.println(attn.getItemID() +
                                                " " +
                                                attn.getType().toString() +
                                                " " +
                                                new Date(attn.getTimeStamp()));
                                    }
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
        Set<User> users = itemStore.getAll(User.class);
        for (User user : users) {
            dumpItem(user);
        }
    }

    private void dumpUser(User user) throws AuraException, RemoteException {
        dumpItem(user);
        dumpAttentionData(itemStore.getAttentionData(user));
    }

    private void recommend(User user) throws AuraException {
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
        Set<Feed> feeds = itemStore.getAll(Feed.class);
        long numFeeds = 0;
        for (Feed feed : feeds) {
            dumpItem(feed);
            numFeeds++;
        }
        System.out.println("Dumped " + numFeeds + " feeds");
    }

    private void dumpItem(Item item) throws AuraException, RemoteException {
        System.out.printf(" %d %d %s\n", item.getID(), itemStore.getAttentionData(item).size(), item.getKey());
    }

    private void dumpFeed(Feed feed) throws AuraException, RemoteException {
        dumpItem(feed);
        System.out.println("   Pulls  : " + feed.getNumPulls());
        System.out.println("   Last   : " + feed.getLastPullTime());
        System.out.println("   Errors : " + feed.getNumErrors());
        dumpAttentionData(itemStore.getAttentionData(feed));
    }

    private void dumpAttentionData(List<Attention> attentionData) throws AuraException, RemoteException {
        for (Attention attention : attentionData) {
            Item user = itemStore.get(attention.getUserID());
            Item item = itemStore.get(attention.getItemID());
            String type = attention.getType().toString();
            System.out.printf("   %s(%d) %s(%d) %s(%d)\n", user.getKey(), user.getID(),
                    type, attention.getType().ordinal(), item.getKey(), item.getID());
        }

    }

    public void initComponents() throws IOException {
        ConfigurationManager cm = new ConfigurationManager();
        URL configFile = Aardvark.class.getResource("aardvarkConfig.xml");
        cm.addProperties(configFile);
        aardvark = (Aardvark) cm.lookup("aardvark");
        itemStore = (ItemStore) cm.lookup("itemStore");
        feedCrawler = (FeedCrawler) cm.lookup("feedCrawler");
    }

    public static void main(String[] args) {
        try {
            Shell shell = new Shell();
            shell.go();
        } catch (IOException ex) {
            System.err.println("Can't run shell " + ex);
        }
    }
}
