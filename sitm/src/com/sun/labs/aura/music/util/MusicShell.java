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

package com.sun.labs.aura.music.util;

/**
 *
 * @author plamere
 */
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.Recommendation;
import com.sun.labs.aura.music.RecommendationSummary;
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.music.crawler.Crawler;
import com.sun.labs.aura.music.crawler.ListenerCrawler;
import com.sun.labs.aura.music.crawler.TagCrawler;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ShellUtils;
import com.sun.labs.aura.service.StatService;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.aura.util.WordCloud;
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

    private TagCrawler tagCrawler;
    private Crawler artistCrawler;
    private ListenerCrawler listenerCrawler;
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
        try {
            sutils = new ShellUtils(shell, musicDatabase.getDataStore(), statService);
        } catch (AuraException ex) {
            System.out.println("Can't connect to datastore");
        }

        shell.add("qartist", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String qname = sutils.stuff(args, 1);
                List<Scored<Artist>> items = musicDatabase.artistSearch(qname,
                        sutils.getHits());
                for (Scored<Artist> item : items) {
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
                    List<Scored<Item>> simItems = musicDatabase.getDataStore().findSimilar(artist.getKey(),
                            new SimilarityConfig(sutils.getHits(),
                            new TypeFilter(ItemType.ARTIST)));
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
                    List<Scored<Item>> simItems = musicDatabase.getDataStore().findSimilar(artist.getKey(),
                            new SimilarityConfig(Artist.FIELD_SOCIAL_TAGS, sutils.getHits(), new TypeFilter(ItemType.ARTIST)));
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
                    List<Scored<Item>> simItems = musicDatabase.getDataStore().findSimilar(artist.getKey(),
                            new SimilarityConfig(Artist.FIELD_BIO_TAGS, sutils.getHits(), new TypeFilter(ItemType.ARTIST)));
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
                    WordCloud tags = musicDatabase.getDataStore().getTopTerms(artist.getKey(), Artist.FIELD_SOCIAL_TAGS, sutils.getHits());
                    System.out.println("Distinctive tags for " + artist.getName());
                    sutils.dumpCloud(tags);
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
                    List<Tag> tags = artist.getTags(Artist.TagType.SOCIAL);
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
                        List<Scored<String>> results = musicDatabase.getDataStore().explainSimilarity(artist1.getKey(), artist2.getKey(), new SimilarityConfig(sutils.getHits()));
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

        shell.add("artistAdd", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length != 2) {
                    return getHelp();
                } else {
                    String mbaid = args[1];
                    artistCrawler.add(mbaid);
                }
                return "";
            }

            public String getHelp() {
                return "artistAdd mbaid";
            }
        });

        shell.add("artistUpdate", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length <= 1) {
                    return getHelp();
                } else {
                    String qname = sutils.stuff(args, 1);
                    Artist artist = findArtist(qname);
                    if (artist != null) {
                        artistCrawler.update(artist.getKey());
                    } else {
                        System.out.println("Can't find " + qname);
                    }
                }
                return "";
            }

            public String getHelp() {
                return "artistUpdate artist name - force the crawl of an artist";
            }
        });

        shell.add("artistTags", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length >= 1) {
                    String qname = sutils.stuff(args, 1);
                    Artist artist = findArtist(qname);
                    if (artist != null) {
                        List<Tag> tags = artist.getTags(Artist.TagType.SOCIAL);
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


        shell.add("addListener", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String name = args[1];
                String lastfmName = args.length > 2 ? args[2] : null;
                String pandoraName = args.length > 3 ? args[3] : null;
                Listener l = musicDatabase.getListener(name);
                if (l == null) {
                    l = musicDatabase.enrollListener(name);
                }
                if (lastfmName != null) {
                    l.setLastFmName(lastfmName);
                }

                if (pandoraName != null) {
                    l.setPandoraName(pandoraName);
                }
                musicDatabase.updateListener(l);
                return "";
            }

            public String getHelp() {
                return "Adds a listener";
            }
        });

        shell.add("listenerUpdate", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    Thread t = new Thread() {

                        public void run() {
                            try {
                                listenerCrawler.crawlAllListeners();
                            } catch (Exception ex) {
                                System.out.println("Trouble crawling listeners");
                            }
                        }
                    };
                    t.start();
                } else {
                    String listenerID = args[1];
                    Listener listener = musicDatabase.getListener(listenerID);
                    if (listener != null) {
                        listenerCrawler.crawlListener(listener, true);
                    } else {
                        System.out.println("Can't find listener " + listenerID);
                    }
                }
                return "";
            }

            public String getHelp() {
                return "updates the info for a listener";
            }
        });

        shell.add("fsl", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String listenerID = args[1];
                Listener listener = musicDatabase.getListener(listenerID);
                if (listener != null) {
                    System.out.println("Finding similar for " + listener.getKey());
                    List<Scored<Item>> simItems = musicDatabase.getDataStore().findSimilar(listener.getKey(),
                            new SimilarityConfig(Listener.FIELD_AGGREGATED_PLAY_HISTORY,
                                sutils.getHits(), new TypeFilter(ItemType.USER)));
                    sutils.dumpScoredItems(simItems);
                    return "";
                } else {
                    return "Can't find listener " + listenerID;
                }
            }

            public String getHelp() {
                return "find similar artist by name using just their social tags";
            }
        });

        shell.add("findListenersForArtist", new CommandInterface() {

            @Override
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String artistID = args[1];
                List<Scored<Listener>> lL = musicDatabase.getListenersForArtist(artistID, 10);
                if (lL.size() > 0) {
                    for (Scored<Listener> sL : lL) {
                        System.out.println("    "+sL.getItem().getLastFmName()+"      "+sL.getScore());
                    }
                } else {
                    System.out.println("    No listeners found.");
                }
                return "";
            }

            @Override
            public String getHelp() {
                return "find the top listeners for an artist";
            }
        });

        shell.add("rec", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length != 3) {
                    return "Usage: rec listener rtype";
                } else {
                    String listenerID = args[1];
                    RecommendationType rtype = null;
                    rtype = getRecommendationType(args[2]);
                    if (rtype == null) {
                        return "Can't find rectype " + rtype;
                    }

                    Listener listener = musicDatabase.getListener(listenerID);
                    if (listener == null) {
                        return "Can't find listener " + listenerID;
                    }

                    RecommendationSummary rs = rtype.getRecommendations(listener.getKey(), sutils.getHits(), null);

                    System.out.println(rs.getExplanation());
                    for (Recommendation r : rs.getRecommendations()) {
                        Artist artist = musicDatabase.artistLookup(r.getId());
                        System.out.printf(" %.2f  %s\n", r.getScore(), artist.getName());
                        System.out.printf("    %s\n", scoredListToString(r.getExplanation()));
                    }
                    return "";
                }
            }

            public String getHelp() {
                return "gets recommendations for a listener";
            }
        });



        shell.add("rectypes", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                for (RecommendationType rtype : musicDatabase.getArtistRecommendationTypes()) {
                    System.out.println("    " + rtype.getName() + " - " + rtype.getDescription());
                }
                return "";
            }

            public String getHelp() {
                return "shows available rec types";
            }
        });


        shell.add("tagFindSimilar", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length > 1) {
                    String qname = sutils.stuff(args, 1);
                    ArtistTag artistTag = findArtistTag(qname);
                    if (artistTag != null) {
                        List<Scored<Item>> simItems = musicDatabase.getDataStore().findSimilar(artistTag.getKey(),
                                new SimilarityConfig(ArtistTag.FIELD_TAGGED_ARTISTS, sutils.getHits(),
                                new TypeFilter(ItemType.ARTIST_TAG)));
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
                    List<Item> itemTags = musicDatabase.getDataStore().getAll(ItemType.ARTIST_TAG);
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
                            Item artist = musicDatabase.getDataStore().getItem(tag.getName());
                            if (artist != null) {
                                System.out.printf("%d %s %s\n", tag.getCount(), tag.getName(), artist.getName());
                            }
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
        List<Scored<Artist>> items = musicDatabase.artistSearch(qname,
                1);
        if (items.size() > 0) {
            return items.get(0).getItem();
        }
        return null;
    }

    private ArtistTag findArtistTag(String qname) throws AuraException, RemoteException {
        List<Scored<ArtistTag>> items = musicDatabase.artistTagSearch(qname,
                1);
        if (items.size() > 0) {
            return items.get(0).getItem();
        }
        return null;
    }

    private void dumpAll(ItemType type) throws AuraException, RemoteException {
        List<Item> items = musicDatabase.getDataStore().getAll(type);
        int count = 0;
        for (Item item : items) {
            System.out.printf("%d %s %s\n", ++count, item.getKey(), item.getName());
        }
    }

    private void fixupArtists() throws AuraException, RemoteException {
        List<Item> artists = musicDatabase.getDataStore().getAll(ItemType.ARTIST);
        for (Item item : artists) {
            Artist artist = new Artist(item);
            repairTags(artist);
        }
    }

    private void repairTags(Artist artist) throws AuraException, RemoteException {
        List<Tag> tags = artist.getTags(Artist.TagType.SOCIAL);
        if (tags.size() > 0 && tags.get(0).getCount() == 101) {
            System.out.println("Fixing " + artist.getName());
            for (Tag tag : tags) {
                int c = tag.getCount() - 1;
                int score = c * c + 1;
                artist.setTag(Artist.TagType.SOCIAL, tag.getName(), score);
            }
            artist.flush(musicDatabase.getDataStore());
        } else {
            System.out.println("Skipping " + artist.getName());
        }
    }

    private RecommendationType getRecommendationType(String rtypeName) {
        for (RecommendationType rtype : musicDatabase.getArtistRecommendationTypes()) {
            if (rtype.getName().equals(rtypeName)) {
                return rtype;
            }
        }
        return null;
    }

    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        tagCrawler = (TagCrawler) ps.getComponent(PROP_TAG_CRAWLER);
        artistCrawler = (Crawler) ps.getComponent(PROP_ARTIST_CRAWLER);
        listenerCrawler = (ListenerCrawler) ps.getComponent(PROP_LISTENER_CRAWLER);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        try {
            musicDatabase = new MusicDatabase(ps.getConfigurationManager());
        } catch (AuraException ex) {
            throw new PropertyException(ex, "MusicShell", ps.getInstanceName(), "Can't create music database");
        }
    }

    private String scoredListToString(List<Scored<String>> list) {
        StringBuilder sb = new StringBuilder();
        for (Scored<String> ss : list) {
            sb.append(String.format("(%s,%.3f)", ss.getItem(), ss.getScore()));
        }
        return sb.toString().trim();
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
    @ConfigComponent(type = ListenerCrawler.class)
    public final static String PROP_LISTENER_CRAWLER = "listenerCrawler";
    @ConfigComponent(type = Crawler.class)
    public final static String PROP_ARTIST_CRAWLER = "artistCrawler";
}
