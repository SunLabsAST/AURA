/*
 * Wikipedia.java
 *
 * Created on April 1, 2007, 7:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.wikipedia;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author plamere
 */
public class Wikipedia {

    private Commander commander;

    /** Creates a new instance of Youtube */
    public Wikipedia() {
        try {
            commander = new Commander("Wikipedia", "http://en.wikipedia.org/wiki/Special:Export/", "");
            commander.setTraceSends(false);
            commander.setTrace(false);
        } catch (IOException ex) {
            System.err.println("Can't get wikipedia commander " + ex);
        }
    }    // http://www.youtube.com/api2_rest?method=youtube.videos.list_by_tag&dev_id=oONGHZSHcBU&tag=weezer&page=1
    // The redirect pattern.  Some examples
    // Desc: #REDIRECT [[The Smashing Pumpkins]] {{R from alternate name}}
    // Desc: #REDIRECT [[Madonna (entertainer)]]
    static Pattern redirectPattern = Pattern.compile("#REDIRECT\\s+\\[\\[(.*?)\\]\\].*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public String getArtistDescription(String wikiurl) throws IOException {
        return getArtistDescription(0, wikiurl);
    }

    private String getArtistDescription(int redirectCount, String wikiurl) throws IOException {

        // there can be circular references, so limit the number of
        // redirections

        if (redirectCount > 3) {
            return null;
        }

        String artistName = getArtistFromURL(wikiurl);
        String encodedArtist = encodeName(artistName);
        Document doc = commander.sendCommand(encodedArtist);
        Element docElement = doc.getDocumentElement();
        Element itemElement = XmlUtil.getFirstElement(docElement, "text");
        String description = null;
        if (itemElement != null) {
            description = itemElement.getTextContent();
        }


        if (description != null) {
            // check for redirect.  They look like this:
            // #REDIRECT [[NAME OF PAGE B]]
            // if we find one, go to it instead.  For an example checkout
            // Madonna_(singer)

            Matcher m = redirectPattern.matcher(description);
            if (m.matches()) {
                String redirectedTo = m.group(1);
                // System.out.println("Redirected to " + redirectedTo);
                description = getArtistDescription(redirectCount + 1, redirectedTo);
            }
        }

        return description;
    }

    public void setMinimumCommandPeriod(int seconds) {
        commander.setMinimumCommandPeriod(seconds * 1000L);
    }

    private String encodeName(String artistName) {
        String encodedArtist = artistName.replaceAll("\\s", "_");
        // encodedArtist = URLEncoder.encode(encodedArtist, "UTF-8");
        // for some strange reason, wikipedia doesn't encode / when it
        // is embedded in an artist name (like ac/dc)-
        encodedArtist = encodedArtist.replace("%2F", "/");
        return encodedArtist;

    }
    // we have a URL to the proper wiki page, we want to get
    // the fragment for the artist so we can get to the Special:Export page
    //
    private String getArtistFromURL(String wikiurl) {
        String artistName = wikiurl;
        if (artistName.startsWith("http://en.wikipedia.org/wiki/")) {
            artistName = artistName.replace("http://en.wikipedia.org/wiki/", "");
        } else {
            int lastSlash = wikiurl.lastIndexOf("/");
            if (lastSlash != -1) {
                artistName = wikiurl.substring(lastSlash + 1);
            }
        }
        return artistName;
    }

    public WikiInfo getWikiInfo(String wikiurl) throws IOException {
        WikiInfo info = new WikiInfo(wikiurl);
        String description = getArtistDescription(wikiurl);
        if (description != null) {
            info.setSummary(extractSummary(wikiurl, description));
            info.setFullText(description);
        }
        return info;
    }

