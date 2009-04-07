/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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
    private boolean debug = false;

    Status(HttpServletRequest request) {
        startTime = System.currentTimeMillis();
        this.request = request;
    }

    void addError(Util.ErrorCode error, String text) {
        errorList.add(new ErrorDescription(error, text));
    }

    void addErrors(List<ErrorDescription> errors) {
        errorList.addAll(errors);
    }

    void addError(Util.ErrorCode error, String text, Throwable t) {
        errorList.add(new ErrorDescription(error, text));
        throwable = t;
    }
    
    void setDebug(boolean debug) {
        this.debug = debug;
    }

    void toXML(PrintWriter out) {
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

        if (debug) {
            String stackTrace = getStackTrace();
            if (stackTrace != null) {
                out.println(stackTrace);
            }
        }

        out.println("</results>");
    }

    boolean isOK() {
        return errorList.size() == 0;
    }

    int getTime() {
        return (int) (System.currentTimeMillis() - startTime);
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
