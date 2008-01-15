/*
 * Utilities.java
 *
 * Created on February 13, 2007, 8:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.search.music.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author plamere
 */
public class Utilities {

    /** Creates a new instance of Utilities */
    private Utilities() {
    }
    /**
     * Implements normalization rules described at
     * http://www.ee.columbia.edu/~dpwe/research/musicsim/normalization.html
     *
     * Artist names are particularly important to get normalized
     * to the same forms.
     * Hence, they have severe normalization:
     *
     * 1. Names are all mapped to lower case
     * 2. Delete apostrophes ("'") and periods (".").
     * 3. Everything else except a-z 0-9 maps to "_".
     *   - but this doesn't work for non-english titles
     * 3A Multiple _'s in sequence fold into a single _.
     * 3B Leading and trailing _'s are dropped.
     * 4. Don't reorder proper names - it's just too hard,
     *    and there's no clear boundary between proper names and band names. .
     * 5. Always drop a leading "the".
     * 5A Always drop a leading indefinite article too
     *
     *
     * Augmented these rules with:
     *   ampersands (&) are replaced with 'and'
     *
     * Issues:
     *    Number folding - '3' vs. 'three'
     *    Non english names
     */
    static Pattern deletedChars = Pattern.compile("[\"'.]");
    static Pattern ampersand = Pattern.compile("&");
    static Pattern everythingBut = Pattern.compile("[^\\p{Alnum}]");
    static Pattern leadingDash = Pattern.compile("^_+");
    static Pattern trailingDash = Pattern.compile("_+$");
    static Pattern leadingThe = Pattern.compile("^the\\s");
    static Pattern trailingThe = Pattern.compile("\\sthe$");
    static Pattern leadingA = Pattern.compile("^a\\s");
    static Pattern trailingA = Pattern.compile("\\sa$");
    static Pattern multiDash = Pattern.compile("_{2,}");
    static Pattern unprintable = Pattern.compile("[^\\p{Print}]");
    static Pattern punctuation = Pattern.compile("[\\p{Punct}]");

    public static String printable(String in) {
        return unprintable.matcher(in).replaceAll("");
    }
    /*
     * 1. Names are all mapped to lower case
     * 2. Delete apostrophes ("'") and periods (".").
     *     2+ PBL added '"'
     * 3. Everything else except a-z 0-9 maps to "_".
     *   - but this doesn't work for non-english titles
     *   - switch this to mapping them to ""
     * 3A Multiple _'s in sequence fold into a single _.
     * 3B Leading and trailing _'s are dropped.
     * 4. Don't reorder proper names - it's just too hard,
     *    and there's no clear boundary between proper names and band names. .
     * 5. Always drop a leading "the".
     * 5A Always drop a leading indefinite article too
     */

    public static String normalize(String in) {
        String s;
        if (in == null) {
            return "";
        }

        s = in.trim();
        s = s.toLowerCase();
        s = deletedChars.matcher(s).replaceAll("");
        s = ampersand.matcher(s).replaceAll(" and ");
        s = leadingDash.matcher(s).replaceAll("");
        s = trailingDash.matcher(s).replaceAll("");
        s = leadingThe.matcher(s).replaceAll("");
        s = trailingThe.matcher(s).replaceAll("");
        s = leadingA.matcher(s).replaceAll("");
        s = trailingA.matcher(s).replaceAll("");
        s = multiDash.matcher(s).replaceAll("_");
        s = everythingBut.matcher(s).replaceAll("");

        // if we've reduced the input down to nothing
        // fall back on input (necessary for non western
        // names

        if (s.length() == 0) {
            s = in;
        }

        //System.out.println(in + " BECOMES " + s);
        return s;
    }
    //static Pattern specialChars = Pattern.compile("[ -/:-@\\[-`]");
    //static Pattern specialChars = Pattern.compile("[<>/\\!#\\$]");
    static Pattern specialChars = Pattern.compile("[\\&,\\[\\]@\\-\\(\\)<>/\\!#\\$]");
    static Pattern the = Pattern.compile("\\s+the\\s+");
    static Pattern indefiniteArticle = Pattern.compile("\\s+a\\s+");
    static Pattern andPattern = Pattern.compile("\\s+and\\s+");
    static Pattern orPattern = Pattern.compile("\\s+or\\s+");
    static Pattern disks1 = Pattern.compile("dis[ck][\\s+][123456789]");
    static Pattern disks2 = Pattern.compile("dis[ck][\\s+]one|two|three|four|five");
    static Pattern disks3 = Pattern.compile("[cC][dD]\\s*[1234567]");
    static Pattern leadingThe1 = Pattern.compile("^the\\s+");
    static Pattern trailingThe1 = Pattern.compile("\\s+the$");
    static Pattern leadingAnd = Pattern.compile("^and\\s+$");
    static Pattern trailingAnd = Pattern.compile("\\s+and$$");
    static Pattern parens = Pattern.compile("\\(.*\\)");

