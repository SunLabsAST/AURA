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
