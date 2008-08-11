/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget.WhyButton;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;

/**
 *
 * @author mailletf
 */
public abstract class CommonTagsAsyncCallback implements AsyncCallback<ItemInfo[]> {

    protected ClientDataManager cdm;
    protected WhyButton why;
    protected String title;

    public CommonTagsAsyncCallback(WhyButton why, String title, ClientDataManager cdm) {
        this.why = why;
        this.title = title;
        this.cdm = cdm;
    }

    public CommonTagsAsyncCallback(WhyButton why, ClientDataManager cdm) {
        this.why = why;
        this.cdm = cdm;
        this.title = "Common tags";
    }

    /**
     * Overwrite to get aftercallback functionality
     * @param results
     */
    public void onCallback(ItemInfo[] results) {
        why.showWhy();
    }

    public final void onSuccess(ItemInfo[] results) {
        TagDisplayLib.showTagCloud(title, results, cdm);
        onCallback(results);

    }

    public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
    }
};
