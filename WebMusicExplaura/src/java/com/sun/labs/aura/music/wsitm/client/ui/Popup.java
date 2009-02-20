/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.sun.labs.aura.music.wsitm.agentspecific.impl.CssDefsImpl;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
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
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
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

    //public static PopupPanel getPopupPanel(int secTillAutoClose) {
    public static PopupPanel getPopupPanel() {
        //final PopupPanelAutoClose popup = new PopupPanelAutoClose(5);
        final PopupPanel popup = new PopupPanel(true);
        return popup;
    }

    public static void showRoundedPopup(Widget w, String title, int width) {
        showRoundedPopup(w, title, getPopupPanel(), width);
    }
    
    public static void showRoundedPopup(Widget w, String title, final PopupPanel popup, int width) {
        showRoundedPopup(w, title, popup, -1, -1, width);
    }

    public static void showRoundedPopup(Widget w, String title, final PopupPanel popup, 
            int x, int y, int width) {
        
        Label titleLabel = null;
        if (title != null && title.length() > 0) {
            titleLabel = new Label(title);
            titleLabel.setStyleName("popupColors");
            titleLabel.addStyleName("popupTitle");
        }
        showRoundedPopup(w, titleLabel, popup, x, y, width);
    }

    /**
     * Display a rounded popup window
     * @param w Content widget
     * @param title Title widget. Use alternate method signature to pass in string
     * @param popup Popup returned by getPopupPanel()
     * @param x Position. Set to -1 to center it
     * @param y Position. Set to -1 to center it
     * @param cdm ClientDataManager
     * @param width Width of the enclosed widget. Will be used to resize the popup on ie
     */
    public static void showRoundedPopup(Widget w, Widget title, final PopupPanel popup, 
            int x, int y, int width) {

        VerticalPanel vP = new VerticalPanel();
        if (title != null) {
            vP.add(title);
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
        CssDefsImpl.impl.setRoundedPopupWidth(popup, width);

        if (x==-1 && y==-1) {
            popup.center();
        } else {
            popup.setPopupPosition(x, y);
            popup.show();
        }

    }

    public static void showInformationPopup(Widget html, int secTillAutoClose,
            boolean closeButton) {

        PopupPanel popup = getPopupPanel();

        VerticalPanel hP = new VerticalPanel();
        hP.setSpacing(4);
        hP.add(html);
        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);

        if (closeButton) {
            Button b = new Button("OK");
            b.addClickHandler(new DEClickHandler<PopupPanel>(popup) {
                @Override
                public void onClick(ClickEvent ce) {
                    data.hide();
                }
            });
            hP.add(b);
        }

        hP.setWidth("600px");
        showRoundedPopup(hP, "Information", popup, 600);
    }

    public static void showInformationPopup(String message) {
        showInformationPopup(new HTML("<p>"+message+"</p>"), 0, true);
    }

    public static PopupPanel showLoadingPopup() {
        HorizontalPanel hP = new HorizontalPanel();
        hP.add(new Image("ajax-ball-t.gif"));
        Label l = new Label("Loading...");
        l.addStyleName("tagPop2");
        hP.add(l);
        PopupPanel p = getPopupPanel();
        p.setWidth("130px");
        showRoundedPopup(hP, "Information", p, 130);
        return p;
    }

    /**
     * Display login popup dialog
     */
    public static void showLoginPopup() {

        /*
        VerticalPanel vP = new VerticalPanel();
        vP.setStyleName("popupColors");
        vP.setWidth("600px");

        // Login / create new panel
        Grid hP = new Grid(1,2);
        hP.setWidth("100%");

        // Login VP
        VerticalPanel loginVP = new VerticalPanel();
        loginVP.setVerticalAlignment(HorizontalPanel.ALIGN_TOP);
        loginVP.setWidth("275px");
        Label loginTitle = new Label("Login to an existing account");
        loginTitle.addStyleName("h2");
        loginTitle.addStyleName("alternateHTitle");

        HorizontalPanel loginHP = new HorizontalPanel();
        loginHP.getElement().getStyle().setPropertyPx("marginTop", 12);
        loginHP.setWidth("100%");
        loginHP.add(new HTML("<br />"));
        Label oiL = new Label("OpenID: ");
        oiL.setStyleName("popupColors");
        loginHP.add(oiL);
        loginHP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        final TextBox loginTb = new TextBox();
        loginTb.setStyleName("openidField");
        final Command loginCmd = new Command() {
            public void execute() {
                Window.Location.assign("./Login?app-openid-auth=true&app-openid-name=" + loginTb.getText());
            }
        };

        loginTb.setText(Cookies.getCookie("app-openid-uniqueid"));
        loginTb.addKeyboardListener(new KeyboardListener() {

            public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                if (keyCode == KEY_ENTER) {
                    DeferredCommand.addCommand(loginCmd);
                }
            }

            public void onKeyPress(Widget sender, char keyCode, int modifiers) {}
            public void onKeyUp(Widget sender, char keyCode, int modifiers) {}
        });

        loginHP.add(loginTb);
        loginHP.add(new HTML("<br />"));
        Button loginButton = new Button();
        loginButton.getElement().getStyle().setPropertyPx("marginTop", 8);
        loginButton.setText("Login with your OpenID");
        loginButton.addClickListener(new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                DeferredCommand.addCommand(loginCmd);
            }
        });

        loginVP.add(loginTitle);
        loginVP.add(loginHP);
        loginVP.add(loginButton);
        hP.setWidget(0, 0, loginVP);
        hP.getCellFormatter().setVerticalAlignment(0, 0, VerticalPanel.ALIGN_TOP);

        // Create new account VP
        VerticalPanel createVP = new VerticalPanel();
        createVP.setWidth("275px");
        Label createTitle = new Label("Create a new account");
        createTitle.addStyleName("h2");
        createTitle.addStyleName("alternateHTitle");

        HTML createTxt = new HTML("<p>OpenID is an open, decentralised single " +
                "sign-on standard, allowing users to log onto many services " +
                "with the same digital identity.</p><p>Some free OpenID " +
                "providers : <a href=\"https://www.myopenid.com\">myOpenID</a></p>");
        createTxt.setStyleName("popupColors");

        createVP.add(createTitle);
        createVP.add(createTxt);
        hP.setWidget(0, 1, createVP);
        hP.getCellFormatter().setVerticalAlignment(0, 1, VerticalPanel.ALIGN_TOP);

        vP.add(hP);
        */

        Grid titlePanel = new Grid(1,2);
        titlePanel.setWidth("100%");
        titlePanel.setStyleName("popupColors");
        titlePanel.addStyleName("popupTitle");
        Image.prefetch("320px-OpenID_logo.svg.png");    // for ie
        Image openIdImage = new Image("320px-OpenID_logo.svg.png");
        openIdImage.setHeight("60px");
        openIdImage.setWidth("160px");
        titlePanel.setWidget(0, 0, openIdImage);
        titlePanel.getColumnFormatter().setWidth(0, "85px");
        titlePanel.setWidget(0, 1, new Label("Login required"));
        titlePanel.getCellFormatter().setHorizontalAlignment(0, 1, HorizontalPanel.ALIGN_LEFT);
        titlePanel.getCellFormatter().setVerticalAlignment(0, 1, VerticalPanel.ALIGN_BOTTOM);

        VerticalPanel vP = new VerticalPanel();
        Label l = new Label("Registering and logging in the Music Explaura is coming soon!");
        l.setStyleName("popupColors");
        vP.add(l);

        vP.setWidth("450px");
        showRoundedPopup(vP, titlePanel, getPopupPanel(), -1, -1, 450);
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
