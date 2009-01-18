/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PlayButton.MusicProviders;

/**
 *
 * @author mailletf
 */
public class SharedPlayButtonMenu extends ContextMenu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedPlayButtonMenu(ClientDataManager tCdm) {

        super();

        this.cdm = tCdm;
        
        addElement("Play with Last.fm", new DEClickHandler<SharedPlayButtonMenu>(this) {
            @Override
            public void onClick(ClickEvent event) {
                cdm.setCurrPreferedMusicProvider(MusicProviders.LASTFM);
            }
        });

        addElement("Play with Spotify", new DEClickHandler<SharedPlayButtonMenu>(this) {
            @Override
            public void onClick(ClickEvent event) {
                cdm.setCurrPreferedMusicProvider(MusicProviders.SPOTIFY);
            }
        });

        addElement("Play from the Web", new DEClickHandler<SharedPlayButtonMenu>(this) {
            @Override
            public void onClick(ClickEvent event) {
                cdm.setCurrPreferedMusicProvider(MusicProviders.THEWEB);
            }
        });
    }
    
    @Override
    public void showAt(Event e, ArtistCompact currArtist) {
        this.currArtist = currArtist;
        super.showAt(e);
    }

}