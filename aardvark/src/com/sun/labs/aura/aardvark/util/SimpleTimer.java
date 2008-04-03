/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.util;

import java.util.logging.Logger;

public class SimpleTimer {
    protected Logger log = Logger.getLogger("");
    long last;
    boolean enabled;

    public SimpleTimer(boolean enabled) {
        last = System.currentTimeMillis();
        this.enabled = enabled;
    }

    public void mark(String msg) {
        if (enabled) {
            long now = System.currentTimeMillis();
            long delta = now - last;
            last = now;
            log.info(msg + ": " + delta + " ms");
        }
    }
}
