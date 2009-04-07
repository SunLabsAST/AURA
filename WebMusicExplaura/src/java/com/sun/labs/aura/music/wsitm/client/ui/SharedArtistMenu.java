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

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedArtistMenu extends ContextMenu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedArtistMenu(ClientDataManager tCdm) {
        super();
        this.cdm = tCdm;

        addElement("View artist details", new ClickListener() {

           @Override
            public void onClick(Widget sender) {
                History.newItem("artist:" + currArtist.getId());
            }


        });
        addElement("View tag cloud", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                TagDisplayLib.showTagCloud("Tag cloud for "+currArtist.getName(),
                        currArtist.getDistinctiveTags(), TagDisplayLib.ORDER.SHUFFLE, cdm);
            }
        });

        addSeperator();
        
        addElement("Start new steerable from artist's top tags", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cdm.setSteerableReset(true);
                History.newItem("steering:" + currArtist.getId());
            }
        });
        addElement("Add artist to steerable", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cdm.getSteerableTagCloudExternalController().addArtist(currArtist);
            }
        });
        addElement("Add artist's top tags to steerable", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cdm.getSteerableTagCloudExternalController().addTags(currArtist.getDistinctiveTags());
            }
        });  
    }

    public void showAt(Event e, ArtistCompact aC) {
        this.currArtist = aC;
        super.showAt(e);
    }
    
}
