/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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
