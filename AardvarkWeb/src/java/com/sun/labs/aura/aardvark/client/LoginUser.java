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
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author plamere
 */
public class LoginUser extends DockPanel {

    private LabeledTextbox userName;
    private AppStateListener appStateListener;

    public LoginUser(AppStateListener listener) {
        appStateListener = listener;
        Label l = new Label("Login to Aardvark");
        l.setStyleName("title");
        add(l, NORTH);

        Panel p = new HorizontalPanel();
        userName = new LabeledTextbox("Your Open ID", "", 40);

        Button ok = new Button("Login");
        ok.setStyleName("standardButton");
        ClickListener clickListener = new ClickListener() {

                    public void onClick(Widget arg0) {
                    }
                };

        ok.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                login();
            }
        });

        userName.addChangeListener(new ChangeListener() {
            public void onChange(Widget arg0) {
                login();
            }
        });


        p.add(userName);
        p.add(ok);
        add(p, CENTER);

        setStyleName("newUserPanel");
    }

    private void login() {
        String name = userName.getText();
        if (name.length() == 0) {
            appStateListener.error("Missing Open ID");
            return;
        }
        appStateListener.info("Logging in " + name);

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

        AardvarkServiceFactory.getService().loginUser(name, callback);
    }
}
