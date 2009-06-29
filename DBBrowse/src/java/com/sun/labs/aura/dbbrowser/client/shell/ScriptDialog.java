/*
 * Copyright 2005-2009 Sun Microsystems, Inc. All Rights Reserved.
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
package com.sun.labs.aura.dbbrowser.client.shell;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog box for entering scripts into the UI
 */
public class ScriptDialog extends DialogBox {
    protected String scriptCode;

    protected TextArea scriptArea;

    public ScriptDialog() {
        scriptArea = new TextArea();
        scriptArea.setCharacterWidth(80);
        scriptArea.setVisibleLines(20);
        scriptArea.setStylePrimaryName("shell-margin");

        Button closeBtn = new Button("Close");
        closeBtn.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                hide();
            }
        });

        DockPanel mainPanel = new DockPanel();
        mainPanel.add(new Label("Script code will be written at /tmp/script.js:"), DockPanel.NORTH);
        mainPanel.add(scriptArea, DockPanel.CENTER);
        mainPanel.add(closeBtn, DockPanel.SOUTH);

        setText("Enter Script");
        setWidget(mainPanel);
        setPopupPosition(30,30);
    }

    public String getScript() {
        return scriptArea.getText();
    }
}
