/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterface;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterfaceAsync;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.TagDependentSharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedTagMenu extends Menu implements TagDependentSharedMenu {

    protected ItemInfo currTag;
    protected MusicSearchInterfaceAsync musicServer;

    public SharedTagMenu(ClientDataManager cdm) {

        initRPC();

        addItem(new Item("View tag details", new DataEmbededBaseItemListener<SharedTagMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                History.newItem("tag:" + data.currTag.getId());
            }
        }));
        addItem(new Item("Add tag to current steerable tag cloud",
                new DualDataEmbededBaseItemListener<ClientDataManager, SharedTagMenu>(cdm, this) {

                    @Override
                    public void onClick(BaseItem item, EventObject e) {
                        data.getSteerableTagCloudExternalController().addTag(sndData.currTag);
                    }
                }));
        addItem(new Item("View similar tags",
                new DataEmbededBaseItemListener<SharedTagMenu>(this) {

                    @Override
                    public void onClick(BaseItem item, EventObject e) {
                        invokeSimTagService(currTag.getId());
                    }
                }));
    }
    
    private void invokeSimTagService(String tagId) {

        AsyncCallback<ItemInfo[]> callback = new AsyncCallback<ItemInfo[]>() {

            public void onSuccess(ItemInfo[] iI) {
                if (iI != null || iI.length > 0) {
                    TagDisplayLib.showTagCloud("Similar tags to "+currTag.getItemName(), iI);
                } else {
                    Window.alert("An error occured while fetching similar tags.");
                }
            }

            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
        };

        try {
            musicServer.getSimilarTags(tagId, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    public void showAt(int x, int y, ItemInfo currTag) {
        this.currTag = currTag;
        int[] xyPosition = new int[]{x, y};
        super.showAt(xyPosition);
    }

    @Override
    public void show(String id) {
        Window.alert("This method cannot be called directly.");
    }

    @Override
    public void showAt(int x, int y) {
        Window.alert("This method cannot be called directly.");
    }

    @Override
    public void showAt(int[] xy) {
        Window.alert("This method cannot be called directly.");
    }

    @Override
    public void showAt(int x, int y, Menu parentMenu) {
        Window.alert("This method cannot be called directly.");
    }
    
    private final void initRPC() {
        musicServer = (MusicSearchInterfaceAsync) GWT.create(MusicSearchInterface.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) musicServer;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "musicsearch";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
    }
}
