/*
 * Concept.java
 *
 * Created on Oct 22, 2007, 6:11:43 AM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.apml;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author plamere
 */
public class Concept implements Comparable<Concept> {
    private static final String DEFAULT_SOURCE = "tastebroker.org";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private String key;
    private float value;
    private String from;
    private long update;

    public Concept(String key, float value, String from, long update) {
        this.key = normalize(key);
        this.value = value;
        this.from = from;
        this.update = update;
    }
    
    public Concept(String key, float value) {
        this(key, value, DEFAULT_SOURCE, 0);
    }

    public String getFrom() {
        return from;
    }

    public String getKey() {
        return key;
    }

    public long getUpdate() {
        if (update == 0) {
            return System.currentTimeMillis();
        } else {
            return update;
        }
    }

    public float getValue() {
        return value;
    }

    private String normalize(String s) {
        s = URLDecoder.decode(s);
        return s.replaceAll("[^\\w\\s]", " ").trim();
    }
    
    //   <Concept key="media" value="0.73" from="GatheringTool.com" updated="2007-03-11T01:55:00Z" / >
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<Concept ");
        append(sb, "key", getKey());
        append(sb, "value", Float.toString(getValue()));
        append(sb, "from", getFrom());

        Date updated = new Date(getUpdate());
        append(sb, "updated", sdf.format(updated));

        sb.append("/>");
        return sb.toString();
    }
    
    public String toString() {
        return toXML();
    }

    private void append(StringBuilder sb, String key, String val) {
        sb.append(key);
        sb.append("=");
        sb.append("\"");
        sb.append(val);
        sb.append("\"");
        sb.append(" ");
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int compareTo(Concept o) {
        if (getValue() > o.getValue()) {
            return 1;
        } else if (getValue() < o.getValue()) {
            return -1;
        } else {
            return 0;
        }
    }
}