/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.WebLib;
import com.sun.labs.aura.music.wsitm.client.items.ServerInfoItem;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mailletf
 */
public class ServerInfoSwidget extends Swidget {

    private Grid g;

    public ServerInfoSwidget(ClientDataManager cdm) {
        super("Server information", cdm);

        g = new Grid(2,1);

        Label update = new Label("update");
        update.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                g.setWidget(1, 0, WebLib.getLoadingBarWidget());
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

                //
                // Cache info
                Grid cachePanel = new Grid(info.getCacheStatus().keySet().size(), 2);
                int index = 0;
                for (String s : info.getCacheStatus().keySet()) {
                    cachePanel.setWidget(index, 0, new Label(s));
                    cachePanel.setWidget(index, 1, new Label(info.getCacheStatus().get(s).toString()));
                    index++;
                }
                hP.add(WebLib.createSection("Cache info", cachePanel));

                g.setWidget(1, 0, hP);

            }

            public void onFailure(Throwable caught) {
                g.setWidget(1, 0, new Label("Update failed."));
            }
        };

        try {
            musicServer.getServerInfo(callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
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
