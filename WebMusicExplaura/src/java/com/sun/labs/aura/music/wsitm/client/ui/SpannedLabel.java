/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;


import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu;
import com.sun.labs.aura.music.wsitm.client.*;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.SourcesMouseEvents;

/**
 *
 * @author mailletf
 */
public class SpannedLabel extends Label implements SourcesClickEvents, SourcesMouseEvents {

    private ClickListenerCollection rightClickListeners;
    private ContextMenu cm;

    public SpannedLabel(String txt) {
        setElement(DOM.createSpan());
        setStyleName("gwt-Label");
        setText(txt);
        cm = new ContextMenu();
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCONTEXTMENU) {
            DOM.eventPreventDefault(event);
            cm.showMenu(event);
        } else {
            super.onBrowserEvent(event);
        }
    }

    public void addRightClickListener(ClickListener listener) {
        if (rightClickListeners == null) {
            rightClickListeners = new ClickListenerCollection();
            sinkEvents(Event.ONCONTEXTMENU);
        }
        rightClickListeners.add(listener);
    }

    public void removeRightClickListener(ClickListener listener) {
        if (rightClickListeners != null) {
            rightClickListeners.remove(listener);
        }
    }
}
