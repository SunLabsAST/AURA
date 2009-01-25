/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterface;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterfaceAsync;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.TagDependentSharedMenu;

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
                
        addSeperator();
        addElement("Add tag to current steerable tag cloud", new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                cdm.getSteerableTagCloudExternalController().addTag(currTag);
            }
        });
    }
    
    private void invokeSimTagService(String tagId) {

        AsyncCallback<ItemInfo[]> callback = new AsyncCallback<ItemInfo[]>() {

            public void onSuccess(ItemInfo[] iI) {
                if (iI != null || iI.length > 0) {
                    TagDisplayLib.showTagCloud("Similar tags to "+currTag.getItemName(), iI, TagDisplayLib.ORDER.SHUFFLE, cdm);
                } else {
                    Window.alert("An error occured while fetching similar tags.");
                }
            }

            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
        };

        try {
            Popup.showLoadingPopup();
            musicServer.getSimilarTags(tagId, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
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
