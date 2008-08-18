/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.sun.labs.aura.music.wsitm.client.WebException;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededBaseItemListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
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
    
    public ContextMenu(Menu menu) {
        this.menu = menu;
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
            @Override
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
    
    public void showSharedMenu(Event e, ItemInfo tag) throws WebException {
        if (menu instanceof TagDependentSharedMenu) {
            DOM.addEventPreview(this);
            ((TagDependentSharedMenu) menu).showAt(e.getClientX(), e.getClientY(), tag);
            isVisible = true;
        } else {
            throw new WebException(WebException.errorMessages.INVALID_MENU_CALLED);
        }
    }
    
    public void showSharedMenu(Event e, ArtistCompact aC) throws WebException {
        if (menu instanceof ArtistDependentSharedMenu) {
            DOM.addEventPreview(this);
            ((ArtistDependentSharedMenu) menu).showAt(e.getClientX(), e.getClientY(), aC);
            isVisible = true;
        } else {
            throw new WebException(WebException.errorMessages.INVALID_MENU_CALLED);
        }
    }

    public void showSharedMenu(Event e, CloudItem cI) throws WebException {
        if (menu instanceof CloudItemDependentSharedMenu) {
            DOM.addEventPreview(this);
            ((CloudItemDependentSharedMenu) menu).showAt(e.getClientX(), e.getClientY(), cI);
            isVisible = true;
        } else {
            throw new WebException(WebException.errorMessages.INVALID_MENU_CALLED);
        }
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
    
    public interface TagDependentSharedMenu {
        
        public void showAt(int x, int y, ItemInfo currTag);
    }
    
    public interface ArtistDependentSharedMenu {
        
        public void showAt(int x, int y, ArtistCompact currTag);
    }

    public interface CloudItemDependentSharedMenu {

        public void showAt(int x, int y, CloudItem cI);
    }
}
