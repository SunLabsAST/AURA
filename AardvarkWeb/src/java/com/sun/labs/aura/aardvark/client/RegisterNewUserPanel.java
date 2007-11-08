/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author plamere
 */
public class RegisterNewUserPanel extends DockPanel {
    private TextBox userName;
    private TextBox starredItemFeed;
    private AppStateListener appStateListener;

    public RegisterNewUserPanel(AppStateListener listener) {
        this.appStateListener = listener;

        Label l = new Label("Join Aardvark");
        l.setStyleName("title");
        add(l, NORTH);

        userName = new TextBox();
        starredItemFeed = new TextBox();

        userName.setVisibleLength(40);
        starredItemFeed.setVisibleLength(40);

        Panel p = new VerticalPanel();
        Grid g = new Grid(2,2);
        g.setText(0, 0, "Your Open ID");
        g.setWidget(0, 1, userName);
        g.setText(1, 0, "Your Starred Item Feed");
        g.setWidget(1, 1, starredItemFeed);


        Button ok = new Button("Register");
        ok.setStyleName("standardButton");
        
        ok.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                String name = userName.getText();
                if (name.length() == 0) {
                    appStateListener.error("Missing Open ID");
                    return;
                }

                String feed = starredItemFeed.getText();
                if (feed.length() == 0) {
                    appStateListener.error("Missing Starred Item Feed");
                    return;
                }

                appStateListener.info("Registering " + name);

                AsyncCallback callback = new AsyncCallback() {

                    public void onFailure(Throwable arg0) {
                        appStateListener.clearInfo();
                        appStateListener.error("Whoops! Looks like the server is down.;");
                    }

                    public void onSuccess(Object result) {
                        appStateListener.clearInfo();
                        WiUserStatus wus = (WiUserStatus) result;
                        String status = wus.getStatus();
                        if (status != null) {
                            appStateListener.error(status);
                        } else {
                            appStateListener.setCurrentUser(wus.getUser());
                        }
                    }
                };
                AardvarkServiceFactory.getService().registerUser(name, feed, callback);
            }
        });

        p.add(g);
        p.add(ok);
        add(p, CENTER);
        setStyleName("newUserPanel");
    }
}
