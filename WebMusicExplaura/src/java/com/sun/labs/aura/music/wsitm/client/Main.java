/*
 * Main.java
 *
 * Created on March 5, 2007, 12:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author plamere
 */
public class Main implements EntryPoint {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    public void onModuleLoad() {
        RootPanel.get().add(getMainPanel());
    }
    
    Widget getMainPanel() {
        DockPanel mainPanel = new DockPanel();
        mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        mainPanel.setWidth("95%");
        
        Label title = new Label("Search Inside the Music - The Music Explaura");
        title.setStyleName("title");
        mainPanel.add(title, DockPanel.NORTH);
        
        if (false) {
            TabPanel tabs = new TabPanel();
            
            Swidget artistSearch = new SimpleSearchWidget();
            Label nothing = new Label("Nothing here yet!");
            
            tabs.setWidth("100%");
            tabs.add(artistSearch, "Search");
            tabs.add(nothing, "Explore");
            tabs.selectTab(0);
            tabs.setHeight("100%");
            mainPanel.add(tabs, DockPanel.CENTER);
        } else {
            Swidget artistSearch = new SimpleSearchWidget();
            mainPanel.add(artistSearch, DockPanel.CENTER);
        }
        
        
        Panel footer = new HorizontalPanel();
        footer.add(new HTML("<hr/> Powered by Sun Microsystems, Last.fm, Spotify, Wikipedia, Flickr, Youtube, Yahoo, Musicbrainz, Upcoming and Amazon"));
        footer.setStyleName("footer");
        
        mainPanel.add(footer, DockPanel.SOUTH);
        mainPanel.setStyleName("main");
        return mainPanel;
    }
}