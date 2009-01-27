/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget.SwapableTxtButton;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib.TagColorType;

/**
 *
 * @author mailletf
 */
public abstract class CommonTagsAsyncCallback implements AsyncCallback<ItemInfo[]> {

    protected ClientDataManager cdm;
    protected SwapableTxtButton why;
    protected String title;

    public CommonTagsAsyncCallback(SwapableTxtButton why, String title, ClientDataManager cdm) {
        this.why = why;
        this.title = title;
        this.cdm = cdm;
    }

    public CommonTagsAsyncCallback(SwapableTxtButton why, ClientDataManager cdm) {
        this.why = why;
        this.cdm = cdm;
        this.title = "Common tags";
    }

    /**
     * Overwrite to get aftercallback functionality
     * @param results
     */
    public void onCallback(ItemInfo[] results) {
        why.showButton();
    }

    @Override
    public final void onSuccess(ItemInfo[] results) {
        TagDisplayLib.showTagCloud(title, results, TagDisplayLib.ORDER.SHUFFLE, cdm);
        onCallback(results);
    }

    @Override
    public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
    }
};
