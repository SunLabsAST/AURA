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

import com.sun.caroline.platform.NetworkAddress;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a set of network resources on the grid that can be used to
 * connect to the VPN.  For details on how to connect after running this,
 * see https://www.projectcaroline.net/main/index.php?q=node/217
 *
 * The group name will be the name of the network (e.g. live-auraNet).
 */
public class CreateVPN extends Aura {

    protected Logger logger;

    @ConfigInteger(defaultValue=3)
    public static String PROP_NUM_ADDRESSES = "numAddresses";
    protected int numAddresses;

    public CreateVPN() {
    }

    /**
     * Create the VPN
     */
    public void createVPN() throws Exception {
        //
        // First, create the internal addresses that we'll use for clients,
        // making NAT rules for them as we go.  This also creates host names.
        List<UUID> intAddrs = new ArrayList<UUID>();
        //NetworkAddress extAddr = gu.getExternalAddressFor("vpn-nat");
        for (int i = 1; i <= numAddresses; i++) {
            NetworkAddress addr = gu.getAddressFor("vpn-" + i);
            //gu.createNAT(extAddr.getUUID(), addr.getUUID(), "vpn-nat-" + i);
            intAddrs.add(addr.getUUID());
        }

        //
        // Now create the VPN with those addresses
        gu.createVPN(intAddrs);
    }

    /**
     * Starts this service.  In this case, that means we should create the
     * VPN.
     */
    public void start() {
        try {
            createVPN();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "CreateVPN failed", e);
        }
    }

    public void stop() {
        
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        logger = ps.getLogger();
        numAddresses = ps.getInt(PROP_NUM_ADDRESSES);
    }

    @Override
    public String serviceName() {
        return "createVPN";
    }
}
