/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.WebException;
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

        this.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                History.newItem("artist:" + aC.getId());
            }
        });
        this.addStyleName("pointer");
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            try {
                DOM.eventPreventDefault(event);
                ((ArtistDependentSharedMenu) cm).showAt(event, aC);
            } catch (WebException ex) {
                Window.alert(ex.toString());
            }
        } else {
            super.onBrowserEvent(event);
        }
    }
}