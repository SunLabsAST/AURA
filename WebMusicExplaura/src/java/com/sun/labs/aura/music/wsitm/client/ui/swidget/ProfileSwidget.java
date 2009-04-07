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

import com.sun.labs.aura.music.wsitm.client.DECommand;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class ProfileSwidget extends Swidget implements LoginListener {

    private FlowPanel mainPanel;
    private Image loadImage;

    public ProfileSwidget(ClientDataManager cdm) {
        super("User preferences", cdm);
        mainPanel = new FlowPanel();
        
        initWidget(getMainWidget());
    }

    public Widget getMainWidget() {
        mainPanel.clear();
        updateMainPanel();
        return mainPanel;
    }

    @Override
    public ArrayList<String> getTokenHeaders() {

        ArrayList<String> l = new ArrayList<String>();
        l.add("userpref:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        // no menu
        menuItem = new MenuItem();
    }

    private void updateMainPanel() {
        if (cdm.isLoggedIn()) {
            mainPanel.add(getProfileWidget());
        } else {
            mainPanel.add(getMustBeLoggedInWidget());
        }
    }

    @Override
    public void doRemoveListeners() {
        onDelete();
    }

    @Override
    public void onLogin(ListenerDetails lD) {
        update("");
    }

    @Override
    public void onLogout() {
        update("");
    }

    @Override
    public void onDelete() {
        cdm.getLoginListenerManager().removeListener(this);
    }

    @Override
    public void update(String historyToken) {
        //
        // We need to update in case the user has logged in or logged off since his last
        // visit to this page
        mainPanel.clear();
        updateMainPanel();
    }

    public Widget getProfileWidget() {

        Map<String, TextBox> newSettings = new HashMap<String, TextBox>();

        VerticalPanel main = new VerticalPanel();

        Grid g = new Grid(2,2);

        TextBox lastfmUserBox = new TextBox();
        newSettings.put("lastfmUser", lastfmUserBox);
        if (cdm.getListenerDetails().getLastFmUser()!=null) {
            lastfmUserBox.setText(cdm.getListenerDetails().getLastFmUser());
        }

        TextBox pandoraUserBox = new TextBox();
        newSettings.put("pandoraUser", pandoraUserBox);
        if (cdm.getListenerDetails().getPandoraUser()!=null) {
            pandoraUserBox.setText(cdm.getListenerDetails().getPandoraUser());
        }

        main.add(new HTML("<h2>APML providers</h2>"));
        Label txt = new Label("Last.fm username :");
        //txt.setStyleName("whiteTxt");
        g.setWidget(0, 0, txt);
        g.setWidget(0, 1, lastfmUserBox);
        txt = new Label("Pandora username : ");
        //txt.setStyleName("whiteTxt");
        g.setWidget(1, 0, txt);
        g.setWidget(1, 1, pandoraUserBox);

        main.add(g);

        HorizontalPanel hP = new HorizontalPanel();

        Button updateButton = new Button("Update your profile");
        updateButton.addStyleName("main");
        updateButton.addClickListener(new UserPrefSubmitClickListener(newSettings));
        hP.add(updateButton);

        loadImage = new Image("ajax-loader-small.gif");
        loadImage.setVisible(false);
        hP.add(loadImage);

        main.add(hP);

        return main;

    }

    private void invokeUpdateListener(final ListenerDetails lD) {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                Popup.showInformationPopup("Update sucessfull.");
                loadImage.setVisible(false);
            }

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "update your profile.", Popup.ERROR_LVL.NORMAL,
                        new DECommand<ListenerDetails>(lD) {
                    @Override
                    public void execute() {
                        invokeUpdateListener(data);
                    }
                });
                loadImage.setVisible(false);
            }
        };

        try {
            musicServer.updateListener(lD, callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "update your profile.", Popup.ERROR_LVL.NORMAL,
                    new DECommand<ListenerDetails>(lD) {
                        @Override
                        public void execute() {
                            invokeUpdateListener(data);
                        }
                    });
        }
    }

    class UserPrefSubmitClickListener implements ClickListener {

        private Map<String, TextBox> newSettings;

        public UserPrefSubmitClickListener(Map<String, TextBox> newSettings) {
            this.newSettings = newSettings;
        }

        public void onClick(Widget asdrg0) {

            loadImage.setVisible(true);

            ListenerDetails lD = cdm.getListenerDetails();

            lD.setLastFmUser(newSettings.get("lastfmUser").getText());
            lD.setPandoraUser(newSettings.get("pandoraUser").getText());

            invokeUpdateListener(lD);
        }
    }
}