    private String extractSummary(String url, String description) {
        if (description != null) {
            description = wikiTextToHtml(description.trim());
            String[] lines = description.split("\\n");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith(":''")) {
                    continue;
                }
                sb.append(line);
                sb.append("\n");
                if (sb.length() > 1024) {
                    break;
                }
            }
            // HACK .... we don't have control over the target window here.'
            return sb.toString().trim() + " <a href=\"" + url + "\" target=\"Window1\" >More...</a>";
        } else {
            return "None available";
        }
    }

    public String getSummaryDescription(String wikiurl) {
        try {
            return extractSummary(wikiurl, getArtistDescription(wikiurl));
        } catch (IOException ioe) {
            return "None available.";
        }
    }

    static String filterNestedText(String text, String start, String end) {
        int inCount = 0;
        int i = 0;
        StringBuilder sb = new StringBuilder();

        while (i < text.length()) {
            if (text.regionMatches(i, start, 0, start.length())) {
                inCount++;
                i += start.length();
            } else if (inCount > 0 && text.regionMatches(i, end, 0, end.length())) {
                inCount--;
                i += end.length();
            } else {
                if (inCount == 0) {
                    sb.append(text.charAt(i));
                }
                i++;
            }
        }
        return sb.toString();
    }

    static String filterNestedText(String text, String beginPattern, String start, String end) {
        int inCount = 0;
        int i = 0;
        StringBuilder sb = new StringBuilder();

        while (i < text.length()) {
            if (text.regionMatches(true, i, beginPattern, 0, beginPattern.length())) {
                inCount++;
                i += start.length();

                while (i < text.length()) {
                    if (text.regionMatches(true, i, start, 0, start.length())) {
                        inCount++;
                        i += start.length();
                    } else if (inCount > 0 && text.regionMatches(true, i, end, 0, end.length())) {
                        inCount--;
                        i += end.length();
                    } else {
                        i++;
                    }
                }
            } else {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
    private static Pattern bracketPattern = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern h3Pattern = Pattern.compile("===(.*?)===", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern h2Pattern = Pattern.compile("==(.*?)==", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern h1Pattern = Pattern.compile("=(.*?)=", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern boldPattern = Pattern.compile("'''(.*?)'''", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern singleBracketPattern = Pattern.compile("\\[(.*?)\\]", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern doubleBracketPattern = Pattern.compile("\\[\\[([^|]*?)\\]\\]", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern complexBracketPattern = Pattern.compile("\\[\\[([^|]*?)\\|(.*?)\\]\\]", Pattern.MULTILINE | Pattern.DOTALL);
    private static Pattern refPattern = Pattern.compile("<ref.*?</ref>", Pattern.MULTILINE | Pattern.DOTALL);    //	[[Image:Wiki.png|thumb|Caption text]]
    private static Pattern imageBracketPattern = Pattern.compile("\\[\\[Image:.*?\\|.*?\\|.*?\\]\\]", Pattern.MULTILINE | Pattern.DOTALL);

    static String wikiTextToHtml(String text) {

        text = filterNestedText(text, "{{", "}}");
        text = filterNestedText(text, "{|", "|}");
        text = filterNestedText(text, "[[Image", "[[", "]]");

        {
            Matcher m = h3Pattern.matcher(text);
            text = m.replaceAll("<b>$1</b>");
        }

        {
            Matcher m = h2Pattern.matcher(text);
            text = m.replaceAll("<h3>$1</h3>");
        }

        {
            Matcher m = h1Pattern.matcher(text);
            text = m.replaceAll("<h2>$1</h2>");
        }

        {
            Matcher m = refPattern.matcher(text);
            text = m.replaceAll("");
        }

        {
            Matcher m = boldPattern.matcher(text);
            text = m.replaceAll("<b>$1</b>");
        }

        {
            Matcher m = doubleBracketPattern.matcher(text);
            text = m.replaceAll("$1");
        }

        {
            Matcher m = complexBracketPattern.matcher(text);
            text = m.replaceAll("$2");
        }

        {
            Matcher m = singleBracketPattern.matcher(text);
            text = m.replaceAll("");
        }

        return text;
    }

    static void patternTest(String t) {
        System.out.println(wikiTextToHtml(t));
    }

    static void filterTest() {
        System.out.println(filterNestedText("{{ never see}} some other text {{ here {{ nested {{ very deep }}}}}}", "{{", "}}"));
    }

    static void wikiTest() throws IOException {
        Wikipedia wikipedia = new Wikipedia();
        System.out.println("Weezer: " + wikipedia.getSummaryDescription("Foo Fighters"));
        System.out.println("Weezer: " + wikipedia.getSummaryDescription("Weezer"));
        System.out.println("Beatles: " + wikipedia.getSummaryDescription("The_Beatles"));
        System.out.println("Miles: " + wikipedia.getSummaryDescription("Miles_Davis"));
    }

    private String flatten(String s) {
        if (s != null) {
            s = s.replaceAll("\\W", " ");
        }
        return s;
    }

    public String getArtistText(String artist) throws IOException {
        return flatten(getArtistDescription(artist));
    }

    public static void main(String[] args) throws IOException {
        Wikipedia wikipedia = new Wikipedia();
        // System.out.println("Nirvana: " +  wikipedia.getSummaryDescription("http://en.wikipedia.org/wiki/Special:Export/Nirvana_(band)"));

        System.out.println(wikipedia.getArtistText("David_Bowie"));
        System.out.println(wikipedia.getArtistText("weezer"));

    /*
    System.out.println(info);
    info = wikipedia.getWikiInfo("Eminem");
    System.out.println(info);
    System.out.println("Bowie");
    info = wikipedia.getWikiInfo("David_Bowie");
    System.out.println(info);
     * */
    }
}
