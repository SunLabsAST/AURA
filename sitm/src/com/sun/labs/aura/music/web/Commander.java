
/*
 * Commander.java
 *
 * Created on March 14, 2006, 8:29 PM
 *
 */
package com.sun.labs.aura.music.web;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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
    private PrintStream logFile;
    private int commandsSent = 0;
    private int timeout = -1;
    private int tryCount = 5;
    private final int DEFAULT_TIMEOUT = 60 * 1000;

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
        setTimeout(DEFAULT_TIMEOUT);
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

    public String encode(String name) {
        try {
            String encodedName = URLEncoder.encode(name, "UTF-8");
            return encodedName;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
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
            } finally {
                is.close();
            }
        }

        if (trace) {
            dumpDocument(document);
        }
        return document;
    }

    public InputStream sendCommandRaw(String command) throws IOException {
        try {
            String fullCommand = prefix + command + fixSuffix(command, suffix);

            long curGap = System.currentTimeMillis() - lastCommandTime;
            long delayTime = minimumCommandPeriod - curGap;


            delay(delayTime);

            // URL url = new URL(fullCommand);
            URI uri = new URI(fullCommand);
            URL url = uri.toURL();

            if (trace || traceSends) {
                System.out.println("Sending-->     " + url);
            }
            if (logFile != null) {
                logFile.println("Sending-->     " + url);
            }

            InputStream is = null;
            for (int i = 0; i < tryCount; i++) {
                try {
                    URLConnection urc = url.openConnection();

                    if (getTimeout() != -1) {
                        urc.setReadTimeout(getTimeout());
                        urc.setConnectTimeout(getTimeout());
                    }
                    is = new BufferedInputStream(urc.getInputStream());
                    break;
                } catch (FileNotFoundException e) {
                    throw e;
                } catch (IOException e) {
                    System.out.println(name + " Error: " + e + " cmd: " + command);
                }
            }

            lastCommandTime = System.currentTimeMillis();
            if (is == null) {
                System.out.println(name + " retry failure  cmd: " + url);
                throw new IOException("Can't send command");
            }
            return is;
        } catch (URISyntaxException ex) {
            throw new IOException("bad uri " + ex);
        }
    }

    // the suffix maybe a param that needs to start with & or ? depending
    // on whether or not this is the only parameter for the command. If the suffix
    // starts with a '&' then it is assumed to be a param, if the command doesn't have
    // any params (i.e. there's no  '?' in the command), then we replace the '&' with a '?'
    private String fixSuffix(String command, String suffix) {
        if (suffix.startsWith("&")) {
            if (command.indexOf("?") == -1) {
                suffix = suffix.replaceFirst("\\&", "?");
            }
        }
        return suffix;

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
    public static void dumpDocument(Document document) {
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

    public static String convertToString(Document document) {
        StringWriter sw = new StringWriter();
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(document);
            Result result = new StreamResult(sw);

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
        return sw.toString();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
