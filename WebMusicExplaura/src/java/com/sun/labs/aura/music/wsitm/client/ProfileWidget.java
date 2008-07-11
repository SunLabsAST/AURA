/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class ProfileWidget extends Swidget {

    private FlowPanel mainPanel;
    private Image loadImage;

    public ProfileWidget(ClientDataManager cdm) {
        super("User preferences", cdm);
        mainPanel = new FlowPanel();
        
        initWidget(getWidget());
    }

    public Widget getWidget() {
        mainPanel.clear();
        updateMainPanel();
        return mainPanel;
    }

    public List<String> getTokenHeaders() {

        List<String> l = new ArrayList<String>();
        l.add("userpref:");
        return l;
    }

    public MenuItem getMenuTitle() {
        return null;
    }

    private void updateMainPanel() {
        if (cdm.isLoggedIn()) {
            mainPanel.add(getProfileWidget());
        } else {
            mainPanel.add(getMustBeLoggedInWidget());
        }
    }

    @Override
    public void update() {
        //
        // We need to update in case the user has logged in or logged off since his last
        // visit to this page
        mainPanel.clear();
        updateMainPanel();
    }

    public Widget getProfileWidget() {

        Map<String, TextBox> newSettings = new HashMap<String, TextBox>();

        VerticalPanel main = new VerticalPanel();

        Grid g = new Grid(2,2);

        TextBox lastfmUserBox = new TextBox();
        newSettings.put("lastfmUser", lastfmUserBox);
        if (cdm.getListenerDetails().lastfmUser!=null) {
            lastfmUserBox.setText(cdm.getListenerDetails().lastfmUser);
        }

        TextBox pandoraUserBox = new TextBox();
        newSettings.put("pandoraUser", pandoraUserBox);
        if (cdm.getListenerDetails().pandoraUser!=null) {
            pandoraUserBox.setText(cdm.getListenerDetails().pandoraUser);
        }

        main.add(new HTML("<h2>APML providers</h2>"));
        Label txt = new Label("Last.fm username :");
        //txt.setStyleName("whiteTxt");
        g.setWidget(0, 0, txt);
        g.setWidget(0, 1, lastfmUserBox);
        txt = new Label("Pandora username : ");
        //txt.setStyleName("whiteTxt");
        g.setWidget(1, 0, txt);
        g.setWidget(1, 1, pandoraUserBox);

        main.add(g);

        HorizontalPanel hP = new HorizontalPanel();

        Button updateButton = new Button("Update your profile");
        updateButton.addStyleName("main");
        updateButton.addClickListener(new UserPrefSubmitClickListener(newSettings));
        hP.add(updateButton);

        loadImage = new Image("ajax-loader-small.gif");
        loadImage.setVisible(false);
        hP.add(loadImage);

        main.add(hP);

        return main;

    }

    private void invokeUpdateListener(ListenerDetails lD) {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                Info.display("Information","Update sucessfull.", new Params());
                loadImage.setVisible(false);
            }

            public void onFailure(Throwable caught) {
                Window.alert("Update failed!");
                loadImage.setVisible(false);
            }
        };

        try {
            musicServer.updateListener(lD, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    class UserPrefSubmitClickListener implements ClickListener {

        private Map<String, TextBox> newSettings;

        public UserPrefSubmitClickListener(Map<String, TextBox> newSettings) {
            this.newSettings = newSettings;
        }

        public void onClick(Widget asdrg0) {

            loadImage.setVisible(true);

            ListenerDetails lD = cdm.getListenerDetails();

            lD.lastfmUser = newSettings.get("lastfmUser").getText();
            lD.pandoraUser = newSettings.get("pandoraUser").getText();

            invokeUpdateListener(lD);
        }

    }

}
