/*
 * Logger.java
 *
 * Created on April 6, 2007, 7:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author plamere
 */
public class Logger {
    private PrintWriter pw;
    
    /** Creates a new instance of Logger */
    Logger(File path) {
        try {
            OutputStream os = new FileOutputStream(path, true);
            pw = new PrintWriter(os);
        } catch (IOException ioe) {
            System.err.println("Can't open log at " + path);
        }
    }
    
    
    synchronized void log(String who, String action, String data) {
        if (pw != null) {
            pw.printf("%d<sep>%s<sep>%s<sep>%s\n", System.currentTimeMillis(), who, action, data);
            pw.flush();
        }
    }
    
    synchronized void close() {
        if (pw != null) {
            pw.close();
            pw = null;
        }
    }
}
