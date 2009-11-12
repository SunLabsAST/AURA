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

package com.sun.labs.aura.util;

import com.sun.labs.aura.service.StatService;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.minion.WeightedField;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Adds aura specific commands to the shell
 */
public class ShellUtils {

    private RemoteMultiComponentManager dscm = null;

    private DataStore dataStore;

    private RemoteComponentManager statcm = null;

    private StatService statService;

    private Logger logger = Logger.getLogger("");

    private int nHits = 10;

    private double skimPercentage = 0.25;

    private String[] displayFields;

    private String displayFormat;

    private Object[] displayVals;

    private CommandInterpreter shell;

    public String[] getDisplayFields() {
        return displayFields;
    }

    public void setDisplayFields(String[] displayFields) {
        this.displayFields = displayFields;
        displayVals = new Object[displayFields.length];
    }

    public String getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(String displayFormat) {
        this.displayFormat = displayFormat;
    }

    public ShellUtils(final CommandInterpreter shell,
            RemoteMultiComponentManager dscm,
            RemoteComponentManager statcm)
    throws AuraException {
        this(shell, (DataStore)dscm.getComponent(), (StatService)statcm.getComponent());
        this.dscm = dscm;
        this.statcm = statcm;
    }

    /**
     * Adds aura specific commands to the shell
     * @param shell the shell of interest
     * @param aDataStore the data store
     * @param aStatService the stat service
     */
    public ShellUtils(final CommandInterpreter shell, DataStore aDataStore,
            StatService aStatService) {
        this.shell = shell;
        this.dataStore = aDataStore;
        this.statService = aStatService;
        setDisplayFields(new String[] {"_score", "aura-type", "aura-key"});
        setDisplayFormat(" %.3f %s %s\n");

        shell.add("displayFields",
                new CommandInterface() {

            @Override
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                String[] fields = new String[args.length-1];
                System.arraycopy(args, 1, fields, 0, fields.length);
                setDisplayFields(fields);
                return "";
            }

            @Override
            public String getHelp() {
                return "Space separated list of field names to display for items";
            }
        });

