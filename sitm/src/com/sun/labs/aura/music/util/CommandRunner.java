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
                System.out.println("exception running " + c.getName());
                c.getException().printStackTrace();
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
