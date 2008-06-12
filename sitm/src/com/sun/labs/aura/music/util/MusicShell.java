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
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.crawler.TagCrawler;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ShellUtils;
import com.sun.labs.aura.util.StatService;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the set of feed crawling threads
 * @author plamere
 */
public class MusicShell implements AuraService, Configurable {

    private DataStore dataStore;
    private TagCrawler tagCrawler;
    private CommandInterpreter shell;
    private StatService statService;
    private ShellUtils sutils;
    private MusicDatabase musicDatabase;
    private static Comparator<Tag> FREQ_SORT = new Comparator<Tag>() {

        public int compare(Tag o1, Tag o2) {
            return o1.getCount() - o2.getCount();
        }
    };

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
                            Artist.FIELD_SOCIAL_TAGS, sutils.getHits(), new TypeFilter(ItemType.ARTIST));
                    sutils.dumpScoredItems(simItems);
                    return "";
                } else {
                    return "Can't find artist " + qname;
                }
            }

            public String getHelp() {
                return "find similar artist by name using just their social tags";
            }
        });

        shell.add("fixupArtists", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                fixupArtists();
                return "";
            }

            public String getHelp() {
                return "repairs artists in the database";
            }
        });

        shell.add("fsab", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                Artist artist = findArtist(qname);
                if (artist != null) {
                    System.out.println("Finding similar for " + artist.getName());
                    List<Scored<Item>> simItems = dataStore.findSimilar(artist.getKey(),
                            Artist.FIELD_BIO_TAGS, sutils.getHits(), new TypeFilter(ItemType.ARTIST));
                    sutils.dumpScoredItems(simItems);
                    return "";
                } else {
                    return "Can't find artist " + qname;
                }
            }

            public String getHelp() {
                return "find similar artist by name using just their bio tags";
            }
        });

        shell.add("distinctiveTags", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                Artist artist = findArtist(qname);
                if (artist != null) {
                    List<Scored<String>> tags = dataStore.getTopTerms(artist.getKey(), Artist.FIELD_SOCIAL_TAGS, sutils.getHits());
                    System.out.println("Distinctive tags for " + artist.getName());
                    sutils.dumpScored(tags);
                    return "";
                } else {
                    return "Can't find artist " + qname;
                }
            }

            public String getHelp() {
                return "show distinctive tags for the artist";
            }
        });

        shell.add("frequentTags", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                Artist artist = findArtist(qname);
                if (artist != null) {
                    List<Tag> tags = artist.getSocialTags();
                    System.out.println("Frequent tags for " + artist.getName());
                    sutils.dumpTags(tags);
                    return "";
                } else {
                    return "Can't find artist " + qname;
                }
            }

            public String getHelp() {
                return "show distinctive tags for the artist";
            }
        });

        shell.add("explainArtistSimilarity", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 3) {
                    Artist artist1 = findArtist(args[1]);
                    Artist artist2 = findArtist(args[2]);
                    if (artist1 != null && artist2 != null) {
                        List<Scored<String>> results = dataStore.explainSimilarity(artist1.getKey(), artist2.getKey(), sutils.getHits());
                        sutils.dumpScored(results);
                        return "";
                    } else {
                        return "Can't find artist";
                    }
                } else {
                    return getHelp();
                }
            }

            public String getHelp() {
                return "explain why 2 artists are similar";
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

        shell.add("artistTags", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length >= 1) {
                    String qname = sutils.stuff(args, 1);
                    Artist artist = findArtist(qname);
                    if (artist != null) {
                        List<Tag> tags = artist.getSocialTags();
                        for (Tag tag : tags) {
                            System.out.println(tag.getName());
                        }
                    } else {
                        System.out.println("Can't find " + qname);
                    }
                }
                return "";
            }

            public String getHelp() {
                return "Shows the tags for an artist";
            }
        });

        shell.add("tagDiscover", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                tagCrawler.discoverArtistTags();
                return "";
            }

            public String getHelp() {
                return "crawls the artists for new tags and adds them to the database";
            }
        });


        shell.add("tagUpdate", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    tagCrawler.updateArtistTags();
                } else {
                    String qname = sutils.stuff(args, 1);
                    ArtistTag artistTag = findArtistTag(qname);
                    if (artistTag != null) {
                        tagCrawler.updateSingleTag(artistTag);
                    }
                }
                return "";
            }

            public String getHelp() {
                return "updates the info for a tag or all tags";
            }
        });

        shell.add("tagSpawnUpdater", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                int count = 100;
                if (args.length > 1) {
                    count = Integer.parseInt(args[1]);
                }
                final int max = count;

                Thread t = new Thread() {

                    @Override
                    public void run() {
                        try {
                            tagCrawler.updateArtistTags();
                            System.out.println("Done with artist tag update");
                        } catch (Exception ex) {
                            System.out.println("Trouble collecting tags");
                        }
                    }
                };
                t.start();
                return "";
            }

            public String getHelp() {
                return "updates the info for a tag or all tags";
            }
        });


        shell.add("tagFindSimilar", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length > 1) {
                    String qname = sutils.stuff(args, 1);
                    ArtistTag artistTag = findArtistTag(qname);
                    if (artistTag != null) {
                        List<Scored<Item>> simItems = dataStore.findSimilar(artistTag.getKey(),
                                ArtistTag.FIELD_TAGGED_ARTISTS, sutils.getHits(),
                                new TypeFilter(ItemType.ARTIST_TAG));
                        sutils.dumpScoredItems(simItems);
                    }
                }
                return "";
            }

            public String getHelp() {
                return "finds similar tags";
            }
        });

        shell.add("tagShow", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    List<Item> itemTags = dataStore.getAll(ItemType.ARTIST_TAG);
                    List<ArtistTag> allTags = new ArrayList();

                    for (Item item : itemTags) {
                        allTags.add(new ArtistTag(item));
                    }

                    Collections.sort(allTags, ArtistTag.POPULARITY);
                    Collections.reverse(allTags);

                    int index = 0;
                    for (ArtistTag artistTag : allTags) {
                        System.out.printf("%d %f %s\n", ++index, artistTag.getPopularity(), artistTag.getName());
                    }
                } else {
                    String qname = sutils.stuff(args, 1);
                    ArtistTag artistTag = findArtistTag(qname);
                    if (artistTag != null) {
                        sutils.dumpItemFull(artistTag.getItem());
                    }
                }
                return "";
            }

            public String getHelp() {
                return "shows all tags, or dumps the content of a specific tag";
            }
        });

        shell.add("tagShowArtists", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length > 1) {
                    String qname = sutils.stuff(args, 1);
                    ArtistTag artistTag = findArtistTag(qname);
                    if (artistTag != null) {
                        List<Tag> artistsAsTags = artistTag.getTaggedArtist();
                        Collections.sort(artistsAsTags, FREQ_SORT);
                        Collections.reverse(artistsAsTags);
                        for (Tag tag : artistsAsTags) {
                            Item artist = dataStore.getItem(tag.getName());
                            System.out.printf("%d %s %s\n", tag.getCount(), tag.getName(), artist.getName());
                        }
                    }
                }
                return "";
            }

            public String getHelp() {
                return "for a given tag, show the artists that have been tagged with the tag";
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

    private ArtistTag findArtistTag(String qname) throws AuraException, RemoteException {
        String query = "(aura-type = ARTIST_TAG) <AND> (aura-name <matches> \"*" + qname + "*\")";
        List<Scored<Item>> items = dataStore.query(query, "-score", sutils.getHits(), null);
        if (items.size() > 0) {
            return new ArtistTag(items.get(0).getItem());
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

    private void fixupArtists() throws AuraException, RemoteException {
        List<Item> artists = dataStore.getAll(ItemType.ARTIST);
        for (Item item : artists) {
            Artist artist = new Artist(item);
            repairTags(artist);
        }
    }

    private void repairTags(Artist artist) throws AuraException, RemoteException {
        List<Tag> tags = artist.getSocialTags();
        if (tags.size() > 0 && tags.get(0).getCount() == 101) {
            System.out.println("Fixing " + artist.getName());
            for (Tag tag : tags) {
                int c = tag.getCount() - 1;
                int score = c * c + 1;
                artist.setSocialTag(tag.getName(), score);
            }
            artist.flush(dataStore);
        } else {
            System.out.println("Skipping " + artist.getName());
        }
    }

    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        tagCrawler = (TagCrawler) ps.getComponent(PROP_TAG_CRAWLER);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        try {
            musicDatabase = new MusicDatabase(dataStore);
        } catch (AuraException ex) {
            throw new PropertyException(ex, "MusicShell", ps.getInstanceName(), "Can't create music database");
        }
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    @ConfigComponent(type = TagCrawler.class)
    public final static String PROP_TAG_CRAWLER = "tagCrawler";
    @ConfigComponent(type = StatService.class)
    public final static String PROP_STAT_SERVICE = "statService";
}
