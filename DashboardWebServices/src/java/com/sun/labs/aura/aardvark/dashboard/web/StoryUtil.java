/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import java.io.PrintWriter;
import java.rmi.RemoteException;
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
            BlogEntry entry = new BlogEntry(item.getItem());
            dumpStory(out, dataStore, entry, item.getScore());
        }
        out.println("</stories>");
    }

    public static void dumpStory(PrintWriter out, DataStore dataStore, BlogEntry entry, double score) throws AuraException, RemoteException {
        String padding = "        ";

        Item ifeed = dataStore.getItem(entry.getFeedKey());

        //TBD - fix this
        entry = new BlogEntry(dataStore.getItem(entry.getKey()));

        BlogFeed feed = new BlogFeed(ifeed);
        out.println("    <story score =\"" + score + "\">");
        String content = entry.getContent();
        if (content == null) {
            content = "";
        }


        dumpHtmlTaggedText(out, padding, "source", feed.getName());
        dumpHtmlTaggedText(out, padding, "imageUrl", feed.getImage());
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
                Collections.sort(autotags);
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
                    out.printf("    %s<topterm score=\"%f\">%s</topterm>\n", padding, 
                            term.getScore(), StoryUtil.filterTag(term.getItem()));
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

    private static String filterTag(String s) {
        s = s.replaceAll("[^\\p{ASCII}]", "");
        s = s.replaceAll("\\&", "&amp;");
        s = s.replaceAll("\\<", "&lt;");
        s = s.replaceAll("\\>", "&gt;");

        return s;
    }

    private static String filterHTML(String s) {
        s = detag(s);
        s = deentity(s);
        s = s.replaceAll("[^\\p{ASCII}]", "");
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("[\\<\\>\\&]", " ");
        s = s.replaceAll("#8217;", "'");
        s = s.replaceAll("#8220;", "'");
        s = s.replaceAll("#8221;", "'");
        s = s.replaceAll("#160;", "'");
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
        String[] words = s.split("\\s+");
        for (int i = 0; i < maxWords && i < words.length; i++) {
            sb.append(words[i] + " ");
        }

        if (maxWords < words.length) {
            sb.append("...");
        }
        return sb.toString().trim();
    }
}
