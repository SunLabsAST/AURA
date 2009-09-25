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

import com.sun.caroline.platform.DuplicateNameException;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RuntimeEnvironment;
import com.sun.caroline.util.GridFinder;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class PerlTest {

    public static void main(String args[]) throws Exception {
        GridFinder gf = new GridFinder(null);
        Grid grid = gf.findGrid(0);
        System.out.println(String.format("Got grid %s", grid));
        String[] cmdLine = new String[]{
            GridUtil.auraDistMntPnt + "/bin/untar.pl",
            "2000"
        };

        Collection<FileSystemMountParameters> mounts =
                new ArrayList<FileSystemMountParameters>();

        mounts.add(new FileSystemMountParameters(
                grid.getFileSystem("sjg-aura.dist").getUUID(),
                "auraDist"));
//        mounts.add(new FileSystemMountParameters(
//                grid.getFileSystem("test").getUUID(),
//                "data"));
        mounts.add(new FileSystemMountParameters(
                grid.getFileSystem("dev-tools").getUUID(),
                "dev-tools"));
        
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setSystemSinks("test.out", false);
        pc.setFileSystems(mounts);
        pc.setProcessExitAction(ProcessExitAction.PARK);
        pc.setRuntimeEnvironment(RuntimeEnvironment.PERL);
        pc.setHomeDirectory("/files/dev-tools");
        pc.setWorkingDirectory("/files/dev-tools");
        pc.setCommandLine(cmdLine);

        ProcessRegistration reg = null;
        String processName = "sjg-test";
        try {
            reg = grid.createProcessRegistration(processName, pc);
            System.out.println("Created process registration: " + processName);
        } catch(DuplicateNameException dne) {
            reg = grid.getProcessRegistration(processName);
            if(reg == null) {
                throw new NullPointerException("Failed to retreive existing registration: " +
                        processName);
            }

            //
            // Make sure this registration is using the config passed in.
            reg.changeConfiguration(pc);
        }
        reg.start(true);
   }
}