    public static String normalizeForSearch(String in) {
        String s;
        if (in == null) {
            return "";
        }

        s = in.trim();
        s = s.toLowerCase();
        s = parens.matcher(s).replaceAll("");
        s = specialChars.matcher(s).replaceAll(" ");
        s = the.matcher(s).replaceAll(" ");
        s = indefiniteArticle.matcher(s).replaceAll(" ");
        s = andPattern.matcher(s).replaceAll(" ");
        s = orPattern.matcher(s).replaceAll(" \"or\" ");
        s = disks1.matcher(s).replaceAll(" ");
        s = disks2.matcher(s).replaceAll(" ");
        s = disks3.matcher(s).replaceAll(" ");
        s = leadingThe1.matcher(s).replaceAll(" ");
        s = trailingThe1.matcher(s).replaceAll(" ");
        s = leadingAnd.matcher(s).replaceAll(" ");
        s = trailingAnd.matcher(s).replaceAll(" ");
        return s;
    }

    /**
     * returns a quoted version of s with all internal quotes escaped
     */
    static String quote(String s) {
        String escaped = s.replaceAll("\\\"", "\\\\\"");
        return "\"" + escaped + "\"";
    }
    private static Map<String, String> genreMap;

    public static String collapseGenre(String genre) {
        if (genreMap == null) {
            genreMap = new HashMap<String, String>();
            genreMap.put("acid", "rock");
            genreMap.put("alternative", "rock");
            genreMap.put("alternative_and_punk", "rock");
            genreMap.put("alternrock", "rock");
            genreMap.put("ambient", "ambient");
            genreMap.put("baseball", "other");
            genreMap.put("blues", "blues");
            genreMap.put("blues_rock", "rock");
            genreMap.put("brit_pop", "pop");
            genreMap.put("celtic", "world");
            genreMap.put("classical", "classical");
            genreMap.put("classic_rock", "rock");
            genreMap.put("country", "country");
            genreMap.put("dance", "electronica");
            genreMap.put("disco", "pop");
            genreMap.put("easy_listening", "pop");
            genreMap.put("electronic", "electronica");
            genreMap.put("electronica_and_dance", "electronica");
            genreMap.put("ethnic", "world");
            genreMap.put("folk", "folk");
            genreMap.put("folklore", "folk");
            genreMap.put("folk_rock", "folk");
            genreMap.put("general_blues", "blues");
            genreMap.put("general_pop", "pop");
            genreMap.put("general_unclassifiable", "other");
            genreMap.put("grunge", "rock");
            genreMap.put("hard_rock", "rock");
            genreMap.put("hip_hop", "rap");
            genreMap.put("humor", "other");
            genreMap.put("industrial", "electronica");
            genreMap.put("instrumental", "rock");
            genreMap.put("jazz", "jazz");
            genreMap.put("jazz_instrument", "jazz");
            genreMap.put("jazz_west_coast", "jazz");
            genreMap.put("latin", "world");
            genreMap.put("live_rock", "rock");
            genreMap.put("mash_up", "rock");
            genreMap.put("metal", "rock");
            genreMap.put("musical", "pop");
            genreMap.put("newage", "world");
            genreMap.put("new_age", "world");
            genreMap.put("newfie", "folk");
            genreMap.put("no_genre", "other");
            genreMap.put("norwegian_folk", "folk");
            genreMap.put("oldies", "pop");
            genreMap.put("other", "other");
            genreMap.put("pop", "pop");
            genreMap.put("progressive_rock", "rock");
            genreMap.put("punk", "rock");
            genreMap.put("punk_rock", "rock");
            genreMap.put("r_and_b", "rap");
            genreMap.put("rap", "rap");
            genreMap.put("reggae", "other");
            genreMap.put("retro", "rock");
            genreMap.put("rock", "rock");
            genreMap.put("rock_pop", "rock");
            genreMap.put("rock_and_roll", "rock");
            genreMap.put("slow_rock", "rock");
            genreMap.put("soft_rock", "rock");
            genreMap.put("soundtrack", "other");
            genreMap.put("techno", "electronica");
            genreMap.put("trance", "electronica");
            genreMap.put("trip_hop", "rap");
            genreMap.put("unclassifiable", "other");
            genreMap.put("vocal", "pop");
            genreMap.put("unknown", "other");
        }
        String normalizedGenre = normalize(genre);
        String mappedGenre = genreMap.get(normalizedGenre);
        if (mappedGenre == null) {
            if (normalizedGenre.contains("rock")) {
                return "rock";
            }
            return genre;
        } else {
            return mappedGenre;
        }
    }

