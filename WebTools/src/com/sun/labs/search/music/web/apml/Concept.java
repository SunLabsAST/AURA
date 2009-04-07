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

package com.sun.labs.search.music.web.apml;

import com.sun.labs.search.music.web.Utilities;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author plamere
 */
public class Concept implements Comparable<Concept> {

    private static final String DEFAULT_SOURCE = "tastebroker.org";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private String key;
    private float value;
    private String from;
    private String update;

    public Concept(String key, float value, String from, String update) {
        this.key = key;
        this.value = value;
        this.from = from;
        this.update = update;
    }

    public Concept(String key, float value) {
        this(key, value, DEFAULT_SOURCE, sdf.format(new Date()));
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

    //   <Concept key="media" value="0.73" from="GatheringTool.com" updated="2007-03-11T01:55:00Z" / >
    public String toXML(boolean explicit) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Concept ");
        append(sb, "key", Utilities.XMLEscape(getKey()));
        append(sb, "value", Float.toString(getValue()));
        if (!explicit) {
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
