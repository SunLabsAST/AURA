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

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterface;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterfaceAsync;
import com.sun.labs.aura.music.wsitm.client.event.DEAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.TagDependentSharedMenu;
import java.util.ArrayList;

/**
 *
 * @author mailletf
 */
public class SharedTagMenu extends ContextMenu implements TagDependentSharedMenu {

    protected ItemInfo currTag;
    protected MusicSearchInterfaceAsync musicServer;
    protected ClientDataManager cdm;

    public SharedTagMenu(ClientDataManager tCdm) {

        super();
        initRPC();
        this.cdm = tCdm;

        addElement("View tag details", new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                History.newItem("tag:" + currTag.getId());
            }
        });

        addElement("View similar tags", new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                invokeSimTagService(currTag.getId());
            }
        });

        addElement("View representative artists", new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                invokeRepArtistService(currTag.getId());
            }
        });
                
        addSeperator();
        addElement("Add tag to current steerable tag cloud", new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                cdm.getSteerableTagCloudExternalController().addTag(currTag);
            }
        });
    }

    private void invokeRepArtistService(String tagId) {

        PopupPanel p = Popup.showLoadingPopup();

        DEAsyncCallback<PopupPanel, ArrayList<ScoredC<ArtistCompact>>> callback =
                new DEAsyncCallback<PopupPanel, ArrayList<ScoredC<ArtistCompact>>>(p) {

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve tag's representative artists.", Popup.ERROR_LVL.NORMAL, null);
            }

            @Override
            public void onSuccess(ArrayList<ScoredC<ArtistCompact>> result) {
                data.hide();
                if (result!=null || result.size()>0) {
                    ItemInfo[] iI = new ItemInfo[result.size()];
                    int i = 0;
                    for (ScoredC<ArtistCompact> sAC : result) {
                        iI[i++] = new ItemInfo(sAC.getItem().getId(),
                                sAC.getItem().getName(), sAC.getScore(),
                                sAC.getItem().getPopularity());
                    }
                    TagDisplayLib.showTagCloud("Representative artists of tag "+currTag.getItemName(),
                            iI, TagDisplayLib.ORDER.SHUFFLE, cdm, TagDisplayLib.TagColorType.TAG_POPUP);
                } else {
                    Popup.showErrorPopup("Returned list was null or empty.", Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "retrieve tag's representative artists.", Popup.ERROR_LVL.NORMAL, null);
                }
            }
        };

        try {
            musicServer.getRepresentativeArtistsOfTag(tagId, callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve tag's representative artists.", Popup.ERROR_LVL.NORMAL, null);
        }
    }


    private void invokeSimTagService(String tagId) {

        PopupPanel p = Popup.showLoadingPopup();

        DEAsyncCallback<PopupPanel, ItemInfo[]> callback =
                new DEAsyncCallback<PopupPanel, ItemInfo[]>(p) {

            public void onSuccess(ItemInfo[] iI) {
                if (iI != null || iI.length > 0) {
                    data.hide();
                    TagDisplayLib.showTagCloud("Similar tags to "+currTag.getItemName(),
                            iI, TagDisplayLib.ORDER.SHUFFLE, cdm);
                } else {
                    Popup.showErrorPopup("Returned list was null or empty.",
                            Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "retrieve similar tags.", Popup.ERROR_LVL.NORMAL, null);
                }
            }

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve similar tags.", Popup.ERROR_LVL.NORMAL, null);
            }
        };

        try {
            musicServer.getSimilarTags(tagId, callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve similar tags.", Popup.ERROR_LVL.NORMAL, null);
        }
    }

    @Override
    public void showAt(Event e, ItemInfo currTag) {
        this.currTag = currTag;
        super.showAt(e);
    }

    private final void initRPC() {
        musicServer = (MusicSearchInterfaceAsync) GWT.create(MusicSearchInterface.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) musicServer;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "musicsearch";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
    }
}
