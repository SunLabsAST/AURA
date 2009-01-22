/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.Constants.Bool;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class AttentionCleanup extends Worker {

    AttentionCleanup() {
        super("Attention Cleanup", "Cleans up stray test attention data");
        param("Dry run", "If true, perform the cleanup, otherwise report what would happen", Bool.values(), Bool.TRUE);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        Set<Attention> matchSet = new HashSet<Attention>();
        boolean dryRun = getParamAsBoolean(params, "Dry run");

        collectByTypeFast(mdb, matchSet, Attention.Type.PLAYED);
        collectByTypeFast(mdb, matchSet, Attention.Type.VIEWED);
        if (dryRun) {
            result.output("Dry Run, would delete " + matchSet.size() + " attention data items");
        } else {
            result.output("Deleting " + matchSet.size() + " attention data items");
            for (Attention attn : matchSet) {
                mdb.getDataStore().removeAttention(attn.getSourceKey(), attn.getTargetKey(), attn.getType());
            }
        }
    }

    private void collectByType(MusicDatabase mdb, Set<Attention> set, Attention.Type type) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setType(type);
        DBIterator<Attention> iter = mdb.getDataStore().getAttentionIterator(ac);

        try {
            while (iter.hasNext()) {
                Attention attn = iter.next();
                if (attn.getSourceKey().startsWith("TEST_USER")) {
                    set.add(attn);
                }
            }
        } finally {
            iter.close();
        }
    }

    private void collectByTypeFast(MusicDatabase mdb, Set<Attention> set, Attention.Type type) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        ac.setType(type);
        List<Attention> attns = mdb.getDataStore().getAttention(ac);
        for (Attention attn : attns) {
            if (attn.getSourceKey().startsWith("TEST_USER")) {
                set.add(attn);
            }
        }
    }
}
