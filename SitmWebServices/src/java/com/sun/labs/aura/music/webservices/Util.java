package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Item;
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

/**
 * Some shared code across the servlets
 */
public class Util {

    enum ErrorCode {

        OK, MissingArgument, DataStore, Configuration, NotFound, BadArgument
    };

    static String filter(String s) {
        if (s != null) {
            s = s.replaceAll("[^\\p{ASCII}]", "");
            s = s.replaceAll("\\&", "&amp;");
            s = s.replaceAll("\\<", "&lt;");
            s = s.replaceAll("\\>", "&gt;");
            s = s.replaceAll("[^\\p{Graph}\\p{Blank}]", "");
        }
        return s;
    }

    static void outputStatus(PrintWriter out, ErrorCode code, String message) {
        if (code == ErrorCode.OK) {
            out.println("    <Status code='OK'/>");
        } else {
            out.println("    <Status code='" + code.toString() + "'>" + message + "</Status>");
        }
    }

    static void outputStatus(PrintWriter out, String tag, ErrorCode code, String message) {
        out.println("<" + tag + ">");
        outputStatus(out, code, message);
        out.println("</" + tag + ">");
    }

    static void outputOKStatus(PrintWriter out) {
        outputStatus(out, ErrorCode.OK, null);
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

    private static String toXMLString(Object o) {
        if (o instanceof Tag) {
            Tag t = (Tag) o;
            return "<tag name=\"" + filter(t.getName()) + "\" freq=\"" + t.getCount() + "\"/>";
        } else {
            return Util.filter(o.toString());
        }
    }

    private static String toXMLString(String tag, Object o) {
        if (o instanceof Tag) {
            Tag t = (Tag) o;
            return "<" + tag + " name=\"" + filter(t.getName()) + "\" freq=\"" + t.getCount() + "\"/>";
        } else {
            return "<" + tag + ">" + Util.filter(o.toString()) + "</" + tag + ">";
        }
    }
}

class Timer {

    long start = System.currentTimeMillis();

    void report(PrintWriter out) {
        out.println("<!-- output generated in " + (System.currentTimeMillis() - start) + " ms -->");
    }
}