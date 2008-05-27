/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.Tag;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author plamere
 */
public class StoryUtil {

    public static void dumpStories(PrintWriter out, DataStore dataStore, List<BlogEntry> entries) throws AuraException, RemoteException {
        out.println("<stories>");
        for (BlogEntry entry : entries) {
            dumpStory(out, dataStore, entry, 1.0);
        }
        out.println("</stories>");
    }

    public static void dumpScoredStories(PrintWriter out, DataStore dataStore, List<Scored<Item>> items) throws AuraException, RemoteException {
        out.println("<stories>");
        for (Scored<Item> item : items) {
            if (item.getItem().getType() == ItemType.BLOGENTRY) {
                BlogEntry entry = new BlogEntry(item.getItem());
                dumpStory(out, dataStore, entry, item.getScore());
            } else {
                System.out.println("StoryUtil.dumpScoredStories -  unexpected item type");
            }
        }
        out.println("</stories>");
    }

    public static void dumpTagInfo(PrintWriter out, DataStore dataStore, List<Scored<String>> autotags, String key)
            throws AuraException, RemoteException {
        out.println("<TagInfos>");
        //out.println("    <key>" + key + "</key>");
        for (Scored<String> tag : autotags) {
            out.printf("    <TagInfo name='%s' score='%f'>\n", tag.getItem(), tag.getScore());

            {
                if (key != null) {
                    out.println("        <DocTerms>");
                    for (Scored<String> explanation : dataStore.getExplanation(key, tag.getItem(), 20)) {
                        out.printf("            <DocTerm name=\'%s\' score=\'%f\'/>\n", explanation.getItem(), explanation.getScore());
                    }
                    out.println("        </DocTerms>");
                }
            }

            {
                out.println("        <TopTerms>");
                for (Scored<String> term : dataStore.getTopAutotagTerms(tag.getItem(), 20)) {
                    out.printf("            <TopTerm name=\'%s\' score=\'%f\'/>\n", term.getItem(), term.getScore());
                }
                out.println("        </TopTerms>");
            }

            {
                out.println("        <SimTags>");
                for (Scored<String> term : dataStore.findSimilarAutotags(tag.getItem(), 20)) {
                    if (!tag.getItem().equals(term.getItem())) {
                        out.printf("            <SimTag name=\'%s\' score=\'%f\'/>\n", term.getItem(), term.getScore());
                    }
                }
                out.println("        </SimTags>");
            }

            out.println("    </TagInfo>");
        }
        out.println("</TagInfos>");
    }


    public static void dumpTagInfo(PrintWriter out, DataStore dataStore, String tag, int max) throws AuraException, RemoteException {
        out.println("<TagInfos>");
        out.println("    <tag>" + tag + "</tag>");
        for (Scored<String> simtags : dataStore.findSimilarAutotags(tag, max + 1)) {

            // skip self similarity
            if (tag.equals(simtags.getItem())) {
                continue;
            }

            out.printf("    <TagInfo name='%s' score='%f'>\n", simtags.getItem(), simtags.getScore());

            {
                    out.println("        <DocTerms>");
                    for (Scored<String> explanation : dataStore.explainSimilarAutotags(tag, simtags.getItem(), 20)) {
                        out.printf("            <DocTerm name=\'%s\' score=\'%f\'/>\n", explanation.getItem(), explanation.getScore());
                    }
                    out.println("        </DocTerms>");
            }

            {
                out.println("        <TopTerms>");
                for (Scored<String> term : dataStore.getTopAutotagTerms(simtags.getItem(), 20)) {
                    out.printf("            <TopTerm name=\'%s\' score=\'%f\'/>\n", term.getItem(), term.getScore());
                }
                out.println("        </TopTerms>");
            }

            {
                out.println("        <SimTags>");
                for (Scored<String> term : dataStore.findSimilarAutotags(simtags.getItem(), 20)) {
                    if (!simtags.getItem().equals(term.getItem())) {
                        out.printf("            <SimTag name=\'%s\' score=\'%f\'/>\n", term.getItem(), term.getScore());
                    }
                }
                out.println("        </SimTags>");
            }

            out.println("    </TagInfo>");
        }
        out.println("</TagInfos>");
    }