    public static String normalizeFilename(File file)
            throws MalformedURLException {
        return file.toURI().toURL().getFile().toString();
    }

    public static long binaryCopy(URL src, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        int byteCount = 0;
        if (!dest.exists()) {
            if (src == null) {
                dest.createNewFile();
            } else {
                try {
                    os = new BufferedOutputStream(new FileOutputStream(dest));
                    URLConnection urc = src.openConnection();
//                    urc.setReadTimeout(10000);
                    urc.setRequestProperty("User-Agent", "Mozilla/4.0");
                    is = new BufferedInputStream(urc.getInputStream());
                    int b;

                    while ((b = is.read()) != -1) {
                        os.write(b);
                        byteCount++;
                    }

                } finally {
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                }
            }
        }
        return byteCount;
    }

    public static long binaryCopyWget(URL src, File dest) throws IOException {
        String wgetPath = System.getProperty("wget");
        if (wgetPath == null) {
            wgetPath = "wget";
        }
        String cmd = wgetPath + " -qU Squeezebox " + src + " -O " + dest;
        Process process = Runtime.getRuntime().exec(cmd);
        try {
            int status = process.waitFor();
            if (status != 0) {
                throw new IOException("binary copy return non zero status " + status);
            }
        } catch (InterruptedException ioe) {
        }
        return dest.length();
    }

    public static void log(String s) {
        System.out.println("   " + s);
    }

    public static void err(String s) {
        System.out.println(" ERR  " + s);
    }

