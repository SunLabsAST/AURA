/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
