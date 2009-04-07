/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.Constants.Bool;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class RemoveAllUsers extends Worker {

    public RemoveAllUsers() {
        super("Remove All Users", "Removes all users and their associated attention data");
        param("Dry Run", "If true, show what would happen but don't actually do the deed.", Bool.values(), Bool.TRUE);
        param("maxCount", "The maximum number of users to remove (zero for all)", 100);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        boolean dryRun = getParamAsEnum(params, "Dry Run") == Bool.TRUE;
        int maxCount = getParamAsInt(params, "maxCount");

        List<String> allKeys = getListenerIDs(mdb);

        int curCount = 0;

        for (String key : allKeys) {
            Listener listener = mdb.getListener(key);
            if (listener != null) {
                result.output("Erasing listener " + listener.getName() + " key: " + listener.getKey());
                if (!dryRun) {
                    mdb.getDataStore().deleteUser(listener.getKey());
                } else {
                    result.output("Dry run, nothing erased");
                }
                if (++curCount >= maxCount && maxCount != 0) {
                    break;
                }
            }
        }
    }
}
