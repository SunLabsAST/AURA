/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Presents a way to select between the different components of the tool.
 */
public class MainSelector extends DockPanel {
    protected HorizontalPanel toolLinks;
    
    protected DeckPanel contents;
    
    public MainSelector() {
        super();
        toolLinks = new HorizontalPanel();
        toolLinks.setStylePrimaryName("db-topBar");
        toolLinks.add(new Label("Select a function: "));
        add(toolLinks, NORTH);
        contents = new DeckPanel();
        add(contents, CENTER);
    }
    
    public void addTool(String toolName, final Widget tool) {
        Button b = new Button(toolName, new ClickListener() {
            public void onClick(Widget sender) {
                int idx = contents.getWidgetIndex(tool);
                contents.showWidget(idx);
            }
        });
        b.setStylePrimaryName("main-SelectorButton");
        toolLinks.add(b);
        contents.add(tool);
    }

    public void select(Widget tool) {
        int idx = contents.getWidgetIndex(tool);
        contents.showWidget(idx);
    }
}
