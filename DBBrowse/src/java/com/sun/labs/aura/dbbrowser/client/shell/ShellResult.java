/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.shell;

import java.io.Serializable;

/**
 * Result of a shell call
 */
public class ShellResult implements Serializable {
    protected long time = 0;
    protected String text = "";

    public ShellResult() {
    }

    public ShellResult(long time, String text) {
        this.time = time;
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public String getText() {
        return text;
    }
}
