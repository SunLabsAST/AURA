/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu;
import com.sun.labs.aura.music.wsitm.client.*;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.sun.labs.aura.music.wsitm.client.event.SourcesRightClickEvents;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.HasContextMenu;

/**
 *
 * @author mailletf
 */
public class ContextMenuSpannedLabel extends SpannedLabel implements SourcesRightClickEvents, HasContextMenu {

    private ClickListenerCollection rightClickListeners;
    private ContextMenu cm;

    public ContextMenuSpannedLabel(String txt) {
        super(txt);
        cm = new ContextMenu();
        
        sinkEvents(Event.ONCONTEXTMENU);
        rightClickListeners = new ClickListenerCollection();
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            DOM.eventPreventDefault(event);
            cm.showMenu(event);
            rightClickListeners.fireClick(this);
        } else {
            super.onBrowserEvent(event);
        }
    }

    public void addRightClickListener(ClickListener listener) {
        rightClickListeners.add(listener);
    }

    public void removeRightClickListener(ClickListener listener) {
        if (rightClickListeners != null) {
            rightClickListeners.remove(listener);
        }
    }
    
    public ContextMenu getContextMenu() {
        return cm;
    }
    
}
