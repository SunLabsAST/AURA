package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Some shared code across the servlets
 */
public class Util {

    private static Random rng = new Random();

    public enum ErrorCode {

        OK, InternalError, MissingArgument, BadArgument, NotFound, InvalidKey, RateLimitExceeded, NotAuthorized
    };

    public enum Detail {

        Tiny, Small, Medium, Large, Full
    };

    static String filter(String s) {
        if (s != null) {
            s = s.replaceAll("[^\\p{ASCII}]", "");
            s = s.replaceAll("\\&", "&amp;");
            s = s.replaceAll("\\<", "&lt;");
            s = s.replaceAll("\\>", "&gt;");
            s = s.replaceAll("[^\\p{Graph}\\p{Blank}]", "");

            //BUG
            s = s.replaceAll("\"", "");
        }
        return s;
    }

    static Timer getTimer() {
        return new Timer();
    }

    static void tagOpen(PrintWriter out, String tag) {
        out.println("<" + tag + ">");
    }

    static void tagClose(PrintWriter out, String tag) {
        out.println("</" + tag + ">");
    }

    static void tag(PrintWriter out, String tag, String val) {
        tagOpen(out, tag);
        out.println(val);
        tagClose(out, tag);
    }

    public static String toXML(Item item) {
        StringBuilder sb = new StringBuilder();
        sb.append("<item key=\"" + item.getKey() + "\">");
        sb.append("    <name>" + filter(item.getName()) + "</name>");
        sb.append("    <type>" + item.getType() + "</type>");
        sb.append("    <time>" + new Date(item.getTimeAdded()) + "</time>");

        //
        // Get the map entries sorted by key.
        List<Map.Entry<String, Serializable>> sl = new ArrayList<Map.Entry<String, Serializable>>();
        for (Map.Entry<String, Serializable> e : item) {
            sl.add(e);
        }
        Collections.sort(sl, new Comparator<Map.Entry<String, Serializable>>() {

            public int compare(Entry<String, Serializable> o1,
                    Entry<String, Serializable> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        //
        // Put them in the string.
        for (Map.Entry<String, Serializable> e : sl) {
            if (e.getValue() instanceof Collection) {
                for (Object element : (Collection) e.getValue()) {
                    sb.append(toXMLString(e.getKey(), element));
                }
            } else if (e.getValue() instanceof Map) {
                Map m = (Map) e.getValue();
                for (Object key : m.keySet()) {
                    sb.append(toXMLString(e.getKey(), m.get(key)));
                }
            } else {
                sb.append(toXMLString(e.getKey(), e.getValue()));
            }
        }
        sb.append("</item>");
        return sb.toString();
    }

    public static String toXML(Attention attn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<attn>");
        sb.append("    <src>" + attn.getSourceKey() + "</src>");
        sb.append("    <tgt>" + attn.getTargetKey() + "</tgt>");
        sb.append("    <type>" + attn.getType().toString() + "</type>");
        sb.append("    <time>" + attn.getTimeStamp() + "</time>");
        if (attn.getString() != null) {
            sb.append("    <sv>" + attn.getString() + "</sv>");
        }
        if (attn.getNumber() != null) {
            sb.append("    <nv>" + attn.getNumber() + "</nv>");
        }
        sb.append("</attn>");
        return sb.toString();
    }

    static String toXMLString(Object o) {
        if (o instanceof Tag) {
            Tag t = (Tag) o;
            return "<tag name=\"" + filter(t.getName()) + "\" freq=\"" + t.getCount() + "\"/>";
        } else {
            return Util.filter(o.toString());
        }
    }

    static String toXMLString(String tag, Object o) {
        if (o instanceof Tag) {
            Tag t = (Tag) o;
            return "<" + tag + " name=\"" + filter(t.getName()) + "\" freq=\"" + t.getCount() + "\"/>";
        } else {
            return "<" + tag + ">" + Util.filter(o.toString()) + "</" + tag + ">";
        }
    }

    static String toXMLString(MusicDatabase mdb, Scored<Artist> sartist, Detail detail) throws AuraException {
        if (detail == Detail.Tiny) {
            Artist artist = sartist.getItem();
            return "    <artist key=\"" + artist.getKey() + "\" " +
                    "score=\"" + sartist.getScore() + "\" " +
                    "name=\"" + Util.filter(artist.getName()) + "\"" + "/>";

        } else {
            Artist artist = sartist.getItem();
            StringBuilder sb = new StringBuilder();
            sb.append("    <artist key=\"" + artist.getKey() + "\" " +
                    "score=\"" + sartist.getScore() + "\" " +
                    "name=\"" + Util.filter(artist.getName()) + "\"" + ">");
            sb.append(" <artist key=\"" + artist.getKey() + "\">");
            sb.append("<name>" + Util.toXMLString(artist.getName()) + "</name>");
            sb.append("<popularity>" + mdb.artistGetNormalizedPopularity(artist) + "</popularity>");
            {
                String photo = selectFromCollection(artist.getPhotos());
                if (photo != null) {
                    sb.append("<image>" + Util.toXMLString(photo) + "</image>");
                }
            }
            {
                String audio = selectFromCollection(artist.getAudio());
                if (audio != null) {
                    sb.append("<audio>" + Util.toXMLString(audio) + "</audio>");
                }
            }
            {
                String spotify = artist.getSpotifyID();
                if (spotify != null) {
                    sb.append("<spotify>" + Util.toXMLString(spotify) + "</spotify>");
                }
            }
            
            if (detail.ordinal() >= Detail.Medium.ordinal()) {
                sb.append("<biographySummary>");
                sb.append(filter(artist.getBioSummary()));
                sb.append("</biographySummary>");
            }
            if (detail.ordinal() >= Detail.Large.ordinal()) {
                List<Tag> tags = artist.getSocialTags();
                for (Tag tag : tags) {
                    sb.append(toXMLString("socialTags", tag));
                }
            }

            sb.append("</artist>");
            return sb.toString();
        }
    }

    static String toTinyXMLString(MusicDatabase mdb, Scored<Artist> sartist) throws AuraException {
        Artist artist = sartist.getItem();
        return "    <artist key=\"" + artist.getKey() + "\" " +
                "score=\"" + sartist.getScore() + "\" " +
                "name=\"" + Util.filter(artist.getName()) + "\"" + "/>";
    }

    static String toSmallXMLString(MusicDatabase mdb, Scored<Artist> sartist) throws AuraException {
        Artist artist = sartist.getItem();
        StringBuilder sb = new StringBuilder();
        sb.append("    <artist key=\"" + artist.getKey() + "\" " +
                "score=\"" + sartist.getScore() + "\" " +
                "name=\"" + Util.filter(artist.getName()) + "\"" + ">");
        sb.append(" <artist key=\"" + artist.getKey() + "\">");
        sb.append("<name>" + Util.toXMLString(artist.getName()) + "</name>");
        sb.append("<popularity>" + mdb.artistGetNormalizedPopularity(artist) + "</popularity>");
        {
            String photo = selectFromCollection(artist.getPhotos());
            if (photo != null) {
                sb.append("<image>" + Util.toXMLString(photo) + "</image>");
            }
        }
        {
            String audio = selectFromCollection(artist.getAudio());
            if (audio != null) {
                sb.append("<audio>" + Util.toXMLString(audio) + "</audio>");
            }
        }
        {
            String spotify = artist.getSpotifyID();
            if (spotify != null) {
                sb.append("<spotify>" + Util.toXMLString(spotify) + "</spotify>");
            }
        }
        sb.append("</artist>");
        return sb.toString();
    }

    static String toMediumXMLString(MusicDatabase mdb, Scored<Artist> sartist) throws AuraException {
        return toTinyXMLString(mdb, sartist);
    }

    static String toLargeXMLString(MusicDatabase mdb, Scored<Artist> sartist) throws AuraException {
        return toTinyXMLString(mdb, sartist);
    }

    static String toFullXMLString(MusicDatabase mdb, Scored<Artist> sartist) throws AuraException {
        return toTinyXMLString(mdb, sartist);
    }

    static String selectFromCollection(Collection<String> set) {
        if (set.size() > 0) {
            return (String) set.toArray()[0];
        }
        return null;
    }

    static String selectRandomFromCollection(Collection<String> set) {
        if (set.size() > 0) {
            return (String) set.toArray()[rng.nextInt(set.size())];
        }
        return null;
    }
}

class Timer {

    long start = System.currentTimeMillis();

    void report(PrintWriter out) {
        out.println("<!-- output generated in " + (System.currentTimeMillis() - start) + " ms -->");
    }
}