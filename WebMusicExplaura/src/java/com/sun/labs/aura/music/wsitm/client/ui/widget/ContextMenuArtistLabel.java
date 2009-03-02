/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuSpannedLabel;

/**
 *
 * @author mailletf
 */
public class ContextMenuArtistLabel extends ContextMenuSpannedLabel {

    protected ArtistCompact aC;

    public ContextMenuArtistLabel(ArtistCompact taC, ClientDataManager cdm) {
        super(taC.getName(), cdm.getSharedArtistMenu());
        this.aC = taC;

        this.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                History.newItem("artist:" + aC.getId());
            }
        });
        this.addStyleName("pointer");
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            DOM.eventPreventDefault(event);
            ((ArtistDependentSharedMenu) cm).showAt(event, aC);
        } else {
            super.onBrowserEvent(event);
        }
    }
}