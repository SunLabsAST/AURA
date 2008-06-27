/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
