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

package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.WebLib;
import com.sun.labs.aura.music.wsitm.client.items.ServerInfoItem;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public class ServerInfoSwidget extends Swidget {

    private Grid g;

    public ServerInfoSwidget(ClientDataManager cdm) {
        super("Server information", cdm);

        g = new Grid(2,1);

        Label update = new Label("Update");
        update.addStyleName("pointer");
        update.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent ce) {
                g.setWidget(1, 0, WebLib.getSunLoaderWidget());
                invokeGetServerInfo();
            }
        });
        g.setWidget(0, 0, update);

        initWidget(g);
    }

    private void invokeGetServerInfo() {

        AsyncCallback<ServerInfoItem> callback = new AsyncCallback<ServerInfoItem>() {

            public void onSuccess(ServerInfoItem info) {

                HorizontalPanel hP = new HorizontalPanel();
                hP.setSpacing(8);

                // Items info
                if (info.getItemCnt() != null) {
                    hP.add(WebLib.createSection("Items", HashMapToGrid(info.getItemCnt())));
                }

                // Attentions info
                if (info.getAttentionCnt() != null) {
                    hP.add(WebLib.createSection("Attentions", HashMapToGrid(info.getAttentionCnt())));
                }

                // Cache info
                if (info.getCacheStatus() != null) {
                    hP.add(WebLib.createSection("Cache info", HashMapToGrid(info.getCacheStatus())));
                }

                g.setWidget(1, 0, hP);

            }

            public void onFailure(Throwable caught) {
                g.setWidget(1, 0, new Label("Update failed."));
            }
        };

        try {
            musicServer.getServerInfo(callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve server info.", Popup.ERROR_LVL.NORMAL, null);
        }
    }

    private Grid HashMapToGrid(HashMap<String, Integer> map) {
        Grid cachePanel = new Grid(map.keySet().size(), 2);
        int index = 0;
        for (String s : map.keySet()) {
            cachePanel.setWidget(index, 0, new Label(s));
            cachePanel.setWidget(index, 1, new Label(map.get(s).toString()));
            index++;
        }
        return cachePanel;
    }

    @Override
    public ArrayList<String> getTokenHeaders() {
        ArrayList<String> l = new ArrayList<String>();
        l.add("serverinfo:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        // no menu
        menuItem = new MenuItem();
    }

    public void doRemoveListeners() {
        // no listeners to remove
    }

}
