/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
public class RemoveUser extends Worker {

    public RemoveUser() {
        super("Remove User", "Removes a user and their associated attention data");
        param("User key", "The key for the user", "");
        param("Erase all data", "If true, also erase all associated attention data", Bool.values(), Bool.TRUE);
        param("Dry Run", "If true, show what would happen but don't actually do the deed.", Bool.values(), Bool.TRUE);
    }

    @Override
    void go( MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String key = getParam(params, "User key");
        boolean eraseAll = getParamAsEnum(params, "Erase all data") == Bool.TRUE;
        boolean dryRun = getParamAsEnum(params, "Dry Run") == Bool.TRUE;

        Listener listener = mdb.getListener(key);

        if (listener == null) {
            result.fail("Listener doesn't exisit");
            return;
        }

        result.output("Erasing listener " + listener.getName() + " key: " + listener.getKey());

        AttentionConfig ac = new AttentionConfig();
        ac.setSourceKey(key);

        List<Attention> attns = null;
        if (eraseAll) {
            attns = mdb.getDataStore().getAttention(ac);
            result.output("Erasing " + attns.size() + " attention data");
        }


        if (!dryRun) {
            if (attns != null) {
                for (Attention attn : attns) {
                    mdb.getDataStore().removeAttention(attn.getSourceKey(), attn.getTargetKey(), attn.getType());
                }

                List<Attention> leftoverAttns = mdb.getDataStore().getAttention(ac);
                if (leftoverAttns.size() > 0) {
                    result.fail("Attention delete failed after deleting user, " + leftoverAttns.size() + " attention remain.");
                    return;
                } else {
                    result.output("All attention data removed or " + listener.getKey());
                }
            }
            mdb.getDataStore().deleteUser(listener.getKey());
        } else {
            result.output("Dry run, nothing erased");
        }
    }
}
