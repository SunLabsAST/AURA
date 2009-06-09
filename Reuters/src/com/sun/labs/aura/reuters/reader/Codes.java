package com.sun.labs.aura.reuters.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A container for codes.
 */
public class Codes {

    private static Logger logger = Logger.getLogger(Codes.class.getName());

    public enum CodeType {

        INDUSTRIES,
        COUNTRIES,
        TOPICS,
        UNKNOWN;

        static Pattern p = Pattern.compile("bip:(countries|topics|industries)");

        public static CodeType getCodeType(String codeClass) {
            Matcher m = p.matcher(codeClass);
            if(m.find()) {
                try {
                    return CodeType.valueOf(m.group(1).toUpperCase());
                } catch(IllegalArgumentException ex) {
                    logger.warning("Unknown code class: " + codeClass);
                    return UNKNOWN;
                }
            }
            logger.warning("Non-matching code class: " + codeClass);
            return UNKNOWN;
        }
    }
    Map<String, String> codes;

    public Codes(String file) throws IOException {
        codes = new HashMap<String, String>();
        BufferedReader r = new BufferedReader(new FileReader(file));
        String l;
        String orig;
        int lineNum = 0;
        while((orig = r.readLine()) != null) {
            lineNum++;
            l = orig.trim();
            int pos = l.indexOf(';');
            if(pos >= 0) {
                l = l.substring(0, pos).trim();
            }
            if(l.length() > 0) {
                String[] vals = l.split("\t");
                if(vals.length > 2 || vals.length < 2) {
                    logger.warning(String.format(
                            "Weird line at %d in %s: \"%s\"",
                            lineNum, file, orig));
                }
                if(vals.length >= 2) {
                    codes.put(vals[0], vals[1]);
                }
            }
        }
        r.close();
    }

    public String getValue(String code) {
        String ret = codes.get(code);
        if(ret == null) {
            logger.warning("Unknown code: " + code);
        }
        return ret;
    }
}
