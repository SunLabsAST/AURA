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

package com.sun.labs.aura.grid.aura;

import java.util.logging.Level;

/**
 * An aura service that can be used to bring down Aura.
 */
public class StopAIOVM extends Aura {

    public String serviceName() {
        return "StopAIOVM";
    }
    
    public void start() {
        logger.info("Stopping registrations");
        try {
            gu.stopProcess(getAIOVMName());
            logger.info("Stopped AIOVM data store");
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping AIOVM data store", e);
        }

        try {
            gu.stopProcess(getReggieName());
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error stopping reggie", e);
        }

        try {
            gu.waitForFinish();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error waiting for processes to finish", e);
        }
    }

    public void stop() {
    }
}
