/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.util;

/**
 *
 * @author plamere
 */
public abstract class Commander extends Thread {

    private String name;
    private long executeTime;
    private Exception e = null;

    public Commander(String name) {
        setName(name);
    }

    @Override
    public final void run() {
        long startTime = System.currentTimeMillis();
        try {
        go();
        } catch (Exception ex) {
            e = ex;
        }
        executeTime = System.currentTimeMillis() - startTime;
    }

    Exception getException() {
        return e;
    }

    public abstract void go() throws Exception;

    public long getExecuteTime() {
        return executeTime;
    }
}