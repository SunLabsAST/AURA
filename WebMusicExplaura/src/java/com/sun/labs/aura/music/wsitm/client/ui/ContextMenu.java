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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.DataEmbededCommand;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import java.util.LinkedList;

/**
 *
 * @author mailletf
 */
public class ContextMenu implements EventPreview {

    private Menu menu;
    private boolean isVisible = false;

    private LinkedList<Item> itemList;

    public ContextMenu() {
        menu = null;
        itemList = new LinkedList<Item>();
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
        //menu.addItem(newItem);
        itemList.add(newItem);
        return newItem;
    }

    public void addItem(Item item) {
        //menu.addItem(item);
        itemList.add(item);
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
        
        if (menu == null) {
            initMenu();
        }
        
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
    
    /**
     * Add the standard tag context menu items to the provided context menu capable object
     * @param cm
     * @param tag
     */
    public void addStandardTagContextMenu(ClientDataManager cdm, ItemInfo tag) {

        addItem("View tag details", new DataEmbededCommand<String, String>(tag.getId()) {

            public void execute() {
                History.newItem("tag:" + data);
            }
        });
        addItem("Add tag to current steerable tag cloud", new DataEmbededCommand<ClientDataManager,ItemInfo>(cdm, tag) {

            public void execute() {
                data.getSteerableTagCloudExternalController().addTag(sndData);
            }
        });
        addItem("View similar tags", new Command() {

            public void execute() {
                Window.alert("Not yet implemented");
            }
        });

    }
    
    private void initMenu() {
        if (menu == null) {
            menu = new Menu();

            for (Item i : itemList) {
                menu.addItem(i);
            }
        }
    }
    
    public interface HasContextMenu {

        public ContextMenu getContextMenu();
    }
}
