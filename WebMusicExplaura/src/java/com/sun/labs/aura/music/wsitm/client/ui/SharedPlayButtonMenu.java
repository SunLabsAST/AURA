/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PlayButton.MusicProviders;

/**
 *
 * @author mailletf
 */
public class SharedPlayButtonMenu extends Menu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedPlayButtonMenu(ClientDataManager tCdm) {

        this.cdm = tCdm;

        addItem(new Item("Play with Spotify", new DataEmbededBaseItemListener<SharedPlayButtonMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                cdm.setCurrPreferedMusicProvider(MusicProviders.SPOTIFY);
            }
        }));
        addItem(new Item("Play with the EcoNest",
                new DataEmbededBaseItemListener<SharedPlayButtonMenu>(this) {

                    @Override
                    public void onClick(BaseItem item, EventObject e) {
                        cdm.setCurrPreferedMusicProvider(MusicProviders.ECHONEST);
                    }
                }));
        addItem(new Item("Play with Last.fm tag radio", new DataEmbededBaseItemListener<SharedPlayButtonMenu>(this) {

            @Override
            public void onClick(BaseItem item, EventObject e) {
                cdm.setCurrPreferedMusicProvider(MusicProviders.LASTFM);
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