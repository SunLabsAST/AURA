/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.util;

public class SimpleTimer {
    long last;

    public SimpleTimer() {
        last = System.currentTimeMillis();
    }

    public void mark(String msg) {
        long now = System.currentTimeMillis();
        long delta = now - last;
        last = now;
        System.out.println(msg + ": " + delta + " ms");
    }
}
