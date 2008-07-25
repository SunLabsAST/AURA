/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.SharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedTagMenu extends Menu implements SharedMenu {

    protected ItemInfo currTag;

    public SharedTagMenu(ClientDataManager cdm) {
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
                        Window.alert("Not yet implemented");
                    }
                }));
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
}
