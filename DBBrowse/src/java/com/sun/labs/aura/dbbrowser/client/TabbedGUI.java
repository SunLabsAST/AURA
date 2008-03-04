
package com.sun.labs.aura.dbbrowser.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.dbbrowser.client.ItemDesc;
import java.util.HashSet;
import java.util.Set;

/**
 * The UI for the browser
 */
public class TabbedGUI extends TabPanel {
    private SearchPanel searchPanel;
    private AttnPanel attnPanel;
    private Set resultsPanels = new HashSet();
    
    public TabbedGUI() {
        searchPanel = new SearchPanel(this);
        add(searchPanel, "Search");
        selectTab(0);
        setHeight("400px");
    }
    
    public void addResults(String name, ItemDesc[] items) {
        ResultsPanel panel = new ResultsPanel(items, this);
        resultsPanels.add(panel);
        if (attnPanel != null) {
            insert(panel, name, getWidgetIndex(attnPanel));
        } else {
            add(panel, name);
        }
        selectTab(this.getWidgetIndex(panel));
    }
    
    public void showAttention(AttnDesc[] attns) {
        if (attnPanel == null) {
            attnPanel = new AttnPanel(attns, this);
            add(attnPanel, "Attention");
        } else {
            attnPanel.replaceAttns(attns);
        }
        selectTab(getWidgetIndex(attnPanel));
    }
    
    public void showError(String msg) {
        final DialogBox err = new DialogBox();
        err.setText(msg);
        Button ok = new Button();
        ok.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                err.hide();
            }
        });
        err.setWidget(ok);
        //DockPanel contents = new DockPanel();
        //contents.add(new Label(msg), DockPanel.CENTER);
    }
    
    public void removeTab(Widget tab) {
        remove(tab);
        selectTab(0);
        if (tab.equals(attnPanel)) {
            attnPanel = null;
        }
    }
}
