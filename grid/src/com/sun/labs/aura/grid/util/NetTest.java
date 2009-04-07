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

package com.sun.labs.aura.grid.util;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.aura.grid.ServiceAdapter;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 */
public class NetTest extends ServiceAdapter {

    @Override
    public String serviceName() {
        return "NetTest";
    }

    public void start() {
        try {
            Network network = gu.getNetwork();
            FileSystem auraDist = gu.getAuraDistFS();
            FileSystem logFS = gu.getAuraLogFS();

            String classpath = GridUtil.auraDistMntPnt + "/dist/aura.jar:" +
                    GridUtil.auraDistMntPnt + "/dist/aardvark.jar:" +
                    GridUtil.auraDistMntPnt + "/dist/grid.jar";

            ProcessConfiguration pc = gu.getProcessConfig(new String[]{
                        "-cp",
                        classpath,
                        "com.sun.labs.aura.grid.util.URLPuller"
                    }, "netTest");

            // Set the addresses for the process
            UUID internal = pc.getNetworkAddresses().iterator().next();
            NetworkAddress external = gu.getExternalAddressFor("netTester");
            gu.createNAT(external.getUUID(),
                    internal,
                    "netTest");
            ProcessRegistration reg =
                    gu.createProcess("netTest", pc);
            gu.startRegistration(reg);

        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Exception with test", ex);
        }
    }

    public void stop() {
    }
}
