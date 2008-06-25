package com.sun.labs.aura.grid.util;

import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class URLPuller {

    public static void main(String[] args) {
        while(true) {
            try {
                URL u = new URL("http://www.sun.com/robots.txt");
                URLConnection uc = u.openConnection();
                uc.connect();
                String type = uc.getContentType();
                Logger.getLogger("").info("content type: " + type);
            } catch(Exception ex) {
                Logger.getLogger(URLPuller.class.getName()).log(Level.SEVERE,
                        "Error!",
                        ex);
            }
        }
    }
}
