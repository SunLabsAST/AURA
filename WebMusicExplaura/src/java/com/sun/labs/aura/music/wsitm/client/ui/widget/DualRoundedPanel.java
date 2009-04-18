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

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.agentspecific.impl.CssDefsImpl;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author mailletf
 */
public class DualRoundedPanel extends Composite {

    private Grid mainPanel;

    public DualRoundedPanel() {
        mainPanel = new Grid(2,1);
        mainPanel.setCellPadding(0);
        mainPanel.setCellSpacing(0);
        initWidget(mainPanel);
    }

    public DualRoundedPanel(Widget title, Widget content) {
        this();
        setHeader(title);
        setContent(content, true);
    }

    public DualRoundedPanel(String title, Widget content) {
        this();
        Label titleLbl = new Label(title);
        titleLbl.setStyleName("h2");
        setHeader(titleLbl);
        setContent(content);
    }

    public void setHeader(Widget w) {
        w.getElement().getStyle().setPropertyPx("marginBottom", 0);
        mainPanel.setWidget(0, 0, w);
    }

    public void setContent(Widget w) {
        setContent(w, true);
    }

    public void setContent(Widget w, boolean addRoundedPanel) {
        if (addRoundedPanel) {
            RoundedPanel rp = new RoundedPanel(w, RoundedPanel.BOTTOM, 4);
            w.addStyleName("roundedPageBack");
            rp.setCornerStyleName("roundedPageBack");
            mainPanel.setWidget(1, 0, rp);
        } else {
            mainPanel.setWidget(1, 0, w);
        }
    }

}
