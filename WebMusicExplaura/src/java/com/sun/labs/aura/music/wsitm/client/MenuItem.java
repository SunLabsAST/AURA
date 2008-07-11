/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import java.util.Comparator;

/**
 *
 * @author mailletf
 */
public class MenuItem {

    private String name;
    private ClickListener cL;
    private boolean mustBeLoggedIn;
    private int order;

    public MenuItem(String name, ClickListener cL, boolean mustBeLoggedIn, int order) {
        this.name = name;
        this.cL = cL;
        this.mustBeLoggedIn = mustBeLoggedIn;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public ClickListener getClickListener() {
        return cL;
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

    /**
     * Returns a click listener that will add the supplied token to the history
     * @param token
     * @return
     */
    public static ClickListener getDefaultTokenClickListener(String token) {
        return new DataEmbededClickListener<String>(token) {

                public void onClick(Widget arg0) {
                    History.newItem(data);
                }
            };
    }

    private static class MenuItemComparator implements Comparator<MenuItem> {

        public int compare(MenuItem o1, MenuItem o2) {
            return new Integer(o1.getOrder()).compareTo(o2.getOrder());
        }

    }
}
