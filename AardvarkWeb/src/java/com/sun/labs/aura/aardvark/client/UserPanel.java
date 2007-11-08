/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author plamere
 */
public class UserPanel extends DockPanel {
    private TextBox name;
    private Label msg;

    public UserPanel() {
        Label prompt = new Label("User Name:");
        name = new TextBox();
        name.setMaxLength(40);
        Button register = new Button("Register");

        register.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                registerUser(name.getText());
            }
        });

        Button login = new Button("Login");
        login.addClickListener(new ClickListener(){
            public void onClick(Widget w) {
                loginUser(name.getText());
            }
        });

        Panel topPanel = new HorizontalPanel();
        topPanel.add(prompt);
        topPanel.add(name);
        topPanel.add(register);
        topPanel.add(login);
        
        msg = new Label("");

        add(topPanel, NORTH);

        add(msg, SOUTH);

    }

    private void message(String m) {
        msg.setText(m);
    }
    
    private void registerUser(String userName) {
        message("Registering " + userName);
        
        if (isValidNewUser(userName)) {
            add(getFeedPanel(userName), CENTER);
            message("");
        } else {
            message("Invalid user " + userName);
        }
    }

    private void loginUser(String userName) {
        message("Logging in " + userName);
        if (isValidUser(userName)) {
            add(getFeedPanel(userName), CENTER);
            message("");
        } else {
            message("Unknown user " + userName);
        }
    }
    
    private boolean isValidNewUser(String name) {
        return true;
    }
    
    private boolean isValidUser(String name) {
        return true;
    }

    private Panel getFeedPanel(String userName) {
        Panel feedPanel = new VerticalPanel();
        
        Panel inPanel = new HorizontalPanel();
        TextBox starredItemFeed = new TextBox();
        starredItemFeed.setMaxLength(100);
        starredItemFeed.setVisibleLength(100);
        inPanel.add(new Label("Enter Starred Item Feed URL:"));
        inPanel.add(starredItemFeed);

        Button setFeed = new Button("Set");
        inPanel.add(setFeed);

        Panel recPanel = new HorizontalPanel();
        Image recommendedFeed = new Image("feedicon.png");
        recPanel.add(new Label("Recommended Feed URL:"));
        recPanel.add(recommendedFeed);

        feedPanel.add(inPanel);
        feedPanel.add(recPanel);
        
        return feedPanel;
    }
}
