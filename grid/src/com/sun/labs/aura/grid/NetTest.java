/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistration;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            Network network = GridUtil.createAuraNetwork(grid, instance);
            FileSystem auraDist = GridUtil.getAuraDistFS(grid, instance);
            FileSystem logFS = GridUtil.getAuraLogFS(grid, instance);

            String classpath = GridUtil.auraDistMntPnt + "/dist/aura.jar:" +
                    GridUtil.auraDistMntPnt + "/dist/aardvark.jar:" +
                    GridUtil.auraDistMntPnt + "/dist/grid.jar";

            ProcessConfiguration pc = new ProcessConfiguration();
            pc.setCommandLine(new String[]{
                        "-cp",
                        classpath,
                        "com.sun.labs.aura.grid.URLPuller"
                    });
            pc.setSystemSinks(GridUtil.logFSMntPnt + "/netTest.out", false);

            Collection<FileSystemMountParameters> mountParams =
                    new ArrayList<FileSystemMountParameters>();

            mountParams.add(
                    new FileSystemMountParameters(auraDist.getUUID(),
                    new File(GridUtil.auraDistMntPnt).getName()));
            mountParams.add(
                    new FileSystemMountParameters(logFS.getUUID(),
                    new File(GridUtil.logFSMntPnt).getName()));

            pc.setFileSystems(mountParams);
            pc.setWorkingDirectory(GridUtil.logFSMntPnt);

            // Set the addresses for the process
            List<UUID> addresses = new ArrayList<UUID>();
            NetworkAddress internal = GridUtil.getAddressFor(grid, network, instance +
                    "-netTest");
            addresses.add(internal.getUUID());
            pc.setNetworkAddresses(addresses);
            pc.setProcessExitAction(ProcessExitAction.PARK);

            NetworkAddress external = GridUtil.getExternalAddressFor(grid,
                    network, "netTester");
            GridUtil.createNAT(grid, instance,
                    external.getUUID(),
                    internal.getUUID(),
                    "netTest");
            ProcessRegistration reg =
                    GridUtil.createProcess(grid, instance + "-netTest", pc);
            GridUtil.startRegistration(reg);

        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Exception with test", ex);
        }
    }

    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
