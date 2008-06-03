
package com.sun.labs.aura.music.webservices;

import java.io.PrintWriter;

/**
 * Some shared code across the servlets
 */
public class Util {

    enum ErrorCode { OK, MissingArgument, DataStore, Configuration};
    
    static String filter(String s) {
        if (s != null) {
            s = s.replaceAll("[^\\p{ASCII}]", "");
            s = s.replaceAll("\\&", "&amp;");
            s = s.replaceAll("\\<", "&lt;");
            s = s.replaceAll("\\>", "&gt;");
            s = s.replaceAll("[^\\p{Graph}\\p{Blank}]", "");
        }
        return s;
    }

    static void outputStatus(PrintWriter out, ErrorCode code, String message) {
        if (code == ErrorCode.OK) {
            out.println("    <Status code='OK'/>");
        } else {
            out.println("    <Status code='" + code.toString() + "'>" + message + "</Status>");
        }
    }

    static void outputStatus(PrintWriter out, String tag, ErrorCode code, String message) {
        out.println("<" + tag + ">");
        outputStatus(out, code, message);
        out.println("</" + tag + ">");
    }

    static void outputOKStatus(PrintWriter out) {
        outputStatus(out, ErrorCode.OK, null);
    }

    static Timer getTimer() {
        return new Timer();
    }
    
    static void tagOpen(PrintWriter out, String tag) {
        out.println("<" + tag + ">");
    }

    static void tagClose(PrintWriter out, String tag) {
        out.println("</" + tag + ">");
    }
}

class Timer {
    long start = System.currentTimeMillis();

    void report(PrintWriter out) {
        out.println("<!-- output generated in " + (System.currentTimeMillis() - start) + " ms -->" );
    }
}