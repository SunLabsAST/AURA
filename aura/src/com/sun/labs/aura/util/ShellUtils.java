/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.FindSimilarConfig;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.WeightedField;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Adds aura specific commands to the shell
 */
public class ShellUtils {

    private DataStore dataStore;

    private StatService statService;

    private Logger logger;

    private int nHits = 10;
    
    private double skimPercentage = 0.25;

    /**
     * Adds aura specific commands to the shell
     * @param shell the shell of interest
     * @param aDataStore the data store
     * @param aStatService the stat service
     */
    public ShellUtils(CommandInterpreter shell, DataStore aDataStore,
            StatService aStatService) {
        this.dataStore = aDataStore;
        this.statService = aStatService;

        shell.add("setN",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length < 2) {
                                return getHelp();
                            }
                            nHits = Integer.parseInt(args[1]);
                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: setN <n> sets the number of hits to return from things.";
                    }
                });

        shell.add("itemStats", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                for(ItemType type : ItemType.values()) {
                    long count = dataStore.getItemCount(type);
                    if(count > 0) {
                        System.out.printf("  %8d %s\n", count, type.toString());
                    }
                }

                System.out.printf("  %d Attention Data\n", dataStore.
                        getAttentionCount());
                return "";
            }

            public String getHelp() {
                return "gets stats on the various types of items and attention data";
            }
        });

        shell.add("timeGetItem",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            NanoWatch nw = new NanoWatch();
                            System.out.println("args: " + args.length);
                            for(int i = 1; i < args.length; i++) {
                                nw.start();
                                Item item = dataStore.getItem(args[i]);
                                nw.stop();
                            }
                            System.out.printf(
                                    "%d gets took: %.4f avg: %.4f/get\n",
                                    args.length - 1,
                                    nw.getTimeMillis(),
                                    nw.getTimeMillis() / (args.length - 1));
                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: getItem <key> gets an item and prints the data map";
                    }
                });

        shell.add("item",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length == 1) {
                                return getHelp();
                            }

                            Item item = dataStore.getItem(args[1]);
                            dumpItemFull(item);
                            if(item != null) {
                                System.out.printf("%-15s %s\n", "autotags",
                                        item.getField("autotag"));
                            }

                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: item <key> gets an item and prints the data map";
                    }
                });

        shell.add("delItem",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length == 1) {
                                return getHelp();
                            }

                            Item item = dataStore.getItem(args[1]);
                            if(item != null) {
                                dataStore.deleteItem(item.getKey());
                            } else {
                                System.out.println("Can't find item " + args[1]);
                            }

                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: delItem <key> deletes an item";
                    }
                });

        shell.add("delUser",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length == 1) {
                                return getHelp();
                            }

                            User user = dataStore.getUser(args[1]);
                            if(user != null) {
                                dataStore.deleteUser(user.getKey());
                            } else {
                                System.out.println("Can't find user " + args[1]);
                            }

                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: delUser <key> deletes a user";
                    }
                });

        shell.add("tstattn",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length != 3) {
                                return getHelp();
                            }

                            Item item1 = dataStore.getItem(args[1]);
                            Item item2 = dataStore.getItem(args[2]);
                            if(item1 != null && item2 != null) {
                                dataStore.attend(StoreFactory.newAttention(
                                        args[1], args[2],
                                        Attention.Type.LINKS_TO));
                            }

                            Item item1A = dataStore.getItem(args[1]);
                            Item item2A = dataStore.getItem(args[2]);
                            dumpItemFull(item1A);
                            dumpItemFull(item2A);
                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: tstattn <key1 key2> tests adding and getting attention";
                    }
                });

        shell.add("dumpTagFrequencies",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            dumpTagFrequencies(nHits);
                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                            ex.printStackTrace();
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: dumpTagFrequencies shows top nHits tag frequencies for entries";
                    }
                });



        shell.add("attnTgt",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length != 2) {
                                return "Usage: attnTgt id";
                            } else {
                                List<Attention> attns = dataStore.
                                        getAttentionForTarget(args[1]);
                                for(Attention attn : attns) {
                                    System.out.println(attn);
                                }
                            }
                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: tgtattn key - shows target attention data for an item";
                    }
                });
        shell.add("attnSrc",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length != 2) {
                                return "Usage: attnSrc id";
                            } else {
                                List<Attention> attns = dataStore.
                                        getAttentionForSource(args[1]);
                                for(Attention attn : attns) {
                                    System.out.println(attn);
                                }
                            }
                        } catch(Exception ex) {
                            System.out.println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: attnSrc key - show source arget attention data for an item";
                    }
                });

        shell.add("stats",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length > 2) {
                                return "Usage: stats [prefix]";
                            }

                            String prefix = args.length == 2 ? args[1] : "";
                            String[] counters = statService.getCounterNames();
                            Arrays.sort(counters);
                            System.out.printf("%20s %8s %8s %8s\n", "Stat",
                                    "counter", "average", "per min");
                            System.out.printf("%20s %8s %8s %8s\n", "----",
                                    "-------", "-------", "-------");
                            for(String counter : counters) {
                                if(counter.startsWith(prefix)) {
                                    long count = statService.get(counter);
                                    double avg = statService.getAverage(counter);
                                    double avgPerMin = statService.
                                            getAveragePerMinute(counter);
                                    System.out.printf("%20s %8d %8.3f %8.3f\n",
                                            counter, count, avg, avgPerMin);
                                }
                            }
                        } catch(Exception e) {
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
                        List<Scored<Item>> items = dataStore.query(query, nHits,
                                null);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "Runs a query";
                    }
                });
        shell.add("gat",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String autotag = stuff(args, 1).trim();
                        List<Scored<Item>> items = dataStore.getAutotagged(
                                autotag, nHits);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "get top auttagged items:   gat <autotag>";
                    }
                });

        shell.add("gtt",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String autotag = stuff(args, 1).trim();
                        List<Scored<String>> terms = dataStore.
                                getTopAutotagTerms(autotag, nHits);
                        for(Scored<String> term : terms) {
                            System.out.println(term);
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "get top autotag terms: gtt <autotag>";
                    }
                });

        shell.add("tsim",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String autotag = stuff(args, 1).trim();
                        List<Scored<String>> autotags =
                                dataStore.findSimilarAutotags(autotag, nHits);
                        for(Scored<String> tag : autotags) {
                            System.out.println(tag);
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "Get autotags most similar to the given tag: tsim <autotag>";
                    }
                });

        shell.add("etsim",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        if(args.length < 3) {
                            return getHelp();
                        }
                        List<Scored<String>> terms =
                                dataStore.explainSimilarAutotags(args[1],
                                args[2], nHits);
                        for(Scored<String> term : terms) {
                            System.out.println(term);
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "Explain autotag similarity: etsim <autotag> <autotag>";
                    }
                });

        shell.add("setSkim",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        try {
                            skimPercentage = Double.parseDouble(args[1]);
                            return "";
                        } catch(NumberFormatException nfe) {
                            return args[1] + " is not a valid percentage";
                        }
                    }

                    public String getHelp() {
                        return "Find similar";
                    }
                });
        shell.add("fs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String key = args[1];
                        FindSimilarConfig config = new FindSimilarConfig(nHits);
                        config.setSkimPercent(skimPercentage);
                        List<Scored<Item>> items = dataStore.findSimilar(key,
                                config);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "Find similar";
                    }
                });
        shell.add("ffs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String field = args[1];
                        String key = args[2];
                        FindSimilarConfig config = new FindSimilarConfig(field, nHits, null);
                        config.setSkimPercent(skimPercentage);
                        List<Scored<Item>> items = dataStore.findSimilar(key,
                                config);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "Find similar with a field";
                    }
                });
        shell.add("efs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        if(args.length < 3) {
                            return getHelp();
                        }
                        String key1 = args[1];
                        String key2 = args[2];
                        List<Scored<String>> expn = dataStore.explainSimilarity(
                                key1, key2, nHits);
                        for(Scored<String> term : expn) {
                            System.out.print(term + " ");
                        }
                        System.out.println("");
                        return "";
                    }

                    public String getHelp() {
                        return "Explain Find similar: efs <key1> <key2>";
                    }
                });
        shell.add("effs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        if(args.length < 4) {
                            return getHelp();
                        }
                        String field = args[1];
                        String key1 = args[2];
                        String key2 = args[3];
                        List<Scored<String>> expn = dataStore.explainSimilarity(
                                key1, key2, field, nHits);
                        for(Scored<String> term : expn) {
                            System.out.print(term + " ");
                        }
                        System.out.println("");
                        return "";
                    }

                    public String getHelp() {
                        return "Explain Fielded Find similar: efs <field> <key1> <key2>";
                    }
                });


        shell.add("fwfs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        WeightedField[] fields = {
                            new WeightedField("content", 1),
                        //    new WeightedField("autotag", 1),
                        //   new WeightedField("aura-name", 1),
                        };
                        String key = args[1];

                        System.out.println("Using fields:");
                        for(WeightedField wf : fields) {
                            System.out.printf("   %s: %f\n", wf.getFieldName(),
                                    wf.getWeight());
                        }
                        FindSimilarConfig config = new FindSimilarConfig(fields, nHits, null);
                        config.setSkimPercent(skimPercentage);
                        List<Scored<Item>> items = dataStore.findSimilar(key,
                                config);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "Find similar with weighted fields";
                    }
                });
                
        shell.add("cfs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String key = args[1];
                        String field = args.length > 2 ? args[2] : "content";
                        WordCloud terms = dataStore.getTopTerms(key,
                                field, nHits);
                        System.out.println("Top terms:");
                        for(Scored<String> term : terms) {
                            System.out.printf("%.3f %s\n", term.getScore(),
                                    term.getItem());
                        }
                        
                        FindSimilarConfig config = new FindSimilarConfig(field, nHits, null);
                        config.setSkimPercent(1);
                        List<Scored<Item>> items = dataStore.findSimilar(terms,
                                config);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "<key> [<field>] gets the top terms from the given field (default: content) in the given document.";
                    }
                });

        shell.add("topTerms",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String key = args[1];
                        String field = args.length > 2 ? args[2] : "content";
                        WordCloud terms = dataStore.getTopTerms(key,
                                field, nHits);
                        for(Scored<String> term : terms) {
                            System.out.printf("%.3f %s\n", term.getScore(),
                                    term.getItem());
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "<key> [<field>] gets the top terms from the given field (default: content) in the given document.";
                    }
                });

        shell.add("explain",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String autotag = args[1];
                        String key = args[2];
                        List<Scored<String>> terms = dataStore.getExplanation(
                                key, autotag, nHits);
                        for(Scored<String> term : terms) {
                            System.out.printf("%.3f %s\n", term.getScore(),
                                    term.getItem());
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "<autotag> <key> explain the classification of key into autotag";
                    }
                });
    }

    public void dumpAllUsers() throws AuraException, RemoteException {
        for(Item item : dataStore.getAll(ItemType.USER)) {
            dumpUser((User) item);
        }
    }

    public void dumpUser(User user) throws AuraException, RemoteException {
        dumpItem(user);
    }

    public void dumpItem(Item item) {
        if(item == null) {
            System.out.println("null");
        } else {
            System.out.printf(" %16s %s %s\n", item.getType().toString(), item.
                    getKey(), item.getName());
        }
    }

    public void dumpScoredItems(List<Scored<Item>> items) {
        for(Scored<Item> item : items) {
            System.out.printf("%.3f ", item.getScore());
            dumpItem(item.getItem());
        }
    }

    public void dumpScored(List<Scored<String>> scoredStrings) {
        for(Scored<String> scored : scoredStrings) {
            System.out.printf("%.3f %s\n", scored.getScore(), scored.getItem());
        }
    }

    public void dumpCloud(WordCloud cloud) {
        for(Scored<String> scored : cloud) {
            System.out.printf("%.3f %s\n", scored.getScore(), scored.getItem());
        }
    }

    public void dumpTags(List<Tag> tags) {
        for(Tag tag : tags) {
            System.out.printf("%d %s\n", tag.getCount(), tag.getName());
        }
    }

    public void dumpItemFull(Item item) throws AuraException, RemoteException {
        if(item == null) {
            System.out.println("null");
        } else {
            System.out.println(ItemAdapter.toString(item));
            System.out.println("src: " + dataStore.getAttentionForSource(item.
                    getKey()).size());
            System.out.println("tgt: " + dataStore.getAttentionForTarget(item.
                    getKey()).size());
        }
    }

    public void dumpScoredItem(Scored<Item> scoredItem) throws AuraException, RemoteException {
        if(scoredItem == null) {
            System.out.println("null");
        } else {
            System.out.printf(" %.0f %s\n", scoredItem.getScore(),
                    scoredItem.getItem().getKey());
        }
    }

    public void dumpAttentionData(String msg, List<Attention> attentionData)
            throws AuraException, RemoteException {
        System.out.println("Attention " + msg);
        for(Attention attention : attentionData) {
            Item source = dataStore.getItem(attention.getSourceKey());
            Item target = dataStore.getItem(attention.getTargetKey());
            String type = attention.getType().toString();

            System.out.printf("   %s -- %s -- %s\n", fmtItem(source), type,
                    fmtItem(target));
        }
    }

    private String fmtItem(Item item) {
        if(item == null) {
            return "(null)";
        } else {
            return item.getKey() + "(" + item.getName() + ")";
        }
    }

    private void dumpTagFrequencies(int n) {

        try {
            List<FieldFrequency> tagFreqs = dataStore.getTopValues("tag", n,
                    true);
            for(FieldFrequency ff : tagFreqs) {
                System.out.printf("%d %s\n", ff.getFreq(), ff.getVal().toString().
                        trim());
            }
        } catch(AuraException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        } catch(RemoteException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        }
    }

    public String stuff(String[] args, int p) {
        StringBuilder sb = new StringBuilder();
        for(int i = p; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public int getHits() {
        return nHits;
    }
    
    public double getSkimPercentage() {
        return skimPercentage;
    }
}
