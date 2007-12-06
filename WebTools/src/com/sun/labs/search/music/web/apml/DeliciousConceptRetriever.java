/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class DeliciousConceptRetriever implements ConceptRetriever {

    private final static Concept[] EMPTY_CONCEPT = new Concept[0];

    public APML getAPMLForUser(String user) throws IOException {
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
        return new APML("Taste for del.icio.us user " + user, "web", concepts.toArray(EMPTY_CONCEPT), EMPTY_CONCEPT);
    }

    public static void main(String[] args) throws Exception {
        DeliciousConceptRetriever dcr = new DeliciousConceptRetriever();
        APML apml = dcr.getAPMLForUser("plamere");
        System.out.println(apml.toString());

        apml = dcr.getAPMLForUser("toby");
        System.out.println(apml.toString());
    }
}
