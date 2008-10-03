/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class Status {

    private long startTime;
    private List<ErrorDescription> errorList = new ArrayList<ErrorDescription>();

    public Status() {
        startTime = System.currentTimeMillis();
    }

    public void addError(Util.ErrorCode error, String text) {
        errorList.add(new ErrorDescription(error, text));
    }

    public void toXML(PrintWriter out) {

        out.println("<results>");
        if (isOK()) {
            out.println("    <status code=\"OK\"/>");
        } else {
            for (ErrorDescription ed : errorList) {
                out.println("    <status code=\"" + ed.getError().name() + "\">" + ed.getText() + "</status>");
            }
        }
        {
            long delta = System.currentTimeMillis() - startTime;
            out.println("    <time code=\"" + delta + "\"/>");
        }
        out.println("</results>");
    }

    public boolean isOK() {
        return errorList.size() == 0;
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
