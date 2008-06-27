/*
 * Concept.java
 *
 * Created on Oct 22, 2007, 6:11:43 AM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.apml;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author plamere
 */
public class Concept {

    public final static Comparator<Concept> VAL_ORDER = new Comparator<Concept>() {
        public int compare(Concept o1, Concept o2) {
            if (o1.getValue() > o2.getValue()) {
                return 1;
            } else if (o1.getValue() < o2.getValue()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private static final String DEFAULT_SOURCE = "tastebroker.org";
    private String key;
    private float value;
    private String from;
    private String update;
    private String annotation;

    public Concept(String key, float value, String from, String update, String annotation) {
        this.key = normalize(key);
        this.value = value;
        this.from = from;
        this.update = update;
        this.annotation = annotation;
    }

    public Concept(String key, float value, String annotation) {
        this(key, value, DEFAULT_SOURCE, 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()), annotation);
    }

    public Concept(String key, float value) {
        this(key, value, null);
    }

    public Concept(String key, float value, String from, String update) {
        this(key, value, from, update, null);
    }

    public String getFrom() {
        return from;
    }

    public String getKey() {
        return key;
    }

    public String getUpdate() {
        return update;
    }

    public float getValue() {
        return value;
    }

    private String normalize(String s) {
        return s.replaceAll("[^\\w\\s-]", " ");
    }

    //   <Concept key="media" value="0.73" from="GatheringTool.com" updated="2007-03-11T01:55:00Z" / >
    public String toXML(boolean implicit) {
        StringBuilder sb = new StringBuilder();

        if (annotation != null) {
            sb.append("\n<!-- " + annotation + " -->");
        }

        sb.append("<Concept ");
        append(sb, "key", getKey());
        append(sb, "value", Float.toString(getValue()));
        if (implicit) {
            append(sb, "from", getFrom());
            append(sb, "updated", getUpdate());
        }
        sb.append("/>");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toXML(true);
    }

    private static void append(StringBuilder sb, String key, String val) {
        sb.append(key);
        sb.append("=");
        sb.append("\"");
        sb.append(val);
        sb.append("\"");
        sb.append(" ");
    }
}