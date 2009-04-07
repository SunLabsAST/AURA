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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedSteeringMenu extends ContextMenu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedSteeringMenu(ClientDataManager tCdm) {

        super();

        this.cdm = tCdm;

        addElement("Start new steerable with artist's top tags", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.setSteerableReset(true);
                History.newItem("steering:" + currArtist.getId());
            }
        });
        addElement("Start new steerable with artist", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.setSteerableReset(true);
                History.newItem("steering:art:" + currArtist.getId());
                }
            });
        addElement("Add artist's top tags", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.getSteerableTagCloudExternalController().addTags(currArtist.getDistinctiveTags());
            }
        });
        addElement("Add artist", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.getSteerableTagCloudExternalController().addArtist(currArtist);
            }
        });       
    }
    
    @Override
    public void showAt(Event e, ArtistCompact currArtist) {
        this.currArtist = currArtist;
        super.showAt(e);
    }

}
