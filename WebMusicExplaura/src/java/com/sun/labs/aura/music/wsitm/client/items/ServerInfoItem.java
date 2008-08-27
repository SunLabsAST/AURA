/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
