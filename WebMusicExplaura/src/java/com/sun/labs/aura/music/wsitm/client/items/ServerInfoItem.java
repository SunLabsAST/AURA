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

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public class ServerInfoItem implements IsSerializable {

    private HashMap<String, Integer> cacheStatus;
    private HashMap<String, Integer> attentionCnt;
    private HashMap<String, Integer> itemCnt;
    
    private int dataStoreNbrReplicants;
    private String dataStoreStatus;

    public ServerInfoItem() {}

    public void setCacheStatus(HashMap<String, Integer> cacheStatus) {
        this.cacheStatus = cacheStatus;
    }

    public HashMap<String, Integer> getCacheStatus() {
        return cacheStatus;
    }

    public void setAttentionCnt(HashMap<String, Integer> attentionCnt) {
        this.attentionCnt = attentionCnt;
    }

    public HashMap<String, Integer> getAttentionCnt() {
        return attentionCnt;
    }

    public void setItemCnt(HashMap<String, Integer> itemCnt) {
        this.itemCnt = itemCnt;
    }

    public HashMap<String, Integer> getItemCnt() {
        return itemCnt;
    }

    public String getDataStoreStatus() {
        return dataStoreStatus;
    }

    public void setDataStoreStatus(String newStatus) {
        this.dataStoreStatus = newStatus;
    }

    public void setDataStoreNbrReplicants(int nbrReplicants) {
        this.dataStoreNbrReplicants = nbrReplicants;
    }

    public int getDataStoreNbrReplicants() {
        return dataStoreNbrReplicants;
    }
}
