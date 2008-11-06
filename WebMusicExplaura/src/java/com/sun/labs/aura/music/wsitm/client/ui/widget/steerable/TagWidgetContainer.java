/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.widget.steerable;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.ui.SharedSteeringArtistMenu;
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

    private SharedSteeringArtistMenu sharedCloudArtistMenu;

    private boolean isInit = false;

    public TagWidgetContainer(MainPanel mainPanel, ClientDataManager cdm) {
        super(mainPanel);

        this.cdm = cdm;

        sharedCloudArtistMenu = new SharedSteeringArtistMenu(cdm, this);

    }

    public void init(TagWidget tW) {

        if (isInit) {
            Window.alert("Can only init TagWidgetContainer once!");
        }

        isInit = true;

        this.activeTagWidget = tW;

        g = new Grid(1, 1);
        g.setWidget(0, 0, activeTagWidget);
        initWidget(g);
    }

    public SharedSteeringArtistMenu getSharedCloudArtistMenu() {
        return sharedCloudArtistMenu;
    }

    public void swapTagWidget(TagWidget newTagWidget) {
        cdm.getTagCloudListenerManager().disableNotifications();
        newTagWidget.addItems(activeTagWidget.getItemsMap(), ITEM_WEIGHT_TYPE.RELATIVE);
        activeTagWidget = newTagWidget;
        g.setWidget(0, 0, activeTagWidget);
        cdm.getTagCloudListenerManager().enableNotifications();
    }

    public HashMap<String, CloudItem> getItemsMap() {
        return activeTagWidget.getItemsMap();
    }

    public void removeItem(String itemId) {
        activeTagWidget.removeItem(itemId);
    }

    public void removeAllItems(boolean updateRecommendations) {
        activeTagWidget.removeAllItems(updateRecommendations);
    }

    public boolean containsItem(String itemId) {
        return activeTagWidget.containsItem(itemId);
    }

    public void addItems(HashMap<String, CloudItem> items, ITEM_WEIGHT_TYPE weightType, int limit) {
        activeTagWidget.addItems(items, weightType, limit);
    }

    public void addItem(CloudItem item, boolean updateRecommendations) {
        activeTagWidget.addItem(item, updateRecommendations);
    }

    @Override
    public double getMaxWeight() {
        return activeTagWidget.getMaxWeight();
    }
}