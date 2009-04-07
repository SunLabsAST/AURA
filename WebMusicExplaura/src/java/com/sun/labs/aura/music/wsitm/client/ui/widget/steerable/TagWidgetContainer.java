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

package com.sun.labs.aura.music.wsitm.client.ui.widget.steerable;

import com.google.gwt.user.client.ui.Grid;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.SharedSteeringCIMenu;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.SteeringSwidget.MainPanel;
import java.util.HashMap;

/**
 * Sits on top of the real tag cloud manipulation UIs and allows switching
 * between them. Implements TagWidget but passes most of the methods down to
 * the currently loaded UI
 * @author mailletf
 */
public class TagWidgetContainer extends TagWidget {

    private Grid g;
    private TagWidget activeTagWidget;
    private ClientDataManager cdm;

    private SharedSteeringCIMenu sharedCloudArtistMenu;
    private SharedSteeringCIMenu sharedCloudTagMenu;

    private boolean isInit = false;

    public TagWidgetContainer(MainPanel mainPanel, ClientDataManager cdm) {
        super(mainPanel);

        this.cdm = cdm;

        sharedCloudArtistMenu = new SharedSteeringCIMenu(cdm, this, CloudItem.CloudItemType.ARTIST);
        sharedCloudTagMenu = new SharedSteeringCIMenu(cdm, this, CloudItem.CloudItemType.TAG);

    }

    public void init(TagWidget tW) {

        if (isInit) {
            Popup.showErrorPopup("", Popup.ERROR_MSG_PREFIX.NONE,
                    "Can only init TagWidgetContainer once!", Popup.ERROR_LVL.NORMAL, null);
        }

        isInit = true;

        this.activeTagWidget = tW;

        g = new Grid(1, 1);
        g.setWidget(0, 0, activeTagWidget);
        initWidget(g);
    }

    public SharedSteeringCIMenu getSharedCloudArtistMenu() {
        return sharedCloudArtistMenu;
    }

    public SharedSteeringCIMenu getSharedCloudTagMenu() {
        return sharedCloudTagMenu;
    }

    public void swapTagWidget(TagWidget newTagWidget) {
        cdm.getTagCloudListenerManager().disableNotifications();
        newTagWidget.addItems(activeTagWidget.getItemsMap(), ITEM_WEIGHT_TYPE.RELATIVE);
        activeTagWidget = newTagWidget;
        g.setWidget(0, 0, activeTagWidget);
        cdm.getTagCloudListenerManager().enableNotifications();
    }

    @Override
    public HashMap<String, CloudItem> getItemsMap() {
        return activeTagWidget.getItemsMap();
    }

    @Override
    public void removeItem(String itemId) {
        activeTagWidget.removeItem(itemId);
    }

    @Override
    public void redrawTagCloud() {
        activeTagWidget.redrawTagCloud();
    }

    @Override
    public void removeAllItems(boolean updateRecommendations) {
        activeTagWidget.removeAllItems(updateRecommendations);
    }

    @Override
    public boolean containsItem(String itemId) {
        return activeTagWidget.containsItem(itemId);
    }

    @Override
    public void addItems(HashMap<String, CloudItem> items, ITEM_WEIGHT_TYPE weightType, int limit) {
        activeTagWidget.addItems(items, weightType, limit);
    }

    @Override
    public void addItem(CloudItem item, boolean updateRecommendations) {
        activeTagWidget.addItem(item, updateRecommendations);
    }

    @Override
    public double getMaxWeight() {
        return activeTagWidget.getMaxWeight();
    }
}
