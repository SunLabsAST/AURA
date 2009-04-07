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

package com.sun.labs.aura.music.wsitm.client.event;

import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget.SwapableTxtButton;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;

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
        Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                "retrieve common tags.", Popup.ERROR_LVL.NORMAL, null);

    }
};
