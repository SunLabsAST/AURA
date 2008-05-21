/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.util;

/**
 *
 * @author plamere
 */
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ShellUtils;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the set of feed crawling threads
 * @author plamere
 */
public class MusicShell implements AuraService, Configurable {

    private DataStore dataStore;
    private CommandInterpreter shell;
    private StatService statService;
    private Logger logger;
    private ShellUtils sutils;

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        shell = new CommandInterpreter();
        shell.setPrompt("musicsh% ");
        sutils = new ShellUtils(shell, dataStore, statService);

        shell.add("qartist", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                String query = "(aura-type = artist) <AND> (aura-name <matches> \"*" + qname + "*\")";
                List<Scored<Item>> items = dataStore.query(query, "-score", sutils.getHits(), null);
                for (Scored<Item> item : items) {
                    System.out.printf("%.3f %s %s\n", item.getScore(), item.getItem().getKey(), item.getItem().getName());
                }
                return "";
            }

            public String getHelp() {
                return "queries for artists";
            }
        });

        shell.add("fsa", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                Artist artist = findArtist(qname);
                if (artist != null) {
                    System.out.println("Finding similar for " + artist.getName());
                    List<Scored<Item>> simItems = dataStore.findSimilar(artist.getKey(), sutils.getHits(), 
                            new TypeFilter(ItemType.ARTIST));
                    sutils.dumpScoredItems(simItems);
                    return "";
                } else {
                    return "Can't find artist " + qname;
                }
            }

            public String getHelp() {
                return "find similar artist by name";
            }
        });

        shell.add("fsaa", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                Artist artist = findArtist(qname);
                if (artist != null) {
                    System.out.println("Finding similar for " + artist.getName());
                    List<Scored<Item>> simItems = dataStore.findSimilar(artist.getKey(), sutils.getHits(), 
                            new TypeFilter(ItemType.ARTIST));
                    sutils.dumpScoredItems(simItems);
                    return "";
                } else {
                    return "Can't find artist " + qname;
                }
            }

            public String getHelp() {
                return "find similar artist by name using all fields";
            }
        });

        shell.add("fsa", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                Artist artist = findArtist(qname);
                if (artist != null) {
                    System.out.println("Finding similar for " + artist.getName());
                    List<Scored<Item>> simItems = dataStore.findSimilar(artist.getKey(), 
                            "socialTags", sutils.getHits(), new TypeFilter(ItemType.ARTIST));
                    sutils.dumpScoredItems(simItems);
                    return "";
                } else {
                    return "Can't find artist " + qname;
                }
            }

            public String getHelp() {
                return "find similar artist by name using just socia tags";
            }
        });



        shell.add("lartist", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length <= 1) {
                    dumpAll(ItemType.ARTIST);
                } else {
                    String qname = sutils.stuff(args, 1);
                    Artist artist = findArtist(qname);
                    if (artist != null) {
                        sutils.dumpItemFull(artist.getItem());
                    } else {
                        System.out.println("Can't find " + qname);
                    }
                }
                return "";
            }

            public String getHelp() {
                return "lartist [artist name] - list an artist (or all artists) ";
            }
        });


        Thread t = new Thread() {

            public void run() {
                shell.run();
                shell =
                        null;
            }
        };
        t.start();
    }

    /**
     * Stops crawling the feeds
     */
    public void stop() {
        if (shell != null) {
            shell.close();
        }

    }

    private Artist findArtist(String qname) throws AuraException, RemoteException {
        String query = "(aura-type = artist) <AND> (aura-name <matches> \"*" + qname + "*\")";
        List<Scored<Item>> items = dataStore.query(query, "-score", sutils.getHits(), null);
        if (items.size() > 0) {
            return new Artist(items.get(0).getItem());
        }
        return null;
    }

    private void dumpAll(ItemType type) throws AuraException, RemoteException {
        List<Item> items = dataStore.getAll(type);
        int count = 0;
        for (Item item : items) {
            System.out.printf("%d %s %s\n", ++count, item.getKey(), item.getName());
        }
    }

    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        statService =
                (StatService) ps.getComponent(PROP_STAT_SERVICE);
        logger =
                ps.getLogger();
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    @ConfigComponent(type = StatService.class)
    public final static String PROP_STAT_SERVICE = "statService";
}
