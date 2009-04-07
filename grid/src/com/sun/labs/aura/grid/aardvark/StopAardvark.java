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

package com.sun.labs.aura.grid.aardvark;

import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.aura.grid.*;
import com.sun.caroline.platform.ProcessRegistration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

/**
 *
 */
public class StopAardvark extends Aardvark {

    @Override
    public String serviceName() {
        return "StopAardvark";
    }

    private void stopAardvarkProcesses() {
        try {
            gu.stopProcess(getAAName());
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error stopping Aardvark", ex);
        }
        for(int i = 0; i < 6; i++) {
            try {
                gu.stopProcess(getFMName(i));
            } catch(Exception ex) {
                logger.log(Level.SEVERE, "Error stopping feed manager " + i, ex);
            }
        }

        try {

            gu.stopProcess(getSchedName());
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error stopping feed scheduler", ex);
        }

        try {
            gu.stopProcess(getRecName());
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error stopping recommender manager", ex);
        }

        try {
            gu.waitForFinish();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error waiting for processes to finish", ex);
        }

    }

    public void start() {
        stopAardvarkProcesses();
    }

    public void stop() {
    }
}
