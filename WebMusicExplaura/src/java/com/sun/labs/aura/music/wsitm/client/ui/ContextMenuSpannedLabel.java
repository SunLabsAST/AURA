/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.event.SourcesRightClickEvents;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.HasContextMenu;

/**
 *
 * @author mailletf
 */
public class ContextMenuSpannedLabel extends SpannedLabel implements SourcesRightClickEvents, HasContextMenu {

    private ClickListenerCollection rightClickListeners;
    protected ContextMenu cm;

    /**
     * Construct with a specific context menu
     * @param txt
     * @param sharedMenu
     */
    public ContextMenuSpannedLabel(String txt, Menu sharedMenu) {
        super(txt);
        this.cm = new ContextMenu(sharedMenu);
        init();
    }
    
    public ContextMenuSpannedLabel(String txt) {
        super(txt);
        cm = new ContextMenu();
        init();
    }
    
    private void init() {
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
