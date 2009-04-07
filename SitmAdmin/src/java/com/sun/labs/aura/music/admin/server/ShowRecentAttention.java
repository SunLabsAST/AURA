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
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.Constants.Bool;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ShowRecentAttention extends Worker {

    ShowRecentAttention() {
        super("Show Recent Attention", "Shows recent attention");
        param("src", "the source of the attention", "");
        param("tgt", "the target of the  attention", "");
        param("constrain type", "If true, constrain the type", Bool.values(), Bool.TRUE);
        param("type", "the type of attention", Attention.Type.values(), Attention.Type.VIEWED);
        param("count", "number of attentions to return", 1000);
        param("use iterator", "If true, use the iterator", Bool.values(), Bool.FALSE);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        AttentionConfig ac = new AttentionConfig();
        int count = getParamAsInt(params, "count");
        String src = getParam(params, "src");
        String tgt = getParam(params, "tgt");

        Attention.Type type = (Attention.Type) getParamAsEnum(params, "type");

        if (src.length() > 0) {
            ac.setSourceKey(src);
        }
        if (tgt.length() > 0) {
            ac.setTargetKey(tgt);
        }

        boolean constrainType = getParamAsEnum(params, "constrain type") == Bool.TRUE;
        if (constrainType) {
            ac.setType(type);
        }

        boolean useIterator = getParamAsEnum(params, "use iterator") == Bool.TRUE;

        List<Attention> attns;
        if (useIterator) {
            attns = new ArrayList<Attention>();
            DBIterator<Attention> dbIterator = mdb.getDataStore().getAttentionIterator(ac);
            try {
                while (dbIterator.hasNext()) {
                    Attention attn = dbIterator.next();
                    attns.add(attn);
                }
            } finally {
                dbIterator.close();
            }
        } else {
            attns = mdb.getDataStore().getLastAttention(ac, count);
        }
        int row = 0;
        for (Attention attn : attns) {
            Long val = attn.getNumber();
            String sval = attn.getString();

            String extra = val != null ? val.toString() : sval != null ? sval : "";
            result.output(String.format("%d %s %s %s %s %s",
                    ++row, attn.getSourceKey(), attn.getTargetKey(), attn.getType().name(),
                    extra, new Date(attn.getTimeStamp()).toString()));

        }
    }
}
