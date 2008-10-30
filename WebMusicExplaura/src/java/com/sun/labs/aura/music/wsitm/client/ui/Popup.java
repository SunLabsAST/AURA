/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Timer;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;

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

        DockPanel docPanel = new DockPanel();

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

    public static PopupPanelAutoClose getPopupPanel(int secTillAutoClose) {
        final PopupPanelAutoClose popup = new PopupPanelAutoClose(5);
        return popup;
    }

    public static void showRoundedPopup(Widget w, String title) {
        showRoundedPopup(w, title, getPopupPanel());
    }
    
    public static void showRoundedPopup(Widget w, String title, final PopupPanel popup) {

        VerticalPanel vP = new VerticalPanel();
        if (title != null && title.length() > 0) {
            Label titleLabel = new Label(title);
            titleLabel.setStyleName("popupColors");
            titleLabel.addStyleName("popupTitle");
            vP.add(titleLabel);
        }
        w.getElement().getStyle().setPropertyPx("padding", 5);
        w.addStyleName("popupColors");
        vP.add(w);
        
        Grid fP = new Grid(1,1);
        fP.setStyleName("popupColors");
        fP.setHeight("100%");
        fP.setWidget(0, 0, vP);
        
        RoundedPanel rp = new RoundedPanel(fP, RoundedPanel.ALL, 5);
        rp.setCornerStyleName("popupColors");
        popup.add(rp);
        popup.setAnimationEnabled(true);
        popup.center();
        popup.center();
    }

    public static void showInformationPopup(HTML html, int secTillAutoClose) {

        PopupPanel popup = getPopupPanel(true);

        Button b = new Button("OK");
        b.addClickListener(new DataEmbededClickListener<PopupPanel>(popup) {

            public void onClick(Widget sender) {
                data.hide();
            }
        });

        VerticalPanel hP = new VerticalPanel();
        hP.setSpacing(4);
        hP.add(html);
        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        hP.add(b);

        showRoundedPopup(hP, "Information", popup);
    }

    public static void showInformationPopup(String message) {
        showInformationPopup(new HTML("<p>"+message+"</p>"), 0);
    }

    private class PopupPanelAutoClose extends PopupPanel {

        private Timer t;
        private int secLeftTillClose;

        public PopupPanelAutoClose(int seconds) {
            super(true);
            this.secLeftTillClose = seconds;

            if (seconds > 0) {
                t = new TimerWithPopupPanel(this);
                t.schedule(1000);
            }
        }

        private class TimerWithPopupPanel extends Timer {

            private PopupPanel popup;

            public TimerWithPopupPanel(PopupPanel popup) {
                this.popup = popup;
            }

            @Override
            public void run() {
                secLeftTillClose--;
                if (secLeftTillClose==0) {
                    popup.hide();
                } else {
                    doEachSec();
                }
            }
        }

        /**
         * Will be called at each second. Overwrite to do any action, like update
         * button label
         */
        protected void doEachSec() {}
    }
}