        shell.add("displayFormat", new CommandInterface() {

            @Override
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                setDisplayFormat(args[1]);
                return "";
            }

            @Override
            public String getHelp() {
                return "Set the format string to use when displaying field values";
            }
        });

        shell.add("setN",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args) {
                        try {
                            if(args.length < 2) {
                                return getHelp();
                            }
                            nHits = Integer.parseInt(args[1]);
                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
                        }
                        return "";
                    }

                    public String getHelp() {
                        return "usage: setN <n> sets the number of hits to return from things.";
                    }
                });

        shell.add("addAttention",
                new CommandInterface() {
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length < 4 || args.length > 5) {
                    return "Usage: " + getHelp();
                }
                String type = args[3].toUpperCase();
                long n = 1;
                if (args.length == 5) {
                    n = Long.valueOf(args[4]);
                }
                Attention a = StoreFactory.newAttention(args[1], args[2], Enum.valueOf(Attention.Type.class, type), n);
                getDataStore().attend(a);
                return "Added " + n + " attention objects";
            }
            public String getHelp() {
                return "<srcKey> <tgtKey> <type> [<n>] where n is the number of attn to add";
            }
        });

        shell.add("genAttention",
                new CommandInterface() {
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length != 6) {
                    return "Usage: " + getHelp();
                }
                Integer numUsers = Integer.parseInt(args[1]);
                Integer numItems = Integer.parseInt(args[2]);
                Attention.Type type = Enum.valueOf(Attention.Type.class, args[3].toUpperCase());
                Integer cntRange = Integer.parseInt(args[4]);
                Integer numAttn = Integer.parseInt(args[5]);
                Random rand = new Random();
                List<Attention> attns = new ArrayList<Attention>(numAttn);
                int total = 0;
                for (int i = 0; i < numAttn; i++) {
                    int user = rand.nextInt(numUsers);
                    int item = rand.nextInt(numItems);
                    long cnt = 1;
                    if (cntRange > 1) {
                        cnt = rand.nextInt(cntRange);
                        cnt++;
                    }
                    total += cnt;
                    Attention a = StoreFactory.newAttention("user" + user, "item" + item, type, cnt);
                    attns.add(a);
                }
                getDataStore().attend(attns);
                return "Added " + total + " attentions in " + numAttn + " objects";
            }

            public String getHelp() {
                return "<numUsers> <numItems> <type> <cntRange> <numAttn>";
            }
        });

        shell.add("processAttention",
                new CommandInterface() {
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                if (args.length != 6) {
                    return "Usage: " + getHelp();
                }
                AttentionConfig ac = new AttentionConfig();
                if (!args[1].equals("*")) {
                    ac.setSourceKey(args[1]);
                }
                if (!args[2].equals("*")) {
                    ac.setTargetKey(args[2]);
                }
                if (!args[3].equals("*")) {
                    ac.setType(Enum.valueOf(Attention.Type.class, args[3].toUpperCase()));
                }
                FileReader reader = new FileReader(new File(args[4]));
                NanoWatch sw = new NanoWatch();
                sw.start();
                Object result = getDataStore().processAttention(ac, readFile(args[4]), args[5]);
                sw.stop();
                shell.getOutput().println("Result:");
                shell.getOutput().println(result);
                return "Completed in " + sw.getTimeMillis() + "ms";
            }
            public String getHelp() {
                return "<srcKey> <tgtKey> <type> <script file> <language> where attn fields may be '*' for all";
            }
        });

        shell.add("getAttentionCount",
                new CommandInterface() {
           public String execute(CommandInterpreter ci, String[] args)  throws Exception {
                if (args.length != 4) {
                   return "Usage: " + getHelp();
                }

                AttentionConfig ac = new AttentionConfig();
                if (!args[1].equals("*")) {
                    ac.setSourceKey(args[1]);
                }
                if (!args[2].equals("*")) {
                    ac.setTargetKey(args[2]);
                }
                if (!args[3].equals("*")) {
                    ac.setType(Enum.valueOf(Attention.Type.class, args[3].toUpperCase()));
                }
                long cnt = getDataStore().getAttentionCount(ac);
                return "Total of " + cnt + " attentions matched";
           }
           public String getHelp() {
               return "<srcKey> <tgtKey> <type> - gives the number of attn that match";
           }
        });

        shell.add("getAttentionCountSlow",
                new CommandInterface() {
           public String execute(CommandInterpreter ci, String[] args)  throws Exception {
                if (args.length != 4) {
                   return "Usage: " + getHelp();
                }
                AttentionConfig ac = new AttentionConfig();
                if (!args[1].equals("*")) {
                    ac.setSourceKey(args[1]);
                }
                if (!args[2].equals("*")) {
                    ac.setTargetKey(args[2]);
                }
                if (!args[3].equals("*")) {
                    ac.setType(Enum.valueOf(Attention.Type.class, args[3].toUpperCase()));
                }
                NanoWatch sw = new NanoWatch();
                sw.start();
                List<Attention> attns = getDataStore().getAttention(ac);
                int cnt = 0;
                for (Attention a : attns) {
                    cnt += a.getNumber();
                }
                sw.stop();
                return "Total of " + cnt + " attentions in " + sw.getTimeMillis() + "ms";
           }
           public String getHelp() {
               return "<srcKey> <tgtKey> <type> - gives the number of attn that match";
           }
        });


        shell.add("getScriptLanguages",
                new CommandInterface() {
            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                List<String> langs = getDataStore().getSupportedScriptLanguages();
                for (String l : langs) {
                    shell.getOutput().println(l);
                }
                return "";
            }
            public String getHelp() {
                return "Displays the supported scripting languages";
            }
        });

        shell.add("itemStats", new CommandInterface() {

            public String execute(CommandInterpreter ci, String[] args) throws Exception {
                for(ItemType type : ItemType.values()) {
                    long count = getDataStore().getItemCount(type);
                    if(count > 0) {
                        shell.getOutput().printf("  %8d %s\n", count, type.toString());
                    }
                }

                shell.getOutput().printf("  %d Attention Data\n", getDataStore().
                        getAttentionCount(new AttentionConfig()));
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
                            shell.getOutput().println("args: " + args.length);
                            for(int i = 1; i < args.length; i++) {
                                nw.start();
                                Item item = getDataStore().getItem(args[i]);
                                nw.stop();
                            }
                            shell.getOutput().printf(
                                    "%d gets took: %.4f avg: %.4f/get\n",
                                    args.length - 1,
                                    nw.getTimeMillis(),
                                    nw.getTimeMillis() / (args.length - 1));
                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
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

                            Item item = getDataStore().getItem(args[1]);
                            dumpItemFull(item);
                            if(item != null) {
                                shell.getOutput().printf("%-15s %s\n", "autotags",
                                        item.getField("autotag"));
                            }

                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
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

                            Item item = getDataStore().getItem(args[1]);
                            if(item != null) {
                                getDataStore().deleteItem(item.getKey());
                            } else {
                                shell.getOutput().println("Can't find item " + args[1]);
                            }

                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
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

                            User user = getDataStore().getUser(args[1]);
                            if(user != null) {
                                getDataStore().deleteUser(user.getKey());
                            } else {
                                shell.getOutput().println("Can't find user " + args[1]);
                            }

                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
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

                            Item item1 = getDataStore().getItem(args[1]);
                            Item item2 = getDataStore().getItem(args[2]);
                            if(item1 != null && item2 != null) {
                                getDataStore().attend(StoreFactory.newAttention(
                                        args[1], args[2],
                                        Attention.Type.LINKS_TO));
                            }

                            Item item1A = getDataStore().getItem(args[1]);
                            Item item2A = getDataStore().getItem(args[2]);
                            dumpItemFull(item1A);
                            dumpItemFull(item2A);
                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
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
                            shell.getOutput().println("Error " + ex);
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
                                AttentionConfig ac = new AttentionConfig();
                                ac.setTargetKey(args[1]);
                                List<Attention> attns = getDataStore().
                                        getAttention(ac);
                                for(Attention attn : attns) {
                                    shell.getOutput().println(attn);
                                }
                            }
                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
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
                                AttentionConfig ac = new AttentionConfig();
                                ac.setSourceKey(args[1]);
                                List<Attention> attns = getDataStore().
                                        getAttention(ac);
                                for(Attention attn : attns) {
                                    shell.getOutput().println(attn);
                                }
                            }
                        } catch(Exception ex) {
                            shell.getOutput().println("Error " + ex);
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
                            String[] counters = getStatService().getCounterNames();
                            Arrays.sort(counters);
                            shell.getOutput().printf("%20s %8s %8s %8s\n", "Stat",
                                    "counter", "average", "per min");
                            shell.getOutput().printf("%20s %8s %8s %8s\n", "----",
                                    "-------", "-------", "-------");
                            for(String counter : counters) {
                                if(counter.startsWith(prefix)) {
                                    long count = getStatService().get(counter);
                                    double avg = getStatService().getAverage(counter);
                                    double avgPerMin = getStatService().
                                            getAveragePerMinute(counter);
                                    shell.getOutput().printf("%20s %8d %8.3f %8.3f\n",
                                            counter, count, avg, avgPerMin);
                                }
                            }
                        } catch(Exception e) {
                            shell.getOutput().println("Error " + e);
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
                        List<Scored<Item>> items = getDataStore().query(query, nHits,
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
                        List<Scored<Item>> items = getDataStore().getAutotagged(
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
                        List<Scored<String>> terms = getDataStore().
                                getTopAutotagTerms(autotag, nHits);
                        for(Scored<String> term : terms) {
                            shell.getOutput().println(term);
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
                                getDataStore().findSimilarAutotags(autotag, nHits);
                        for(Scored<String> tag : autotags) {
                            shell.getOutput().println(tag);
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
                                getDataStore().explainSimilarAutotags(args[1],
                                args[2], nHits);
                        for(Scored<String> term : terms) {
                            shell.getOutput().println(term);
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
                        SimilarityConfig config = new SimilarityConfig(nHits);
                        config.setSkimPercent(skimPercentage);
                        List<Scored<Item>> items = getDataStore().findSimilar(key,
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
                        SimilarityConfig config = new SimilarityConfig(field, nHits, null);
                        config.setSkimPercent(skimPercentage);
                        List<Scored<Item>> items = getDataStore().findSimilar(key,
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
                        List<Scored<String>> expn = getDataStore().explainSimilarity(
                                key1, key2, new SimilarityConfig(nHits));
                        for(Scored<String> term : expn) {
                            shell.getOutput().print(term + " ");
                        }
                        shell.getOutput().println("");
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

                        List<Scored<String>> expn = getDataStore().explainSimilarity(
                                key1, key2, new SimilarityConfig(field, nHits));
                        for(Scored<String> term : expn) {
                            shell.getOutput().print(term + " ");
                        }
                        shell.getOutput().println("");
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

                        shell.getOutput().println("Using fields:");
                        for(WeightedField wf : fields) {
                            shell.getOutput().printf("   %s: %f\n", wf.getFieldName(),
                                    wf.getWeight());
                        }
                        SimilarityConfig config = new SimilarityConfig(fields, nHits, null);
                        config.setSkimPercent(skimPercentage);
                        List<Scored<Item>> items = getDataStore().findSimilar(key,
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
                        WordCloud terms = getDataStore().getTopTerms(key,
                                field, nHits);
                        shell.getOutput().println("Top terms:");
                        for(Scored<String> term : terms) {
                            shell.getOutput().printf("%.3f %s\n", term.getScore(),
                                    term.getItem());
                        }

                        SimilarityConfig config = new SimilarityConfig(field, nHits, null);
                        config.setSkimPercent(1);
                        List<Scored<Item>> items = getDataStore().findSimilar(terms,
                                config);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "<key> [<field>] gets the top terms from the given field (default: content) in the given document.";
                    }
                });
        shell.add("wcfs",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String field = args[1];
                        shell.getOutput().println("field: " + field);
                        shell.getOutput().println("args: " + args.length);
                        if((args.length - 2) % 2 != 0) {
                            return getHelp();
                        }
                        WordCloud terms = new WordCloud();
                        for(int i = 2; i < args.length-1; i+= 2) {
                            terms.add(args[i], Double.parseDouble(args[i+1]));
                        }
                        shell.getOutput().println("Terms:");
                        for(Scored<String> term : terms) {
                            shell.getOutput().printf("%.3f %s\n", term.getScore(),
                                    term.getItem());
                        }
                        SimilarityConfig config = new SimilarityConfig(field,
                                nHits, null);
                        config.setSkimPercent(1);
                        List<Scored<Item>> items = getDataStore().findSimilar(terms,
                                config);
                        dumpScoredItems(items);
                        return "";
                    }

                    public String getHelp() {
                        return "<field> [<word> <weight>] [<word> <weight>] ... finds items similar to the given word cloud from the given field";
                    }
                });

        shell.add("topTerms",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String key = args[1];
                        String field = args.length > 2 ? args[2] : "content";
                        WordCloud terms = getDataStore().getTopTerms(key,
                                field, nHits);
                        for(Scored<String> term : terms) {
                            shell.getOutput().printf("%.3f %s\n", term.getScore(),
                                    term.getItem());
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "<key> [<field>] gets the top terms from the given field (default: content) in the given document.";
                    }
                });

        shell.add("topTermCounts",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String key = args[1];
                        String field = args.length > 2 ? args[2] : "content";
                        List<Counted<String>> terms = getDataStore().getTopTermCounts(key, field, nHits);
                        for(Counted<String> term : terms) {
                            shell.getOutput().printf("%d %s\n", term.getCount(),
                                    term.getItem());
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "<key> [<field>] gets the top terms by count from the given field (default: content) in the given document.";
                    }
                });

        shell.add("getTermCounts",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String term = args[1];
                        String field = args.length > 2 ? args[2] : "content";
                        List<Counted<String>> docs = getDataStore().getTermCounts(term, field, nHits, new TypeFilter(ItemType.ARTIST));
                        for(Counted<String> doc : docs) {
                            shell.getOutput().printf("%d %s\n", doc.getCount(),
                                    doc.getItem());
                        }

                        return "";
                    }

                    public String getHelp() {
                        return "<term> [<field>] gets the top terms by count from the given field (default: content) in the given document.";
                    }
                });

        shell.add("explain",
                new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] args)
                            throws Exception {
                        String autotag = args[1];
                        String key = args[2];
                        List<Scored<String>> terms = getDataStore().getExplanation(
                                key, autotag, nHits);
                        for(Scored<String> term : terms) {
                            shell.getOutput().printf("%.3f %s\n", term.getScore(),
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
        for(Item item : getDataStore().getAll(ItemType.USER)) {
            dumpUser((User) item);
        }
    }

    public void dumpUser(User user) throws AuraException, RemoteException {
        dumpItem(user);
    }

    public void dumpItem(Item item) {
        if(item == null) {
            shell.getOutput().println("null");
        } else {
            shell.getOutput().printf(" %16s %s %s\n", item.getType().toString(), item.
                    getKey(), item.getName());
        }
    }

    public void dumpScoredItems(List<Scored<Item>> items) {
        for(Scored<Item> item : items) {
            try {
                dumpScoredItem(item);
            } catch(Exception ex) {
                shell.getOutput().printf("Error dumping item %s\n", ex);
            }
        }
    }

    public void dumpScored(List<Scored<String>> scoredStrings) {
        for(Scored<String> scored : scoredStrings) {
            shell.getOutput().printf("%.3f %s\n", scored.getScore(), scored.getItem());
        }
    }

    public void dumpCloud(WordCloud cloud) {
        for(Scored<String> scored : cloud) {
            shell.getOutput().printf("%.3f %s\n", scored.getScore(), scored.getItem());
        }
    }

    public void dumpTags(List<Tag> tags) {
        for(Tag tag : tags) {
            shell.getOutput().printf("%d %s\n", tag.getCount(), tag.getName());
        }
    }

    public void dumpItemFull(Item item) throws AuraException, RemoteException {
        if(item == null) {
            shell.getOutput().println("null");
        } else {
            shell.getOutput().println(ItemAdapter.toString(item));
            {
                AttentionConfig ac = new AttentionConfig();
                ac.setSourceKey(item.getKey());
                long count  = getDataStore().getAttentionCount(ac);
                shell.getOutput().println("src: " + count);
            }
            {
                AttentionConfig ac = new AttentionConfig();
                ac.setTargetKey(item.getKey());
                long count  = getDataStore().getAttentionCount(ac);
                shell.getOutput().println("tgt: " + count);
            }
        }
    }

    public void dumpScoredItem(Scored<Item> scoredItem) throws AuraException, RemoteException {
        if(scoredItem == null) {
            shell.getOutput().println("null");
        } else {

            for(int i = 0; i < displayFields.length; i++) {
                String field = displayFields[i];
                if(field.equals("_score")) {
                    displayVals[i] = scoredItem.getScore();
                } else if(field.equals("aura-key")) {
                    displayVals[i] = scoredItem.getItem().getKey();
                } else {
                    displayVals[i] = scoredItem.getItem().getField(field);
                }
            }
            shell.getOutput().printf(displayFormat, displayVals);
        }
    }

    public void dumpAttentionData(String msg, List<Attention> attentionData)
            throws AuraException, RemoteException {
        shell.getOutput().println("Attention " + msg);
        for(Attention attention : attentionData) {
            Item source = getDataStore().getItem(attention.getSourceKey());
            Item target = getDataStore().getItem(attention.getTargetKey());
            String type = attention.getType().toString();

            shell.getOutput().printf("   %s -- %s -- %s\n", fmtItem(source), type,
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
            List<FieldFrequency> tagFreqs = getDataStore().getTopValues("tag", n,
                    true);
            for(FieldFrequency ff : tagFreqs) {
                shell.getOutput().printf("%d %s\n", ff.getFreq(), ff.getVal().toString().
                        trim());
            }
        } catch(AuraException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        } catch(RemoteException ex) {
            logger.severe("dumpTagFrequencies " + ex);
        }
    }

    private String readFile(String path) throws IOException {
        File f = new File(path);
        StringBuilder sb = new StringBuilder((int)f.length());
        BufferedReader reader = new BufferedReader(new FileReader(f));
        char[] buf = new char[2048];
        int bytes = 0;
        while ((bytes=reader.read(buf)) != -1) {
            sb.append(buf, 0, bytes);
        }
        reader.close();
        return sb.toString();
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

    protected DataStore getDataStore() throws AuraException {
        if (dscm != null) {
            return (DataStore)dscm.getComponent();
        }
        return dataStore;
    }

    protected StatService getStatService() throws AuraException {
        if (statcm != null) {
            return (StatService)statcm.getComponent();
        }
        return statService;
    }
}
