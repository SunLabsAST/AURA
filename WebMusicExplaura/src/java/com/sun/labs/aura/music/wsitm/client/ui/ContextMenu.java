/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;

/**
 *
 * @author mailletf
 */
public class ContextMenu implements EventPreview {

    private Menu menu;
    private boolean isVisible = false;

    public ContextMenu() {

        menu = new Menu();
        /*
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
        */
    }

    /**
     * Add a new item to the context menu
     * @param name Name to display
     * @param cmd Command to execute on click
     * @return the new item, to add it to another menu if necessary
     */
    public Item addItem(String name, Command cmd) {
        Item newItem = new Item(name, new DataEmbededBaseItemListener<Command>(cmd) {
            public void onClick(BaseItem item, EventObject e) {
                    data.execute();
            }
        });
        menu.addItem(newItem);
        return newItem;
    }

    public void addItem(Item item) {
        menu.addItem(item);
    }

    public void addSeperator() {
        menu.addSeparator();
    }

    public boolean isVisible() {
        return isVisible;
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

        if (isVisible) {
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
        }
        return true;
    }
}
