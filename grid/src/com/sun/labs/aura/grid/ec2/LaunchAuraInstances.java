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

package com.sun.labs.aura.grid.ec2;

import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;

/**
 *
 */
public class LaunchAuraInstances {

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            System.err.println(String.format("Usage: LaunchInstance <image id> <keypair name>"));
            return;
        }
        String imageID = args[0];
        String keyPairName = args[1];

        EC2Grid grid = new EC2Grid();
        KeyPairInfo kpi = grid.getKeyPairInfo(keyPairName);

        //
        // Retrieve or create a key pair.

        LaunchConfiguration lc = new LaunchConfiguration(imageID);
        lc.setKeyName(keyPairName);

        //
        // Launch!
//        ReservationDescription rd = ec2.runInstances(lc);
    }
}
