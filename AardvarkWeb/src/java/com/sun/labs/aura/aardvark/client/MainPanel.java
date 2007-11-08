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
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author plamere
 */
public class MainPanel extends DockPanel implements AppStateListener {

    private WiUser user = null;
    private Panel welcomeMenu;
    private Label logo = new Label("Aardvark!");
    private Label join = new Label("Join");
    private Label about = new Label("About");
    private Label login = new Label("Login");
    private Label logout = new Label("Logout");
    private Label recs = new Label("");
    private PopupPanel infoPanel = new PopupPanel(false, true);
    private PopupPanel errorPanel = new PopupPanel(true, true);
    private Widget mainContent;
    private Widget joinContent;
    private Widget loginContent;
    private Widget content = null;
    private Widget userContent = null;

    public MainPanel() {
        // click on the logo, show the intro content
        logo.setStyleName("menuText");
        logo.addClickListener(new ClickListener() {

                    public void onClick(Widget arg0) {
                        setContent(mainContent);
                    }
                });

        // click on the join option, show the join dialog
        join.setStyleName("menuText");
        join.addClickListener(new ClickListener() {

                    public void onClick(Widget arg0) {
                        setContent(joinContent);
                    }
                });

        about.setStyleName("menuText");
        about.addClickListener(new ClickListener() {

                    public void onClick(Widget arg0) {
                        invokeAddAboutPanel();
                    }
                });

        login.setStyleName("menuText");
        login.addClickListener(new ClickListener() {

                    public void onClick(Widget arg0) {
                        setContent(loginContent);
                    }
                });


        logout.setStyleName("menuText");
        logout.addClickListener(new ClickListener() {
                    public void onClick(Widget arg0) {
                        setCurrentUser(null);
                    }
                });

        recs.setStyleName("menuText");
        recs.addClickListener(new ClickListener() {
                    public void onClick(Widget arg0) {
                        invokeAddRecommendationPanel();
                    }
                });

        mainContent = new HTML("<h2> Welcome to Aardvark</h2> This is where we describe " +
                "in great detail, what Aardark is, and what it does. " +
                "We would also try to make it look nice.");
        mainContent.setStyleName("standardMainPanel");

        joinContent = new RegisterNewUserPanel(this);
        loginContent = new LoginUser(this);

        infoPanel.setStyleName("infoPanel");
        errorPanel.setStyleName("errorPanel");
        update();
    }

    private void update() {
        if (welcomeMenu != null) {
            remove(welcomeMenu);
        }

        if (user == null) {
            welcomeMenu = new HorizontalPanel();
            welcomeMenu.add(logo);
            welcomeMenu.add(join);
            welcomeMenu.add(about);
            welcomeMenu.add(login);
        } else {
            recs.setText("Recommendations for " + user.getName());

            welcomeMenu = new HorizontalPanel();
            welcomeMenu.add(logo);
            welcomeMenu.add(recs);
            welcomeMenu.add(about);
            welcomeMenu.add(logout);
        }
        welcomeMenu.setStyleName("welcomeMenu");
        setContent(mainContent);
        add(welcomeMenu, NORTH);
        setCellHeight(welcomeMenu, "20px");
        setStyleName("mainPanel");
    }

    public void info(String msg) {
        infoPanel.clear();
        infoPanel.add(new Label(msg));
        infoPanel.center();
    }

    public void clearInfo() {
        infoPanel.hide();
    }

    public void error(String msg) {
        errorPanel.clear();
        errorPanel.add(new Label(msg));
        errorPanel.center();
    }

    private void setContent(Widget w) {
        if (content != null) {
            remove(content);
            content = null;
        }
        content = w;
        add(w, CENTER);
        setCellVerticalAlignment(w, ALIGN_TOP);
    }

    public void setCurrentUser(WiUser newUser) {
        user = newUser;
        update();
    }

    private void invokeAddAboutPanel() {
        AsyncCallback callback = new AsyncCallback() {

                    public void onSuccess(Object result) {
                        WiStats stats = (WiStats) result;
                        clearInfo();
                        Panel statsPanel = new StatsPanel(stats);
                        Panel about = new AboutPanel(statsPanel);
                        setContent(about);
                    }

                    public void onFailure(Throwable caught) {
                        error("Problem getting stats " + caught.getMessage());
                    }
                };

        info("Getting stats");
        AardvarkServiceFactory.getService().getStats(callback);
    }

    private void invokeAddRecommendationPanel() {
        //
        AsyncCallback callback = new AsyncCallback() {

                    public void onSuccess(Object result) {
                        WiEntrySummary[] entries = (WiEntrySummary[]) result;
                        clearInfo();
                        userContent = new RecommendationPanel(entries);
                        setContent(userContent);
                    }

                    public void onFailure(Throwable caught) {
                        error("Problem getting recommendations:" + caught.getMessage());
                    }
                };

        info("Getting recommendations");
        AardvarkServiceFactory.getService().getRecommendations(user.getName(), callback);
    }
}

class AboutPanel extends VerticalPanel {

    public AboutPanel(Panel p) {
        HTML aboutContent = new HTML("<h2> About Aardvark</h2> Let me tell you about Aardvark.  " +
                "Aardvark is a blog recommender that works with popular blog readers such as the" +
                " Google Reader.  Aardvark will scour the blogosphere to find the blog posts that " +
                "you will want to read, based upon your reading habits. " +
                "<h2> Using Aardvark</h2>" +
                "To use Aardvark follow these simple steps:" +
                "  <ol> " +
                "<li> Join Aardvark" +
                "<li> Set your Starred Item Feed" +
                "<li> Add the Aardvark Recommends feed to your set of feeds in Google Reader" +
                "<li> Start enjoying personalized blogs." +
                "</ol>");
        add(aboutContent);
        add(p);
        setStyleName("aboutPanel");
    }
}

class StatsPanel extends DockPanel {

    StatsPanel(WiStats stats) {
        add(new Label("Aardvark Stats"), NORTH);
        Grid grid = new Grid(6, 2);
        grid.setText(0, 0, "Version");
        grid.setText(0, 1, stats.getVersion());

        grid.setText(1, 0, "Users");
        grid.setText(1, 1, Long.toString(stats.getNumUsers()));

        grid.setText(2, 0, "Items");
        grid.setText(2, 1, Long.toString(stats.getNumItems()));

        grid.setText(3, 0, "Taste");
        grid.setText(3, 1, Long.toString(stats.getNumAttention()));

        grid.setText(4, 0, "Feed Pulls");
        grid.setText(4, 1, Integer.toString(stats.getFeedPulls()));

        grid.setText(5, 0, "Feed Errors");
        grid.setText(5, 1, Integer.toString(stats.getFeedErrors()));
        add(grid, CENTER);
        setStyleName("statsPanel");
    }
}

class RecommendationPanel extends VerticalPanel {

    RecommendationPanel(WiEntrySummary[] entries) {
        add(new HTML("<h1> Your Recommendations</h1>"));
        for (int i = 0; i < entries.length; i++) {
            HTML h = new HTML("<a target='recwindow' href='" + entries[i].getLink() + "'>" +
                    entries[i].getTitle() + "</a>");
            add(h);
        }
        add(new HTML("<h1> Your Recommendation Feeds</h1>"));
        HTML feed = new HTML("<a href='nowhere.rss'> <img src='feedicon.png'/></a>");
        add(feed);
        setStyleName("standardMainPanel");
    }
}