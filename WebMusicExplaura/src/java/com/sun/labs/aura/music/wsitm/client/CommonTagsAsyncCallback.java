/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.labs.aura.music.wsitm.client.ArtistListWidget.WhyButton;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;

/**
 *
 * @author mailletf
 */
public abstract class CommonTagsAsyncCallback implements AsyncCallback {

    protected WhyButton why;

    public CommonTagsAsyncCallback(WhyButton why) {
        this.why = why;
    }

    /**
     * Overwrite to get aftercallback functionality
     * @param results
     */
    public void onCallback(ItemInfo[] results) {
        why.showWhy();
    }

    public final void onSuccess(Object result) {
        ItemInfo[] results = (ItemInfo[]) result;
        TagDisplayLib.showTagCloud("Common tags", results);
        onCallback(results);

    }

    public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
    }
};
