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

package com.sun.labs.search.music.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Commander {

    private String name;
    private String prefix;
    private String suffix;
    private boolean trace;
    private boolean traceSends;
    private boolean log;
    private long lastCommandTime = 0;
    private long minimumCommandPeriod = 0L;
    private DocumentBuilder builder;
    private String subscriptionId;
    private PrintStream logFile;
    private int commandsSent = 0;
    private int timeout = -1;
    private int tryCount = 5;

    public Commander(String name, String prefix, String suffix) throws IOException {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        trace = Boolean.getBoolean("trace");
        traceSends = Boolean.getBoolean("traceSends");
        log = Boolean.getBoolean("log");
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Can't load parser " + e);
        }

        if (trace) {
            System.out.println("Tracing is on");
        }
        if (log) {
            String logname = name + ".log";
            try {
                logFile = new PrintStream(logname);
            } catch (IOException e) {
                System.err.println("Can't open " + logname);
            }
        }
    }

    public void setTraceSends(boolean traceSends) {
        this.traceSends = traceSends;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void setRetries(int retries) {
        tryCount = retries + 1;
        if (tryCount < 1) {
            tryCount = 1;
        }
    }

    public void showStats() {
        System.out.printf("Commands sent to %s: %d\n", name, commandsSent);
    }

    /**
     * Sets the minimum period between consecutive commands
     * @param minPeriod the minimum period.
     */
    public void setMinimumCommandPeriod(long minPeriod) {
        minimumCommandPeriod = minPeriod;
    }

    // BUG fix this threading model
    public Document sendCommand(String command) throws IOException {
        Document document = null;
        InputStream is = sendCommandRaw(command);
        commandsSent++;

        synchronized (builder) {
            try {
                document = builder.parse(is);
            } catch (SAXException e) {
                throw new IOException("SAX Parse Error " + e);
            }
        }
        is.close();

        if (trace) {
            dumpDocument(document);
        }
        return document;
    }

    private InputStream sendCommandRaw(String command) throws IOException {
        String fullCommand = prefix + command + suffix;
        if (trace || traceSends) {
            System.out.println("Sending-->     " + fullCommand);
        }
        if (logFile != null) {
            logFile.println("Sending-->     " + fullCommand);
        }

        long curGap = System.currentTimeMillis() - lastCommandTime;
        long delayTime = minimumCommandPeriod - curGap;


        delay(delayTime);

        URL url = new URL(fullCommand);
        InputStream is = null;
        for (int i = 0; i < tryCount; i++) {
            try {
                URLConnection urc = url.openConnection();

                if (getTimeout() != -1) {
                    urc.setReadTimeout(getTimeout());
                }
                is = urc.getInputStream();
                break;
            } catch (FileNotFoundException e) {
                throw (e);
            } catch (IOException e) {
                System.out.println(name + " Error: " + e + " cmd: " + command);
            }
        }

        lastCommandTime = System.currentTimeMillis();
        if (is == null) {
            throw new IOException("Can't send command");
        }
        return is;
    }

    private void delay(long time) {
        if (time < 0) {
            return;
        } else {
            try {
                Thread.sleep(time);
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * A debuging method ... dumps a domdocument to
     * standard out
     */
    static void dumpDocument(Document document) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(document);
            Result result = new StreamResult(System.out);

            // Write the DOM document to the file
            // Get Transformer
            Transformer xformer =
                    TransformerFactory.newInstance().newTransformer();
            // Write to a file

            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");
            xformer.setOutputProperty(
                    "{http://xml.apache.org/xalan}indent-amount", "4");

            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            System.out.println("TransformerConfigurationException: " + e);
        } catch (TransformerException e) {
            System.out.println("TransformerException: " + e);
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
