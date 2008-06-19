/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class ProfileWidget extends Swidget {

    public ProfileWidget(ClientDataManager cdm) {
        super("User preferences", cdm);

        initWidget(getWidget());
    }

    public List<String> getTokenHeaders() {

        List<String> l = new ArrayList<String>();
        l.add("userpref:");
        return l;
    }

    public Widget getWidget() {

        Map<String, TextBox> newSettings = new HashMap<String, TextBox>();

        HorizontalPanel main = new HorizontalPanel();

        Grid g = new Grid(2,2);

        TextBox lastfmUserBox = new TextBox();
        newSettings.put("lastfmUser", lastfmUserBox);
        if (cdm.getListenerDetails().lastfmUser!=null) {
            lastfmUserBox.setText(cdm.getListenerDetails().lastfmUser);
        }

        TextBox pandoraUserBox = new TextBox();
        newSettings.put("pandoraUser", pandoraUserBox);
        if (cdm.getListenerDetails().pandoraUser!=null) {
            pandoraUserBox.setText(cdm.getListenerDetails().pandoraUser);
        }

        Label txt = new Label("Last.fm username :");
        txt.setStyleName("whiteTxt");
        g.setWidget(0, 0, txt);
        g.setWidget(0, 1, lastfmUserBox);
        txt = new Label("Pandora username : ");
        txt.setStyleName("whiteTxt");
        g.setWidget(1, 0, txt);
        g.setWidget(1, 1, pandoraUserBox);

        main.add(g);

        Button updateButton = new Button("Update");
        updateButton.addClickListener(new UserPrefSubmitClickListener(newSettings));
        main.add(updateButton);

        return main;

    }

    private void invokeUpdateListener(ListenerDetails lD) {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                Window.alert("Update OK");
            }

            public void onFailure(Throwable caught) {
                Window.alert("Update failed!");
            }
        };

        try {
            musicServer.updateListener(lD, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    class UserPrefSubmitClickListener implements ClickListener {

        private Map<String, TextBox> newSettings;

        public UserPrefSubmitClickListener(Map<String, TextBox> newSettings) {
            this.newSettings = newSettings;
        }

        public void onClick(Widget asdrg0) {
            ListenerDetails lD = cdm.getListenerDetails();

            lD.lastfmUser = newSettings.get("lastfmUser").getText();
            lD.pandoraUser = newSettings.get("pandoraUser").getText();
            Window.alert("pandora client side is :"+lD.pandoraUser);

            invokeUpdateListener(lD);
        }

    }

}
