/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuImage;

/**
 *
 * @author mailletf
 */
public class ContextMenuSteeringWheelWidget extends ContextMenuImage {

    ArtistCompact aC;
    
    public ContextMenuSteeringWheelWidget(ClientDataManager cdm, SteeringWheelWidget sww, ArtistCompact aC) {
        super(sww, cdm.getSharedSteeringMenu(), sww.getClickHandler(), sww.getMouseOutHandler(), sww.getMouseOverHandler());
        this.aC = aC;
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            DOM.eventPreventDefault(event);
            ((ArtistDependentSharedMenu)cm).showAt(event, aC);
        } else {
            super.onBrowserEvent(event);
        }
    }
    
}
