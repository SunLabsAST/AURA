/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Label;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
import java.util.Comparator;

/**
 *
 * @author mailletf
 */
public class MenuItem {

    private String name;
    private ClickHandler cH;
    private boolean mustBeLoggedIn;
    private int order;
    private boolean displayInMenu;

    private Label lbl;  // label will be added when this item is displayed

    public MenuItem() {
        this.displayInMenu = false;
        this.cH = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {}
        };
        this.order = 5000;  // big value
    }

    public MenuItem(String name, ClickHandler cH, boolean mustBeLoggedIn, int order) {
        this.name = name;
        this.cH = cH;
        this.mustBeLoggedIn = mustBeLoggedIn;
        this.order = order;
        this.displayInMenu = true;
    }

    public String getName() {
        return name;
    }

    public ClickHandler getClickHandler() {
        return cH;
    }

    public boolean mustBeLoggedIn() {
        return mustBeLoggedIn;
    }

    public int getOrder() {
        return order;
    }
    
    public static Comparator<MenuItem> getOrderComparator() {
        return new MenuItemComparator();
    }

    public void setLabel(Label lbl) {
        this.lbl = lbl;
    }

    public void setSelected() {
        if (lbl != null) {
            lbl.getElement().setAttribute("style", "text-decoration: underline");
        }
    }

    public void setNotSelected() {
        if (lbl != null) {
            lbl.getElement().setAttribute("style", "text-decoration: none");
        }
    }

    /**
     * Returns a click handler that will add the supplied token to the history
     * @param token
     * @return
     */
    public static ClickHandler getDefaultTokenClickHandler(String token) {
        return new DEClickHandler<String>(token) {
                @Override
                public void onClick(ClickEvent event) {
                    History.newItem(data);
                }
            };
    }

    private static class MenuItemComparator implements Comparator<MenuItem> {

        @Override
        public int compare(MenuItem o1, MenuItem o2) {
            return new Integer(o1.getOrder()).compareTo(o2.getOrder());
        }
    }
}
