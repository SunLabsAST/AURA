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

package com.sun.labs.aura.aardvark.util;

import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemEvent;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;


/**
 *
 * @author plamere
 */
public class AardvarkStatusReporter implements Configurable, AuraService, ItemListener {

    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;

    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);

        try {
            ItemListener exportedItemListener = (ItemListener) ps.getConfigurationManager().getRemote(this, dataStore);
            dataStore.addItemListener(ItemType.FEED, exportedItemListener);
            // dataStore.addItemListener(ItemType.BLOGENTRY, exportedItemListener);

        } catch (RemoteException ex) {
            throw new PropertyException(ps.getInstanceName(),
                    PROP_DATA_STORE, "remote exception " + ex.getMessage());
        } catch (AuraException ex) {
            throw new PropertyException(ps.getInstanceName(),
                    PROP_DATA_STORE, "aura exception " + ex.getMessage());
        }
    }

    public void start() {
        System.out.println("AardvarkStatusReporter started");
    }

    public void stop() {
        System.out.println("AardvarkStatusReporter stopped");
    }

    public void itemCreated(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            System.out.println("============ new ========= ");
            System.out.println(ItemAdapter.toString(item));
        }
    }

    public void itemChanged(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            System.out.println("============ mod ========= ");
            System.out.println(ItemAdapter.toString(item));
        }
    }

    public void itemDeleted(ItemEvent e) throws RemoteException {
        for (Item item : e.getItems()) {
            System.out.println("============ del ========= ");
            System.out.println(ItemAdapter.toString(item));
        }
    }
}
