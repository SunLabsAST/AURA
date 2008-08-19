/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.event.BaseItemListener;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.CloudItemDependentSharedMenu;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidgetContainer;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public class SharedSteeringArtistMenu extends Menu implements CloudItemDependentSharedMenu {

    protected CloudItem cI;
    protected ClientDataManager cdm;
    protected TagWidgetContainer twc;

    public SharedSteeringArtistMenu(ClientDataManager tCdm, TagWidgetContainer tTwc) {
        
        this.cdm = tCdm;
        this.twc = tTwc;

        addItem(new Item("Expand artist", new DataEmbededBaseItemListener<SharedSteeringArtistMenu>(this) {

            @Override
            public void onClick(BaseItem bItem, EventObject e) {

                HashMap<String, CloudItem> itemsMap = new HashMap<String, CloudItem>();

                double maxVal = 0;
                for (CloudItem item : cI.getContainedItems()) {
                    if (item.getWeight() > maxVal) {
                        maxVal = item.getWeight();
                    }
                }
                    
                for (CloudItem item : cI.getContainedItems()) {
                    item.setWeight( item.getWeight() / maxVal * cI.getWeight() );
                    itemsMap.put(item.getId(), item);
                }

                twc.addItems(itemsMap, TagWidget.ITEM_WEIGHT_TYPE.ABSOLUTE);
                twc.removeItem(cI.getId());
            }
        }));
        
        addItem(new Item("View tag cloud", new DataEmbededBaseItemListener<SharedSteeringArtistMenu>(this) {

            @Override
            public void onClick(BaseItem bItem, EventObject e) {
                TagDisplayLib.showTagCloud("Tag cloud for "+cI.getDisplayName(), cI.getTagMap(), cdm);
            }

        }));
    }

    public void showAt(int x, int y, CloudItem cI) {
        this.cI = cI;
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
