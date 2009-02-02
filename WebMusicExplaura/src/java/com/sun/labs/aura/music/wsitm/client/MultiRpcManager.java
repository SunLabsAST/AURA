/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;

/**
 *
 * @author mailletf
 */
public class MultiRpcManager implements AsyncCallback {

    boolean inRpc = true;
    protected ArrayList<AsyncCallback> callbackList;

    public MultiRpcManager(AsyncCallback cb) {
        callbackList = new ArrayList<AsyncCallback>();
        callbackList.add(cb);
    }

    public void addCallback(AsyncCallback cb) {
        if (inRpc) {
            callbackList.add(cb);
        }
    }

    @Override
    public void onFailure(Throwable caught) {
        for (AsyncCallback cb : callbackList) {
            cb.onFailure(caught);
        }
        reset();
    }

    @Override
    public void onSuccess(Object result) {
        for (AsyncCallback cb : callbackList) {
            cb.onSuccess(result);
        }
        reset();
    }

    private void reset() {
        callbackList = null;
        inRpc = false;
    }

    public boolean isInRpc() {
        return inRpc;
    }

}
