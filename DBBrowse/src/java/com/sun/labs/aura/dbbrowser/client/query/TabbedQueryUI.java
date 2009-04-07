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

package com.sun.labs.aura.dbbrowser.client.query;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashSet;
import java.util.Set;

/**
 * The UI for the browser
 */
public class TabbedQueryUI extends TabPanel {
    private SearchPanel searchPanel;
    private AttnPanel attnPanel;
    private Set resultsPanels = new HashSet();
    private int prevTab = 0;
    
    public TabbedQueryUI() {
        searchPanel = new SearchPanel(this);
        add(searchPanel, "Search");
        selectTab(0);
        setHeight("400px");
    }
    
    public void selectTab(int index) {
        prevTab = getTabBar().getSelectedTab();
        super.selectTab(index);
        
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
        err.setText("Alert!");
        DockPanel contents = new DockPanel();
        contents.add(new Label(msg), DockPanel.CENTER);
        Button ok = new Button("OK");
        ok.addClickListener(new ClickListener() {
            public void onClick(Widget arg0) {
                err.hide();
            }
        });
        contents.add(ok, DockPanel.SOUTH);
        err.setWidget(contents);
        err.show();
    }
    
    public void removeTab(Widget tab) {
        remove(tab);
        selectTab(0);
        if (tab.equals(attnPanel)) {
            attnPanel = null;
        }
    }
    
    public void removeAttnTab(Widget tab) {
        remove(tab);
        selectTab(prevTab);
        attnPanel = null;
    }
    
    public static String getLinkText(String url) {
        String name = url;
        if (name.length() > 40) {
            name = name.substring(0, 40) + "...";
        }
        return "<a href=\"" + url + "\">" + name + "</a>";
    }
}
