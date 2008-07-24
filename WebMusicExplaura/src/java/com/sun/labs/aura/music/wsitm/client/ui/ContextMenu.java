/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;

/**
 *
 * @author mailletf
 */
public class ContextMenu implements EventPreview {

    private Menu menu;
    private boolean isVisible = false;

    public ContextMenu() {

        menu = new Menu();
        Item editItem = new Item("Edit", new BaseItemListenerAdapter() {

            public void onClick(BaseItem item, EventObject e) {
                Window.alert("edit");
            }
        });
        editItem.setId("edit-item");
        menu.addItem(editItem);

        Item disableItem = new Item("Disable", new BaseItemListenerAdapter() {

            public void onClick(BaseItem item, EventObject e) {
                Window.alert("disable");
            }
        });
        menu.addItem(disableItem);

    }

    public void hideMenu() {
        DOM.removeEventPreview(this);
        menu.hide();
        isVisible = false;
    }

    public void showMenu(Event e) {
        DOM.addEventPreview(this);
        menu.showAt(e.getClientX(), e.getClientY());
        isVisible = true;
    }

    public boolean onEventPreview(Event event) {
        Element target = DOM.eventGetTarget(event);

        if (DOM.getCaptureElement() != null) {
            return true;
        }

        boolean eventTargetsPopup = (target != null) && DOM.isOrHasChild(menu.getElement(), target);

        if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
            if (!eventTargetsPopup) {
                hideMenu();
            }
        }

        return true;
    }
}
