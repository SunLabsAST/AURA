/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public abstract class Popup {

    public static DialogBox getDialogBox() {
        final DialogBox popup = new DialogBox(true);
        return popup;
    }

    public static void showPopup(Widget w, String title) {
        showPopup(w, title, getDialogBox());
    }

    public static void showPopup(Widget w, String title, final DialogBox popup) {
        //final DialogBox popup = new DialogBox(true);
        DockPanel docPanel = new DockPanel();

        //docPanel.setStyleName("borderpopup");
        //docPanel.addStyleName("cw-DialogBox");
        Label closeButton = new Label("Close");
        closeButton.setStyleName("clickableLabel");
        closeButton.addStyleName("whiteTxt");
        closeButton.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                popup.hide();
            }
        });

        FlowPanel container = new FlowPanel();
        container.setStyleName("outerpopup");
        container.add(w);

        docPanel.add(container, DockPanel.CENTER);
        docPanel.add(closeButton, DockPanel.SOUTH);
        docPanel.setCellHorizontalAlignment(closeButton, DockPanel.ALIGN_RIGHT);
        popup.add(docPanel);
        popup.setText(title);
        popup.setAnimationEnabled(true);
        popup.center();
    }

}
