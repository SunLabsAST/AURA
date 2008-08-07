/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.WebException;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;

/**
 *
 * @author mailletf
 */
public class ContextMenuTagLabel extends ContextMenuSpannedLabel {

    protected ItemInfo tag;

    public ContextMenuTagLabel(ItemInfo tag, ClientDataManager cdm) {
        super(tag.getItemName(), cdm.getSharedTagMenu());
        this.tag = tag;
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            try {
                DOM.eventPreventDefault(event);
                cm.showSharedMenu(event, tag);
            } catch (WebException ex) {
                Window.alert(ex.toString());
            }
        } else {
            super.onBrowserEvent(event);
        }
    }
}
