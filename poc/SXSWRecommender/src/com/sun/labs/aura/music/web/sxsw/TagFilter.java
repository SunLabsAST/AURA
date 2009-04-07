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

package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.aura.music.web.Utilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class TagFilter {
    private Map<String, String> validTagMap = new HashMap<String, String>();


    public TagFilter() {
        loadTagFilter();
    }

    public String mapTagName(String tagName) {
        String mappedName = validTagMap.get(tagName);
        if (mappedName == null) {
            mappedName = validTagMap.get(Utilities.normalize(tagName));
        }
        /*
        if (mappedName != null && !tagName.equals(mappedName)) {
        System.out.printf("remapped '%s' to '%s'\n", tagName, mappedName);
        }
         */
        return mappedName;
    }

    private void loadTagFilter() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(ArtistCrawler.class.getResourceAsStream("taglist.txt")));
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] aliases = line.split(",");
                    if (aliases.length > 0) {
                        String primary = aliases[0].trim();
                        for (String alias : aliases) {
                            alias = alias.trim();
                            validTagMap.put(alias, primary);
                            validTagMap.put(Utilities.normalize(alias), primary);
                        }
                    }
                }
            } finally {
                in.close();
            }
        } catch (IOException ioe) {
            System.err.println("Couldn't read the tagfilter list");
        }
    }
}
