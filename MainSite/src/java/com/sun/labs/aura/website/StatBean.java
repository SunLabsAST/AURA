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

package com.sun.labs.aura.website;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;

/**
 * A JavaBean that holds some stats about the data store
 */
public class StatBean {
    public static DecimalFormat longForm = new DecimalFormat("###,###,###,###");
    public static DecimalFormat doubForm = new DecimalFormat("###,###,###,###.#");

    protected long numUsers = 0;
    protected long numItems = 0;
    protected long numAttn = 0;
    
    public StatBean() {
    }
    
    public StatBean(DataStore dataStore) {
        try {
            numUsers = dataStore.getItemCount(ItemType.USER);
            numItems = dataStore.getItemCount(null);
            numAttn = dataStore.getAttentionCount(null);
        } catch (AuraException e) {
            
        } catch (RemoteException e) {
            
        }
    }

    public String getNumUsers() {
        return longForm.format(numUsers);
    }

    public void setNumUsers(long numUsers) {
        this.numUsers = numUsers;
    }

    public String getNumItems() {
        return longForm.format(numItems);
    }

    public void setNumItems(long numItems) {
        this.numItems = numItems;
    }

    public String getNumAttn() {
        return longForm.format(numAttn);
    }

    public void setNumAttn(long numAttn) {
        this.numAttn = numAttn;
    }
}
