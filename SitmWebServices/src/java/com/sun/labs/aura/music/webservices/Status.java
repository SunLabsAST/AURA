/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class Status {

    private long startTime;
    private List<ErrorDescription> errorList = new ArrayList<ErrorDescription>();
    private HttpServletRequest request;
    private Throwable throwable;

    public Status(HttpServletRequest request) {
        startTime = System.currentTimeMillis();
        this.request = request;
    }

    public void addError(Util.ErrorCode error, String text) {
        errorList.add(new ErrorDescription(error, text));
    }

    public void addError(Util.ErrorCode error, String text, Throwable t) {
        errorList.add(new ErrorDescription(error, text));
        throwable = t;
    }

    public void toXML(PrintWriter out) {

        if (request != null) {
            out.println("<request>");
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                String value = request.getParameter(name);
                out.print("    <param name=\"" + name + "\">");
                if (value != null) {
                    out.print(Util.filter(value));
                }
                out.println("</param>");
            }
            out.println("</request>");
        }

        if (isOK()) {
            out.println("<results status=\"OK\">");
        } else {
            out.println("    <results status=\"ERROR\">");
            for (ErrorDescription ed : errorList) {
                out.println("    <error code=\"" + ed.getError().name() + "\">" + ed.getText() + "</error>");
            }
        }
        {
            long delta = System.currentTimeMillis() - startTime;
            out.println("    <time ms=\"" + delta + "\"/>");
        }
        {
            String stackTrace = getStackTrace();
            if (stackTrace != null) {
                out.println(stackTrace);
            }
        }
        out.println("</results>");
    }

    public boolean isOK() {
        return errorList.size() == 0;
    }

    private String getStackTrace() {
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter out = new PrintWriter(sw);
            throwable.printStackTrace(out);
            out.flush();
            out.close();
            return "<stack>" + Util.filter(sw.toString()) + "</stack>";
        } else {
            return null;
        }
    }
}

class ErrorDescription {

    Util.ErrorCode error;
    String text;

    public ErrorDescription(ErrorCode error, String text) {
        this.error = error;
        this.text = text;
    }

    public ErrorCode getError() {
        return error;
    }

    public String getText() {
        return text;
    }
}
