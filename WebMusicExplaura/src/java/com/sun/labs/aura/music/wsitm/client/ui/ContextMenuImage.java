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

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Image;
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
    
    public ContextMenuImage(String url, ContextMenu sharedMenu) {
        super(url);
        cm = sharedMenu;
        
        sinkEvents(Event.ONCONTEXTMENU);
        rightClickListeners = new ClickListenerCollection();
    }
    
    public ContextMenuImage(Image img, ContextMenu sharedMenu, ClickHandler cH, MouseOutHandler mOutH, MouseOverHandler mOverH) {
        ImageElement.as(img.getElement());
        setElement(img.getElement());
        if (mOutH != null) {
            this.addMouseOutHandler(mOutH);
        }
        if (mOverH!=null) {
            this.addMouseOverHandler(mOverH);
        }
        if (cH != null) {
            this.addClickHandler(cH);
        }

        cm = sharedMenu;
        
        sinkEvents(Event.ONCONTEXTMENU);
        rightClickListeners = new ClickListenerCollection();
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            DOM.eventPreventDefault(event);
            cm.showAt(event);
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

    @Override
    public ContextMenu getContextMenu() {
        return cm;
    }
}
