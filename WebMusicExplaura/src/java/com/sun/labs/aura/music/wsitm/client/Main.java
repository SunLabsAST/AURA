/*
 * Main.java
 *
 * Created on March 5, 2007, 12:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client;

import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.Swidget;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.SteeringSwidget;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.DashboardSwidget;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.SimpleSearchSwidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PageHeaderWidget;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.ProfileSwidget;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ui.PerformanceTimer;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.ServerInfoSwidget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class Main implements EntryPoint, HistoryListener {

    private ClientDataManager cdm;
    private Map<String, Swidget> tokenHeadersMap;
    private String curToken = null;
    private Swidget curSwidget;
    private Panel contentPanel;

    private HashMap<String, String> loginMsg;

    /** Creates a new instance of Main */
    public Main() {
        loginMsg = new HashMap<String, String>();
        loginMsg.put("loginMsg:username", "Login error, username not found.");
    }

    public void onModuleLoad() {

        History.addHistoryListener(this);
        tokenHeadersMap = new HashMap<String, Swidget>();
        cdm = new ClientDataManager();

        RootPanel.get().add(getMainPanel());
        showResults(History.getToken());
    }

    Widget getMainPanel() {
        DockPanel mainPanel = new DockPanel();
        mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        mainPanel.setWidth("95%");

        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

        contentPanel = new FlowPanel();

        Swidget serverInfo = new ServerInfoSwidget(cdm);
        registerTokenHeaders(serverInfo);
        cdm.registerSwidget(serverInfo);

        Swidget userPref = new ProfileSwidget(cdm);
        registerTokenHeaders(userPref);
        cdm.registerSwidget(userPref);

        Swidget dashboard = new DashboardSwidget(cdm);
        registerTokenHeaders(dashboard);
        cdm.registerSwidget(dashboard);
        menuItems.add(dashboard.getMenuItem());

        Swidget steeringRec = new SteeringSwidget(cdm);
        registerTokenHeaders(steeringRec);
        cdm.registerSwidget(steeringRec);
        menuItems.add(steeringRec.getMenuItem());

        Swidget artistSearch = new SimpleSearchSwidget(cdm);
        registerTokenHeaders(artistSearch);
        menuItems.add(artistSearch.getMenuItem());

        PageHeaderWidget uP = new PageHeaderWidget(cdm);
        cdm.registerSwidget(uP);
        cdm.setWidgets(uP);

        mainPanel.add(uP, DockPanel.NORTH);
        mainPanel.add(contentPanel, DockPanel.CENTER);

        uP.setMenuItems(menuItems);

        VerticalPanel footer = new VerticalPanel();
        footer.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        footer.add(new HTML("<br/><hr/><center>Powered by Sun Microsystems, Last.fm, Spotify, Wikipedia, Flickr,<br/>Youtube, Echo nest, Yahoo, Musicbrainz, Upcoming and Amazon</center>"));

        // if performance monitoring is enabled, add a button to the footer
        // that will show us the stats
        if (PerformanceTimer.isEnabled()) {

            HorizontalPanel hP = new HorizontalPanel();
            hP.setSpacing(5);

            // Add the server info link
            SpannedLabel perfmon = new SpannedLabel("PerfMon");
            perfmon.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    PerformanceTimer.showPopup();
                }
            });
            hP.add(perfmon);
            
            // Add the server info link
            SpannedLabel sI = new SpannedLabel("ServerInfo");
            sI.addClickListener(new ClickListener() {

                public void onClick(Widget sender) {
                    History.newItem("serverinfo:");
                }
            });
            hP.add(sI);

            footer.add(hP);

        }
        //
        // Hack to allow opening the spotify link while preventing losing the GWT state
        footer.add(new HTML("<iframe name=\"spotifyFrame\" height=\"0px\" frameborder=\"0\" />"));
        footer.setStyleName("footer");

        mainPanel.add(footer, DockPanel.SOUTH);
        mainPanel.setStyleName("main");
        return mainPanel;
    }

    /**
     * Registers all the swidget's token headers with the token headers map
     * @param swidget swidget to register
     */
    private void registerTokenHeaders(Swidget swidget) {
        for (String s : swidget.getTokenHeaders()) {
            tokenHeadersMap.put(s, swidget);
        }
    }


    public void onHistoryChanged(String historyToken) {
        if (!historyToken.equals(curToken)) {
            WebLib.trackPageLoad(historyToken);
            showResults(historyToken);
        }
    }

    private final String getResultNameHeader(String resultName) {
        return resultName.substring(0, resultName.indexOf(":") + 1);
    }

    private void showResults(String resultName) {
        String resultNameHeader = getResultNameHeader(resultName);

        // Set all menu items as deselected
        for (Swidget w : tokenHeadersMap.values()) {
            w.getMenuItem().setNotSelected();
        }

        if (tokenHeadersMap.containsKey(resultNameHeader)) {
            Swidget s = tokenHeadersMap.get(resultNameHeader);
            s.getMenuItem().setSelected();
            setResults(resultName, s);
        } else {
            // Check to see if we want to display a login message
            if (loginMsg.containsKey(resultName)) {
                Popup.showInformationPopup(loginMsg.get(resultName));
            }

            Swidget s = tokenHeadersMap.get("searchHome:");
            s.getMenuItem().setSelected();
            setResults("searchHome:", s);
        }
    }

    private void setResults(String historyName, Swidget newSwidget) {
        if (curSwidget == newSwidget) {
            return;
        }

        if (!History.getToken().equals(historyName)) {
            History.newItem(historyName);
            curToken = historyName;
        } else if (newSwidget.getTokenHeaders().contains(getResultNameHeader(historyName))) {
            curToken = historyName;
        }

        if (curSwidget != null) {
            contentPanel.remove(curSwidget);
            cdm.unregisterSwidget(curSwidget);
            curSwidget = null;
        }

        if (newSwidget != null) {
            newSwidget.update();
            contentPanel.add(newSwidget);
            cdm.registerSwidget(newSwidget);
            curSwidget = newSwidget;
        }
    }
}