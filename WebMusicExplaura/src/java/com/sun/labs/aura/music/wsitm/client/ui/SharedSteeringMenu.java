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
public class SharedSteeringMenu extends Menu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedSteeringMenu(ClientDataManager tCdm) {

        this.cdm = tCdm;

        addItem(new Item("Start new steerable with artist's top tags", new DataEmbededBaseItemListener<SharedSteeringMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                cdm.setSteerableReset(true);
                History.newItem("steering:" + data.currArtist.getId());
            }
        }));
        addItem(new Item("Start new steerable with artist",
                new DualDataEmbededBaseItemListener<ClientDataManager, SharedSteeringMenu>(cdm, this) {

                    @Override
                    public void onClick(BaseItem item, EventObject e) {
                        cdm.setSteerableReset(true);
                        History.newItem("steering:art:" + sndData.currArtist.getId());
                    }
                }));
        addItem(new Item("Add artist's top tags", new DualDataEmbededBaseItemListener<ClientDataManager, SharedSteeringMenu>(cdm, this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                data.getSteerableTagCloudExternalController().addTags(sndData.currArtist.getDistinctiveTags());
            }
        }));
        addItem(new Item("Add artist", new DualDataEmbededBaseItemListener<ClientDataManager, SharedSteeringMenu>(cdm, this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                data.getSteerableTagCloudExternalController().addArtist(sndData.currArtist);
            }
        }));       
    }
    
    public void showAt(int x, int y, ArtistCompact currArtist) {
        this.currArtist = currArtist;
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
