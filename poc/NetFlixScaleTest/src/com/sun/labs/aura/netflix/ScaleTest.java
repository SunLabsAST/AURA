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

package com.sun.labs.aura.netflix;

import com.sun.kt.search.Document;
import com.sun.kt.search.DocumentVector;
import com.sun.kt.search.Log;
import com.sun.kt.search.Result;
import com.sun.kt.search.ResultSet;
import com.sun.kt.search.SearchEngine;
import com.sun.kt.search.SearchEngineException;
import com.sun.kt.search.SearchEngineFactory;
import com.sun.kt.search.SimpleIndexer;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * A test harness for examining scalability of the
 * core tfidf recommender, using the netflixprize scaling data.
 *
 * @author plamere
 */
public class ScaleTest {

    private CommandInterpreter shell;
    private SearchEngine searchEngine;
    private String path;
    private Map<String, Movie> movieMap =
            new HashMap<String, Movie>();
    private float skimPercent = 1.0f;
    private int minRating = 0;
    private String simField = "movie.users";

    /** Creates a new instance of Main
     * @param path the path to the directory containing the netflixprize data
     * @throws java.io.IOException if an io error occurs while loading the data
     * @throws com.sun.kt.search.SearchEngineException  if the search engine cannot be allocated
     */
    public ScaleTest(String path, String indexDir, String engineConfig) throws SearchEngineException,
            IOException {
        this.path = path;
        String searchIndex = path + File.separator + indexDir;
        URL config = getClass().getResource(engineConfig);
        searchEngine =
                SearchEngineFactory.getSearchEngine(config, searchIndex,
                "music_search_engine");
        shell = new CommandInterpreter();
        shell.setPrompt("netflix% ");

        // make sure that we always shutdown the searchengine
        Runtime.getRuntime().
                addShutdownHook(new Thread() {

            @Override
            public void run() {
                close();
            }
        });

        loadMovies();
        addCommands();
    }

    /**
     * Closes and frees resources
     */
    private void close() {
        try {
            if(searchEngine != null) {
                System.out.println("closing the database");
                searchEngine.close();
                searchEngine = null;
            }
        } catch(SearchEngineException se) {
            System.out.println("Can't close search engine");
        }
    }

    private void loadMovies() throws IOException {
        BufferedReader reader =
                new BufferedReader(new FileReader(new File(path,
                "movie_titles.txt")));

        String line;

        while((line = reader.readLine()) != null) {
            String[] parms = line.split(",");
            if(parms.length >= 3) {
                String id = parms[0];
                String year = parms[1];
                String title = getRest(parms, 2);

                Movie movie = new Movie(id, title, 0);
                addMovie(movie);
            }
        }

        reader.close();
        System.out.printf("Added %d movies\n", movieMap.size());
    }

    /**
     *  Searches for a movie
     * @param query the search query
     * @param num the maximum number of results to return
     * @return the scored movie results
     */
    List<Scored<Movie>> movieSearch(String query, int num) {
        List<Scored<Movie>> list =
                new ArrayList<Scored<Movie>>(num);
        try {
            ResultSet rs =
                    searchEngine.search("(common.type = movie) <AND> (common.name <contains> \"" +
                    query + "\")", "-score", null);
            List<Result> results = rs.getResults(0, num);
            for(Result result : results) {
                list.add(new Scored<Movie>(getMovie(result.getDocument()),
                        result.getScore()));
            }
        } catch(SearchEngineException se) {
            System.out.println("Can't perform search " + se);
        }
        return list;
    }

    /**
     * Find similar movies
     * @param movie the seed movie
     * @param num the number of movies to return
     * @return  the scored set of movies
     */
    List<Scored<Movie>> findSimilar(Movie movie, int num) {
        List<Scored<Movie>> scoredMovies =
                new ArrayList<Scored<Movie>>(num);

        try {
            DocumentVector lv =
                    searchEngine.getDocumentVector(movie.getID(), simField);
            if(lv != null) {
                ResultSet rs = lv.findSimilar("-score", skimPercent);

                List<Result> results = rs.getResults(0, num);
                for(Result result : results) {
                    Movie similarMovie = getMovie(result.getDocument());
                    if(similarMovie != null) {
                        scoredMovies.add(new Scored<Movie>(similarMovie,
                                result.getScore()));
                    }
                }
            }
        } catch(SearchEngineException se) {
            System.err.println("Trouble with findSimilar " + se);
        }
        if(scoredMovies.size() > num) {
            scoredMovies = scoredMovies.subList(0, num);
        }
        return scoredMovies;
    }