    public static void dumpStory(PrintWriter out, DataStore dataStore, BlogEntry entry, double score) throws AuraException, RemoteException {
        String padding = "        ";

        Item ifeed = dataStore.getItem(entry.getFeedKey());

        //TBD - fix this
        // entry = new BlogEntry(dataStore.getItem(entry.getKey()));

        String feedName = "";
        String feedImage = "";
        BlogFeed feed = new BlogFeed(ifeed);
        if (feed != null) {
            feedName = feed.getName();
            feedImage = feed.getImage();
        }
        out.println("    <story score =\"" + score + "\">");
        String content = entry.getContent();
        if (content == null) {
            content = "";
        }


        dumpHtmlTaggedText(out, padding, "source", feedName);
        dumpHtmlTaggedText(out, padding, "imageUrl", feedImage);
        dumpHtmlTaggedText(out, padding, "url", entry.getKey());
        dumpHtmlTaggedText(out, padding, "title", excerpt(filterHTML(entry.getTitle()), 20));
        dumpHtmlTaggedText(out, padding, "pulltime", Long.toString(entry.getTimeAdded()));
        dumpHtmlTaggedText(out, padding, "length", Integer.toString(content.length()));

        if (entry.getContent() != null) {
            out.println("        <description>" + excerpt(filterHTML(content), 100) + "</description>");
        }


        {
            if (entry.getTags().size() > 0) {
                out.println("        <tags>");
                for (Tag tag : entry.getTags()) {
                    out.printf("    %s<tag score=\"%d\">%s</tag>\n", padding, tag.getCount(), filterTag(tag.getName()));
                }
                out.println("        </tags>");
            }
        }

        {
            List<Scored<String>> autotags = entry.getAutoTags();
            if (autotags.size() > 0) {
                Collections.sort(autotags, ScoredComparator.COMPARATOR);
                Collections.reverse(autotags);
                out.println("        <autotags>");
                for (Scored<String> tag : autotags) {
                    out.printf("    %s<autotag score=\"%f\">%s</autotag>\n", padding, tag.getScore(), tag.getItem());
                }
                out.println("        </autotags>");
            }
        }


        {
            List<Scored<String>> topTerms = dataStore.getTopTerms(entry.getKey(), null, 10);
            if (topTerms.size() > 0) {
                out.println("        <topterms>");
                for (Scored<String> term : topTerms) {
                    String filteredTerm = filterTag(term.getItem());
                    if (filteredTerm.length() > 0) {
                        out.printf("    %s<topterm score=\"%f\">%s</topterm>\n", padding,
                                term.getScore(), filteredTerm);
                    }
                }
                out.println("        </topterms>");
            }
        }

        out.println("    </story>");
    }

    private static void dumpHtmlTaggedText(PrintWriter out, String indent, String tag, String value) {
        if (value != null) {
            value = filterTag(value);
            out.println(indent + "<" + tag + ">" + value + "</" + tag + ">");
        }
    }

    static String filterTag(String s) {
        if (s != null) {
            s = s.replaceAll("[^\\p{ASCII}]", "");
            s = s.replaceAll("\\&", "&amp;");
            s = s.replaceAll("\\<", "&lt;");
            s = s.replaceAll("\\>", "&gt;");
            s = s.replaceAll("[^\\p{Graph}\\p{Blank}]", "");
        }
        return s;
    }

    private static String filterHTML(String s) {
        if (s != null) {
            s = detag(s);
            s = deentity(s);
            s = s.replaceAll("[^\\p{ASCII}]", "");
            s = s.replaceAll("\\s+", " ");
            s = s.replaceAll("[\\<\\>\\&]", " ");
            s = s.replaceAll("#8217;", "'");
            s = s.replaceAll("#8220;", "'");
            s = s.replaceAll("#8221;", "'");
            s = s.replaceAll("#160;", "'");
            s = s.replaceAll("#(\\p{Digit}){3,4};", "'");
        }
        return s;
    }

    private static String detag(String s) {
        return s.replaceAll("\\<.*?\\>", "");
    }

    private static String deentity(String s) {
        return s.replaceAll("\\&[a-zA-Z]+;", " ");
    }

    private static String excerpt(String s, int maxWords) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            String[] words = s.split("\\s+");
            for (int i = 0; i < maxWords && i < words.length; i++) {
                sb.append(words[i] + " ");
            }

            if (maxWords < words.length) {
                sb.append("...");
            }
        }
        return sb.toString().trim();
    }
}
