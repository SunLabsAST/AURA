/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.minion.SearchEngineException;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class Shell {
    private CommandInterpreter shell;
    private ArtistCrawler crawler;
    private int numResults = 15;

    Shell(ArtistCrawler crawler) {
        shell = new CommandInterpreter();
        shell.setPrompt("sxsw% ");
        this.crawler = crawler;
        addCommands();
    }

    void go() {
        shell.run();
    }

    void close() {
    }

    void addCommands() {
        shell.add("resolveArtists", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                crawler.resolveArtists();
                return "";
            }

            public String getHelp() {
                return "resolves SXSW artists";
            }
        });

        shell.add("generatePages", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                crawler.generatePages();
                return "";
            }

            public String getHelp() {
                return "generates the HTML pages";
            }
        });

        shell.add("dumpTagTree", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                Tree tree = crawler.createTagTree();
                tree.dumpGraphviz("tagtree.dot", true);
                return "";
            }

            public String getHelp() {
                return "dumps the tag tree";
            }
        });

        shell.add("dumpArtistTree", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                Tree tree = crawler.createArtistTree(2000);
                tree.dumpGraphviz("artisttree.dot", true);
                return "";
            }

            public String getHelp() {
                return "dumps the artist tree";
            }
        });

        shell.add("go", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                crawler.doAll();
                return "";
            }

            public String getHelp() {
                return "do everthing necessary to create the catalog";
            }
        });

        shell.add("listTags", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                crawler.listTags();
                return "";
            }

            public String getHelp() {
                return "list all tags";
            }
        });

        shell.add("listArtistsByNameLength", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                List<Artist> artists = crawler.getAllArtists();
                Collections.sort(artists, Artist.NAME_LENGTH_SORT);
                Collections.reverse(artists);
                for (Artist artist : artists) {
                    System.out.println(artist.getName());
                }
                return "";
            }

            public String getHelp() {
                return "list all artists by their name length";
            }
        });

        shell.add("histArtistsByNameLength", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                List<Artist> artists = crawler.getAllArtists();
                Histogram hist = new Histogram();

                for (Artist artist : artists) {
                    hist.accum(artist.getName().length(), 1);
                }

                List<Scored<Integer>> hres = hist.getAll();

                for (Scored<Integer> sint : hres) {
                    System.out.printf("%d %.0f\n", sint.getItem(), sint.getScore());
                }
                return "";
            }

            public String getHelp() {
                return "get histo gram of name length";
            }
        });

        shell.add("pophot", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                List<Artist> artists = crawler.getAllArtists();
                Collections.sort(artists, Artist.POPULARITY_SORT);
                Collections.reverse(artists);
                float maxListeners = artists.get(0).getArtistInfo().getListeners();

                for (Artist artist : artists) {
                    if (artist.getArtistInfo().getListeners() > 0 && artist.getHotness() > 0) {
                        System.out.printf("%.4f %.4f %s\n", artist.getArtistInfo().getListeners() / maxListeners,
                                artist.getHotness(), artist.getName());
                    }
                }
                return "";
            }

            public String getHelp() {
                return "show the popularity and hotness of artists";
            }
        });

        shell.add("listArtistsByWordLength", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                List<Artist> artists = crawler.getAllArtists();
                Collections.sort(artists, Artist.WORD_LENGTH_SORT);
                Collections.reverse(artists);
                for (Artist artist : artists) {
                    System.out.println(artist.getName());
                }
                return "";
            }

            public String getHelp() {
                return "list all artists by their word length";
            }
        });

        shell.add("listArtists", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                crawler.listArtists();
                return "";
            }

            public String getHelp() {
                return "list all Artists";
            }
        });

        shell.add("echoQueryCheck", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                crawler.echoQueryCheck();
                return "";
            }

            public String getHelp() {
                return "echo the echonest querye";
            }
        });

        shell.add("index", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                crawler.indexArtists();
                crawler.indexTags();
                return "";
            }

            public String getHelp() {
                return "index all of the artists/tags";
            }
        });

        shell.add("findSimilarArtist", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    return "Usage: findsimilar artist name ";
                } else {
                    String query = mash(args, 1, args.length);
                    List<Scored<Artist>> results = crawler.searchArtist(query, 1);
                    if (results.size() == 1) {
                        Artist artist = results.get(0).getItem();
                        List<Scored<Artist>> fsResults = crawler.findSimilarArtist(artist, numResults);
                        for (Scored<Artist> r : fsResults) {
                            System.out.printf("%6.4f %s\n", r.getScore(), r.getItem().getName());
                        }
                    } else {
                        return "No match for " + query;
                    }
                }
                return "";
            }

            public String getHelp() {
                return "finds similar items";
            }
        });

        shell.add("searchArtist", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    return "Usage: search query ...";
                } else {
                    String query = mash(args, 1, args.length);
                    List<Scored<Artist>> results = crawler.searchArtist(query, numResults);
                    for (Scored<Artist> r : results) {
                        System.out.printf("%6.4f %s\n", r.getScore(), r.getItem().getName());
                    }
                }
                return "";
            }

            public String getHelp() {
                return "search the database";
            }
        });

        shell.add("findSimilarTag", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    return "Usage: findSimilarTag tag name ";
                } else {
                    String query = mash(args, 1, args.length);
                    List<Scored<String>> results = crawler.searchTag(query, 1);
                    if (results.size() == 1) {
                        String tag = results.get(0).getItem();
                        System.out.println("FST for " + tag);
                        List<Scored<String>> fsResults = crawler.findSimilarTag(tag, numResults);
                        for (Scored<String> r : fsResults) {
                            System.out.printf("%6.4f %s\n", r.getScore(), r.getItem());
                        }
                    } else {
                        return "No match for " + query;
                    }
                }
                return "";
            }

            public String getHelp() {
                return "finds similar items";
            }
        });

        shell.add("searchTag", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    return "Usage: search query ...";
                } else {
                    String query = mash(args, 1, args.length);
                    List<Scored<String>> results = crawler.searchTag(query, numResults);
                    for (Scored<String> r : results) {
                        System.out.printf("%6.4f %s\n", r.getScore(), r.getItem());
                    }
                }
                return "";
            }

            public String getHelp() {
                return "search the database";
            }
        });

        shell.add("search", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length == 1) {
                    return "Usage: search query ...";
                } else {
                    String query = mash(args, 1, args.length);
                    List<Scored<String>> results = crawler.search(query, numResults);
                    for (Scored<String> r : results) {
                        System.out.printf("%6.4f %s\n", r.getScore(), r.getItem());
                    }
                }
                return "";
            }

            public String getHelp() {
                return "search the database";
            }
        });
        
        shell.addAlias("findSimilarArtist", "fsa");
        shell.addAlias("findSimilarTag", "fst");
    }

    private String mash(String[] args, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append(args[i]);
            if (i < end - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }


    public static void main(String[] args) {
        try {
            ArtistCrawler crawler = new ArtistCrawler();
            Shell shell = new Shell(crawler);
            shell.go();
            System.exit(1);
        } catch (SearchEngineException ex) {
            System.out.println("Couldn't create crawler search engine" + ex);
        } catch (IOException ex) {
            System.out.println("Couldn't create crawler " + ex);
            System.exit(1);
        }
    }
}

class Histogram {

    private Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    public void set(Integer name, int value) {
        map.put(name, value);
    }

    public void accum(Integer name, int value) {
        Integer d = map.get(name);
        if (d == null) {
            d = Integer.valueOf(0);
        }
        map.put(name, d + value);
    }

    List<Scored<Integer>> getAll() {
        List<Scored<Integer>> results = new ArrayList<Scored<Integer>>();

        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            results.add(new Scored<Integer>(e.getKey(), e.getValue()));
        }
        Collections.sort(results, ScoredComparator.COMPARATOR);
        Collections.reverse(results);
        return results;
    }
}