/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.event.SourcesRightClickEvents;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.HasContextMenu;

/**
 *
 * @author mailletf
 */
public class ContextMenuImage extends Image implements SourcesRightClickEvents, HasContextMenu {
    
    private ClickListenerCollection rightClickListeners;
    protected ContextMenu cm;

    public ContextMenuImage() {
        super();
        cm = new ContextMenu();
    }

    public ContextMenuImage(String url) {
        super(url);
        cm = new ContextMenu();

        sinkEvents(Event.ONCONTEXTMENU);
        rightClickListeners = new ClickListenerCollection();
    }
    
    public ContextMenuImage(String url, Menu sharedMenu) {
        super(url);
        cm = new ContextMenu(sharedMenu);
        
        sinkEvents(Event.ONCONTEXTMENU);
        rightClickListeners = new ClickListenerCollection();
    }
    
    public ContextMenuImage(Image img, Menu sharedMenu, ClickListener cL, MouseListener mL) {
        // is this creating leaks??
        ImageElement.as(img.getElement());
        setElement(img.getElement());
        if (mL != null) {
            this.addMouseListener(mL);
        }
        if (cL != null) {
            this.addClickListener(cL);
        }

        cm = new ContextMenu(sharedMenu);
        
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
