/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