    private Movie getMovie(String id) {
        Document doc = searchEngine.getDocument(id);
        if(doc != null) {
            return getMovie(doc);
        } else {
            return null;
        }
    }

    private Movie getMovie(Document doc) {
        String id = doc.getKey();
        String title = getFieldAsString(doc, "common.name");
        Double popularity =
                (Double) getField(doc, "movie.users.popularity");
        return new Movie(id, title, popularity);
    }

    private Object getField(Document doc, String fieldName) {
        List value = doc.getSavedField(fieldName);
        if(value != null && value.size() > 0) {
            return value.get(0);
        }
        return null;
    }

    private String getFieldAsString(Document doc, String fieldName) {
        List value = doc.getSavedField(fieldName);
        if(value != null && value.size() > 0) {
            return (String) value.get(0);
        }
        return null;
    }

    private void indexRatings() throws IOException,
            SearchEngineException {
        File dir = new File(path, "training_set");

        File[] files = dir.listFiles();

        System.out.println("Min rating is " + getMinRating());
        SimpleIndexer indexer = searchEngine.getSimpleIndexer();
        for(int i = 0; i < files.length; i++) {
            indexRatingFile(i, indexer, files[i]);
        }
        indexer.finish();
    }

    private void indexRatingFile(int which,
            SimpleIndexer indexer, File file) throws IOException,
            SearchEngineException {
        BufferedReader reader =
                new BufferedReader(new FileReader(file));
        int numUsers = 0;
        String line;

        String movieId = reader.readLine().trim();
        movieId = movieId.substring(0, movieId.length() - 1);

        Movie movie = movieMap.get(movieId);

        assert movie != null;

        indexer.startDocument(movieId);
        indexer.addField("common.name", movie.getTitle());
        indexer.addField("common.type", "movie");
        Pattern cp = Pattern.compile(",");
        while((line = reader.readLine()) != null) {
            String[] parms = cp.split(line);
            if(parms.length == 3) {
                String userID = parms[0];
                String srating = parms[1];
                int irating = Integer.parseInt(srating);

                if(irating >= minRating) {
                    int adjustedRating = irating * irating;
                    indexer.addTerm("movie.users", userID, adjustedRating);
                    numUsers++;
                }
            }
        }
        indexer.addField("movie.users.popularity", numUsers);
        System.out.println(which + " id:" + movieId + " Users:" + numUsers + " " +
                movie);
        indexer.endDocument();
        reader.close();
    }

    public int getMinRating() {
        return minRating;
    }

    public void setMinRating(int minRating) {
        this.minRating = minRating;
    }

    public float getSkimPercent() {
        return skimPercent;
    }

    public void setSkimPercent(float skimPercent) {
        this.skimPercent = skimPercent;
    }

    private void index() throws IOException,
            SearchEngineException {
        indexRatings();
    }

    private void addMovie(Movie movie) {
        movieMap.put(movie.getID(), movie);
    }

