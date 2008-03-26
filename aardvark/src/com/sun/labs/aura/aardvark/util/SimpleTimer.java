/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.util;

public class SimpleTimer {

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
            System.out.println(msg + ": " + delta + " ms");
        }
    }
}