    public static String jam(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    
    // The following code, adapted from Purple Technology, Inc. 

    static Object[][] entities = {
       // {"#39", new Integer(39)},       // ' - apostrophe
        {"quot", new Integer(34)},      // " - double-quote
        {"amp", new Integer(38)},       // & - ampersand
        {"lt", new Integer(60)},        // < - less-than
        {"gt", new Integer(62)},        // > - greater-than
        {"nbsp", new Integer(160)},     // non-breaking space
        {"copy", new Integer(169)},     // © - copyright
        {"reg", new Integer(174)},      // ® - registered trademark
        {"Agrave", new Integer(192)},   // À - uppercase A, grave accent
        {"Aacute", new Integer(193)},   // Á - uppercase A, acute accent
        {"Acirc", new Integer(194)},    // Â - uppercase A, circumflex accent
        {"Atilde", new Integer(195)},   // Ã - uppercase A, tilde
        {"Auml", new Integer(196)},     // Ä - uppercase A, umlaut
        {"Aring", new Integer(197)},    // Å - uppercase A, ring
        {"AElig", new Integer(198)},    // Æ - uppercase AE
        {"Ccedil", new Integer(199)},   // Ç - uppercase C, cedilla
        {"Egrave", new Integer(200)},   // È - uppercase E, grave accent
        {"Eacute", new Integer(201)},   // É - uppercase E, acute accent
        {"Ecirc", new Integer(202)},    // Ê - uppercase E, circumflex accent
        {"Euml", new Integer(203)},     // Ë - uppercase E, umlaut
        {"Igrave", new Integer(204)},   // Ì - uppercase I, grave accent
        {"Iacute", new Integer(205)},   // Í - uppercase I, acute accent
        {"Icirc", new Integer(206)},    // Î - uppercase I, circumflex accent
        {"Iuml", new Integer(207)},     // Ï - uppercase I, umlaut
        {"ETH", new Integer(208)},      // ? - uppercase Eth, Icelandic
        {"Ntilde", new Integer(209)},   // Ñ - uppercase N, tilde
        {"Ograve", new Integer(210)},   // Ò - uppercase O, grave accent
        {"Oacute", new Integer(211)},   // Ó - uppercase O, acute accent
        {"Ocirc", new Integer(212)},    // Ô - uppercase O, circumflex accent
        {"Otilde", new Integer(213)},   // Õ - uppercase O, tilde
        {"Ouml", new Integer(214)},     // Ö - uppercase O, umlaut
        {"Oslash", new Integer(216)},   // Ø - uppercase O, slash
        {"Ugrave", new Integer(217)},   // Ù - uppercase U, grave accent
        {"Uacute", new Integer(218)},   // Ú - uppercase U, acute accent
        {"Ucirc", new Integer(219)},    // Û - uppercase U, circumflex accent
        {"Uuml", new Integer(220)},     // Ü - uppercase U, umlaut
        {"Yacute", new Integer(221)},   // ? - uppercase Y, acute accent
        {"THORN", new Integer(222)},    // ? - uppercase THORN, Icelandic
        {"szlig", new Integer(223)},    // ß - lowercase sharps, German
        {"agrave", new Integer(224)},   // à - lowercase a, grave accent
        {"aacute", new Integer(225)},   // á - lowercase a, acute accent
        {"acirc", new Integer(226)},    // â - lowercase a, circumflex accent
        {"atilde", new Integer(227)},   // ã - lowercase a, tilde
        {"auml", new Integer(228)},     // ä - lowercase a, umlaut
        {"aring", new Integer(229)},    // å - lowercase a, ring
        {"aelig", new Integer(230)},    // æ - lowercase ae
        {"ccedil", new Integer(231)},   // ç - lowercase c, cedilla
        {"egrave", new Integer(232)},   // è - lowercase e, grave accent
        {"eacute", new Integer(233)},   // é - lowercase e, acute accent
        {"ecirc", new Integer(234)},    // ê - lowercase e, circumflex accent
        {"euml", new Integer(235)},     // ë - lowercase e, umlaut
        {"igrave", new Integer(236)},   // ì - lowercase i, grave accent
        {"iacute", new Integer(237)},   // í - lowercase i, acute accent
        {"icirc", new Integer(238)},    // î - lowercase i, circumflex accent
        {"iuml", new Integer(239)},     // ï - lowercase i, umlaut
        {"igrave", new Integer(236)},   // ì - lowercase i, grave accent
        {"iacute", new Integer(237)},   // í - lowercase i, acute accent
        {"icirc", new Integer(238)},    // î - lowercase i, circumflex accent
        {"iuml", new Integer(239)},     // ï - lowercase i, umlaut
        {"eth", new Integer(240)},      // ? - lowercase eth, Icelandic
        {"ntilde", new Integer(241)},   // ñ - lowercase n, tilde
        {"ograve", new Integer(242)},   // ò - lowercase o, grave accent
        {"oacute", new Integer(243)},   // ó - lowercase o, acute accent
        {"ocirc", new Integer(244)},    // ô - lowercase o, circumflex accent
        {"otilde", new Integer(245)},   // õ - lowercase o, tilde
        {"ouml", new Integer(246)},     // ö - lowercase o, umlaut
        {"oslash", new Integer(248)},   // ø - lowercase o, slash
        {"ugrave", new Integer(249)},   // ù - lowercase u, grave accent
        {"uacute", new Integer(250)},   // ú - lowercase u, acute accent
        {"ucirc", new Integer(251)},    // û - lowercase u, circumflex accent
        {"uuml", new Integer(252)},     // ü - lowercase u, umlaut
        {"yacute", new Integer(253)},   // ? - lowercase y, acute accent
        {"thorn", new Integer(254)},    // ? - lowercase thorn, Icelandic
        {"yuml", new Integer(255)},     // ÿ - lowercase y, umlaut
        {"euro", new Integer(8364)},    // Euro symbol
    };
    static Map e2i = new HashMap();
    static Map i2e = new HashMap();
    static {
        for (int i=0; i<entities.length; ++i) {
            e2i.put(entities[i][0], entities[i][1]);
            i2e.put(entities[i][1], entities[i][0]);
        }
    }

    /**
     * Turns funky characters into HTML entity equivalents<p>
     * e.g. <tt>"bread" & "butter"</tt> => <tt>&amp;quot;bread&amp;quot; &amp;amp; &amp;quot;butter&amp;quot;</tt>.
     * Update: supports nearly all HTML entities, including funky accents. See the source code for more detail.
     * @see #htmlunescape(String)
     **/
    public static String htmlEscape(String s1) {
        StringBuffer buf = new StringBuffer();
        int i;
        for (i = 0; i < s1.length(); ++i) {
            char ch = s1.charAt(i);
            String entity = (String) i2e.get(new Integer((int) ch));
            if (entity == null) {
                if (((int) ch) > 128) {
                    buf.append("&#" + ((int) ch) + ";");
                } else {
                    buf.append(ch);
                }
            } else {
                buf.append("&" + entity + ";");
            }
        }
        return buf.toString();
    }

    /**
     * Given a string containing entity escapes, returns a string
     * containing the actual Unicode characters corresponding to the
     * escapes.
     *
     * Note: nasty bug fixed by Helge Tesgaard (and, in parallel, by
     * Alex, but Helge deserves major props for emailing me the fix).
     * 15-Feb-2002 Another bug fixed by Sean Brown <sean@boohai.com>
     *
     * @see #htmlescape(String)
     **/
    public static String htmlUnescape(String s1) {
        StringBuffer buf = new StringBuffer();
        int i;
        for (i = 0; i < s1.length(); ++i) {
            char ch = s1.charAt(i);
            if (ch == '&') {
                int semi = s1.indexOf(';', i + 1);
                if (semi == -1) {
                    buf.append(ch);
                    continue;
                }
                String entity = s1.substring(i + 1, semi);
                Integer iso;
                if (entity.charAt(0) == '#') {
                    iso = new Integer(entity.substring(1));
                } else {
                    iso = (Integer) e2i.get(entity);
                }
                if (iso == null) {
                    buf.append("&" + entity + ";");
                } else {
                    buf.append((char) (iso.intValue()));
                }
                i = semi;
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }
    
    public static String XMLEscape(String s) {
        // To allow attribute values to contain both single and double quotes, 
        // the apostrophe or single-quote character (') may be represented as 
        // "&apos;", and the double-quote character (") as "&quot;".
        s = s.replaceAll("'", "&apos;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

    public static String XMLUnescape(String s) {
        s = s.replaceAll("&apos;", "'");
        s = s.replaceAll("&quot;", "\"");
        s = s.replaceAll("&amp;", "\"");
        s = s.replaceAll("&gt;", ">");
        s = s.replaceAll("&lt;", "<");
        return s;
    }
}
