/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.*;
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
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class ProfileSwidget extends Swidget implements LoginListener {

    private FlowPanel mainPanel;
    private Image loadImage;

    public ProfileSwidget(ClientDataManager cdm) {
        super("User preferences", cdm);
        mainPanel = new FlowPanel();
        
        initWidget(getMainWidget());
    }

    public Widget getMainWidget() {
        mainPanel.clear();
        updateMainPanel();
        return mainPanel;
    }

    @Override
    public ArrayList<String> getTokenHeaders() {

        ArrayList<String> l = new ArrayList<String>();
        l.add("userpref:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        // no menu
        menuItem = new MenuItem();
    }

    private void updateMainPanel() {
        if (cdm.isLoggedIn()) {
            mainPanel.add(getProfileWidget());
        } else {
            mainPanel.add(getMustBeLoggedInWidget());
        }
    }

    public void doRemoveListeners() {
        onDelete();
    }

    public void onLogin(ListenerDetails lD) {
        update("");
    }

    public void onLogout() {
        update("");
    }

    public void onDelete() {
        cdm.getLoginListenerManager().removeListener(this);
    }

    @Override
    public void update(String historyToken) {
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
        if (cdm.getListenerDetails().getLastFmUser()!=null) {
            lastfmUserBox.setText(cdm.getListenerDetails().getLastFmUser());
        }

        TextBox pandoraUserBox = new TextBox();
        newSettings.put("pandoraUser", pandoraUserBox);
        if (cdm.getListenerDetails().getPandoraUser()!=null) {
            pandoraUserBox.setText(cdm.getListenerDetails().getPandoraUser());
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
                Popup.showInformationPopup("Update sucessfull.");
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

            lD.setLastFmUser(newSettings.get("lastfmUser").getText());
            lD.setPandoraUser(newSettings.get("pandoraUser").getText());

            invokeUpdateListener(lD);
        }
    }
}
