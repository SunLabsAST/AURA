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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author plamere
 */
public class DeliciousProfileRetriever implements APMLRetriever {

    private final static Concept[] EMPTY_CONCEPT = new Concept[0];

    private Profile getProfileForUser(String user) throws IOException {
        List<Concept> concepts = new ArrayList<Concept>();
        InputStream is = null;
        List<String> elements = new ArrayList<String>();
        try {
            URL url = new URL("http://del.icio.us/" + user + "?settagsort=freq");
            is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            int c;
            while ((c = br.read()) != -1) {
                sb.append((char) c);
            }
            br.close();
            String content = sb.toString();
// 			<li><span>3</span> <a href="/plamere/research">research</a></li>
            String tagregex = "<li><span>(\\d+)</span> <a href=\"/" + user + "/([^\"]+)";
            Pattern tagPattern = Pattern.compile(tagregex, Pattern.CASE_INSENSITIVE);
            Matcher tagMatcher = tagPattern.matcher(content);
            int max = -Integer.MAX_VALUE;
            while (tagMatcher.find()) {
                String scount = tagMatcher.group(1);
                int count = Integer.parseInt(scount);
                String tag = tagMatcher.group(2);
                if (count > max) {
                    max = count;
                }
                Concept concept = new Concept(tag, count);
                concepts.add(concept);
            }

            for (Concept concept : concepts) {
                concept.setValue(concept.getValue() / max);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return new Profile("web", concepts.toArray(EMPTY_CONCEPT), EMPTY_CONCEPT);
    }

    public APML getAPMLForUser(String user) throws IOException {
        APML apml = new APML("Del.icio.us data taste for " + user);
        apml.addProfile(getProfileForUser(user));
        return apml;
    }

    public static void main(String[] args) throws Exception {
        DeliciousProfileRetriever dcr = new DeliciousProfileRetriever();
        APML apml = dcr.getAPMLForUser("plamere");
        System.out.println(apml.toString());

        apml = dcr.getAPMLForUser("toby");
        System.out.println(apml.toString());
    }
}
