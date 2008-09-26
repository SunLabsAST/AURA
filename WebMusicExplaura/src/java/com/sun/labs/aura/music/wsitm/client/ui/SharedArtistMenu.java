/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedArtistMenu extends Menu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedArtistMenu(ClientDataManager cdm) {

        this.cdm = cdm;

        addItem(new Item("View artist details",
                new DataEmbededBaseItemListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                History.newItem("artist:" + data.currArtist.getId());
            }
        }));
        addItem(new Item("View tag cloud",
                new DataEmbededBaseItemListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                TagDisplayLib.showTagCloud("Tag cloud for "+data.currArtist.getName(),
                        data.currArtist.getDistinctiveTags(), TagDisplayLib.ORDER.SHUFFLE, data.cdm);
            }
        }));

        addItem(new Item("Start new steerable from artist's top tags",
                new DataEmbededBaseItemListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                data.cdm.setSteerableReset(true);
                History.newItem("steering:" + data.currArtist.getId());
            }
        }));
        addItem(new Item("Add artist to steerable",
                new DataEmbededBaseItemListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                data.cdm.getSteerableTagCloudExternalController().addArtist(data.currArtist);
            }
        }));
        addItem(new Item("Add artist's top tags to steerable",
                new DataEmbededBaseItemListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                data.cdm.getSteerableTagCloudExternalController().addTags(data.currArtist.getDistinctiveTags());
            }
        }));

        
    }

    public void showAt(int x, int y, ArtistCompact aC) {
        this.currArtist = aC;
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
