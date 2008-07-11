/*
 * Main.java
 *
 * Created on March 5, 2007, 12:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
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
import java.util.ArrayList;
import java.util.List;
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
    
    /** Creates a new instance of Main */
    public Main() {

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
        
        Label title = new Label("Search Inside the Music - The Music Explaura");
        title.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                History.newItem("searchHome:");
            }
        });
        title.setStyleName("title");

        List<MenuItem> menuItems = new ArrayList<MenuItem>();

        contentPanel = new FlowPanel();

        Swidget userPref = new ProfileWidget(cdm);
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
        cdm.setWidgets(uP, (SimpleSearchSwidget)artistSearch);
        
        mainPanel.add(uP, DockPanel.NORTH);
        mainPanel.add(title, DockPanel.NORTH);
        mainPanel.add(contentPanel, DockPanel.CENTER);

        uP.setMenuItems(menuItems);

        Panel footer = new HorizontalPanel();
        footer.add(new HTML("<hr/> Powered by Sun Microsystems, Last.fm, Spotify, Wikipedia, Flickr, Youtube, Yahoo, Musicbrainz, Upcoming and Amazon"));
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
            showResults(historyToken);
        }
    }

    private final String getResultNameHeader(String resultName) {
        return resultName.substring(0, resultName.indexOf(":")+1);
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