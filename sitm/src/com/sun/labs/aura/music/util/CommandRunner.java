/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class CommandRunner {

    private boolean trace = true;
    private boolean singleThread;
    private List<Commander> commanders = new ArrayList<Commander>();

    public CommandRunner(boolean singleThread, boolean tracing) {
        this.singleThread = singleThread;
        this.trace = tracing;
    }

    public void add(Commander c) {
        commanders.add(c);
    }

    public void go() throws Exception {
        if (singleThread) {
            for (Commander c : commanders) {
                c.run();
            }
        } else {
            for (Commander c : commanders) {
                c.start();
            }

            for (Commander c : commanders) {
                try {
                    c.join();
                } catch (InterruptedException ie) {
                }
            }
        }

        for (Commander c : commanders) {
            if (trace) {
                System.out.println("    " + c + " " + c.getExecuteTime() + " ms");
            }
            if (c.getException() != null) {
                trace(c, "exception");
                throw new Exception("while running " + c.getName(), c.getException());
            }
        }
    }

    private void trace(Commander c, String msg) {
        if (trace) {
            System.out.println("   " + msg + " " + c);
        }
    }
}