    private String getRest(String[] parms, int start) {
        StringBuilder sb = new StringBuilder();

        for(int i = start; i < parms.length; i++) {
            sb.append(parms[i]);
            if(i < parms.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private void addCommands() {
        shell.add("index",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                try {
                    index();
                    return "";
                } catch(IOException e) {
                    return "IO error " + e;
                } catch(SearchEngineException se) {
                    return "SE Error " + se;
                }
            }

            public String getHelp() {
                return "indexes the netflix data";
            }
        });

        shell.add("sim_film",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length != 2) {
                    return "Usage: sim_film id";
                } else {
                    String id = args[1].trim();
                    Movie movie = movieMap.get(id);
                    if(movie != null) {
                        List<Scored<Movie>> similar = findSimilar(movie, 20);
                        for(Scored<Movie> s : similar) {
                            System.out.printf("%.4f %s %s\n", s.getScore(),
                                    s.getItem().getID(), s.getItem().getTitle());
                        }
                    } else {
                        return "Can't find movie with id " + id;
                    }
                    return "";
                }
            }

            public String getHelp() {
                return "finds similar film";
            }
        });

        shell.add("vec_time",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length != 2) {
                    return "Usage: vec_time id";
                } else {
                    String id = args[1].trim();
                    Movie movie = movieMap.get(id);

                    DocumentVector lv =
                            searchEngine.getDocumentVector(movie.getID(),
                            "movie.users");
                    if(lv == null) {
                        return "Can't find movie with id " + id;
                    }
                    return "";
                }
            }

            public String getHelp() {
                return "finds similar film";
            }
        });
        shell.add("sim_random",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length != 2) {
                    return "Usage: sim_random count";
                } else {
                    Random rand = new Random();
                    int count = Integer.parseInt(args[1]);
                    List<String> ids =
                            new ArrayList<String>(movieMap.keySet());
                    for(int i = 0; i < count; i++) {
                        int which = rand.nextInt(ids.size());
                        String id = ids.get(which);
                        Movie movie = movieMap.get(id);
                        if(movie != null) {
                            System.out.println("Movie: " + movie.getTitle() +
                                    " Popularity:" + movie.getPopularity());
                            List<Scored<Movie>> similar =
                                    findSimilar(movie, 20);
                            for(Scored<Movie> s : similar) {
                                System.out.printf("   %.4f %s %s\n",
                                        s.getScore(), s.getItem().getID(),
                                        s.getItem().getTitle());
                            }
                        } else {
                            return "Can't find movie with id " + id;
                        }
                    }
                    return "";
                }
            }

            public String getHelp() {
                return "randomly finds similar films";
            }
        });

        shell.add("search",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length < 2) {
                    System.out.println("Usage: search query ...");
                }
                String query = args[1];
                System.out.println("Searching for " + query);
                List<Scored<Movie>> results = movieSearch(query, 20);
                System.out.printf("Found %d results\n", results.size());
                for(Scored<Movie> s : results) {
                    System.out.printf("%.4f %s %s\n", s.getScore(),
                            s.getItem().getID(), s.getItem());
                }
                return "";
            }

            public String getHelp() {
                return "finds a film";
            }
        });

        shell.add("sim_user",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getHelp() {
                return "finds similar users";
            }
        });

        shell.add("setSkim",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length != 2) {
                    return "Usage: setSkim value - where value is between 0.0 and 1.0";
                } else {
                    float val = Float.parseFloat(args[1]);
                    if(val >= 0f && val <= 1.0f) {
                        setSkimPercent(val);
                    } else {
                        return "value must be between 0 and 1 (inclusive)";
                    }
                    return "";
                }
            }

            public String getHelp() {
                return "Sets the skim percent";
            }
        });

       shell.add("setSimField",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length != 2) {
                    return "Usage: setSimField value - where value is an indexed field name or null";
                } else {
                    simField = args[1].toLowerCase();
                    if(simField.equals("null")) {
                        simField = null;
                    }
                    return "";
                }
            }

            public String getHelp() {
                return "Sets the field that we'll use for computing similarity";
            }
        });

        shell.add("setMinRating",
                new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) {
                if(args.length != 2) {
                    return "Usage: setMinRatingSkim value - where value is between 0 and 5";
                } else {
                    int val = Integer.parseInt(args[1]);
                    if(val >= 0 && val <= 5) {
                        setMinRating(val);
                    } else {
                        return "value must be between 0 and 5 (inclusive)";
                    }
                    return "";
                }
            }

            public String getHelp() {
                return "Sets the min rating for indexing";
            }
        });
    }

    public void go() {
        shell.run();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: ScaleTest path-to-netflix-test-data [indexDir] [engineConfig]");
            System.exit(1);
        }
        try {
            Log log = Log.getLog();
            log.setStream(System.err);
            log.setLevel(3);
            ScaleTest st =
                    new ScaleTest(args[0],
                    args.length > 1 ? args[1] : "searchIndex",
                    args.length > 2 ? args[2] : "engineConfig.xml");
            st.go();
            System.exit(0);
        } catch(SearchEngineException se) {
            System.err.println("Trouble creating search engine " + se);
            System.exit(2);
        } catch(IOException ioe) {
            System.err.println("Trouble loading film data" + ioe);
            System.exit(3);
        }
    }
}

/**
 *  Represents a movie
 */
class Movie {

    private String id;
    private String title;
    private double popularity;

    /**
     * Creates a Movie
     * @param id  the netflix ID of the movie
     * @param title  the title of the movie
     * @param popularity  the popularity of the movie
     */
    public Movie(String id, String title, double popularity) {
        this.id = id;
        this.title = title;
        this.popularity = popularity;
    }

    /**
     * Gets the ID of the movie
     *
     * @return  the id
     */
    public String getID() {
        return id;
    }

    /**
     * Gets the title of the movie
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * Gets the popularity of the movie
     * @return the popularity
     */
    public double getPopularity() {
        return popularity;
    }

    /**
     * Sets the popularity for the movie
     * @param popularity  the popularity
     */
    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }
}
