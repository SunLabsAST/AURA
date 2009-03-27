/*
 * Swidget.java
 *
 * Created on March 7, 2007, 5:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.swidget;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.RoundedPanel;
import com.sun.labs.aura.music.wsitm.client.ui.bundles.ArtistRelatedBundle;
import java.util.ArrayList;

/**
 *
 * @author plamere
 */
public abstract class Swidget extends Composite implements HasListeners {

    public static ArtistRelatedBundle playImgBundle =
            (ArtistRelatedBundle) GWT.create(ArtistRelatedBundle.class);


    private String name;

    private boolean useTopLoader = false;
    private Label topMessage = null;
    private Image topActivityIcon = null;
    private Grid topMsgGrid = null;

    protected MusicSearchInterfaceAsync musicServer;
    protected ClientDataManager cdm;

    protected MenuItem menuItem;

    public Swidget(String name, ClientDataManager cdm) {
        this.name = name;
        this.cdm = cdm;
        initMenuItem();
        initRPC();
    }

    /**
     * Returns a list of all the token headers associated with this swidget.
     * @return
     */
    public abstract ArrayList<String> getTokenHeaders();

    /**
     * Returns this section's title as it should appear in the top menu
     * @return null if this swidget should not appear in the top menu.
     */
    public final MenuItem getMenuItem() throws WebException {
        if (menuItem == null) {
            throw new WebException("Menuitem not initialised in swidget '" + name + "'");
        }
        return menuItem;
    };

    /**
     * Called when the swidget is being constructed. Method should create the
     * menuItem object.
     */
    protected abstract void initMenuItem();

    /**
     * Gets called when the swidget is displayed. By default, this does nothing.
     * Usefull for swidgets that might block out content out content is user is
     * not logged in and still have that cached when he returns and might have
     * logged in.
     */
    public void update(String historyToken) {
        updateWindowTitle("");
    }

    /**
     * Returns the swidget's name
     * @return
     */
    public final String getName() {
        return name;
    }

    /**
     * Display help when top header link is clicked
     */
    public void displayHelp() {
        Popup.showInformationPopup("Sorry, no help is available for this section.");
    }

    protected final void updateWindowTitle(String swidgetTitle) {
        if (swidgetTitle!=null && swidgetTitle.length()>0) {
            Window.setTitle(swidgetTitle+" - The Music Explaura");
        } else {
            Window.setTitle("The Music Explaura");
        }
    }

    private final void initRPC() {
        // (1) Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the async version of
        // the interface. The cast is always safe because the generated proxy
        // implements the async interface automatically.
        //
        musicServer = (MusicSearchInterfaceAsync) GWT.create(MusicSearchInterface.class);

        // (2) Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) musicServer;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "musicsearch";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
    }

    /**
     * Returns Label with generic message informing the user that he must be logged
     * in to do what he is trying to do
     * @return
     */
    protected Label getMustBeLoggedInWidget() {
        return new Label("Sorry but you must be logged in to access this page.");
    }


    protected void initWidget(Widget widget, boolean useTopLoader) {
        this.useTopLoader = useTopLoader;
        if (useTopLoader) {
            topMessage = new Label();
            topMessage.setStyleName("topMsgIndicator");

            topActivityIcon = new Image(WebLib.ICON_WAIT_SUN);
            topActivityIcon.setVisible(false);
            topActivityIcon.setStyleName("img");

            topMsgGrid = new Grid(1,3);
            topMsgGrid.setWidth("100%");
            topMsgGrid.getCellFormatter().setHeight(0, 1, "35px");
            
            topMsgGrid.getCellFormatter().setWidth(0, 0, "200px");
            topMsgGrid.getCellFormatter().setWidth(0, 1, "100%");
            topMsgGrid.getCellFormatter().setWidth(0, 2, "200px");

            topMsgGrid.getCellFormatter().setHorizontalAlignment(0, 1, HorizontalPanel.ALIGN_CENTER);

            VerticalPanel mainPanel = new VerticalPanel();
            mainPanel.setWidth("100%");
            mainPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
            mainPanel.add(topMsgGrid);
            mainPanel.add(widget);
            super.initWidget(mainPanel);
            showHelpOffer();
        } else {
            super.initWidget(widget);
        }
    }

    protected void showLoader() {
        if (useTopLoader) {
            topMessage.setVisible(false);
            topActivityIcon.setVisible(true);
            topMsgGrid.setWidget(0, 1, topActivityIcon);
        }
    }

    protected void hideLoader() {
        if (useTopLoader) {
            topActivityIcon.setVisible(false);
        }
    }

    protected void showTopMessage(String message) {
        if (useTopLoader) {
            topMessage.setText(message);
            topMessage.setVisible(true);
            topActivityIcon.setVisible(false);
            topMsgGrid.setWidget(0, 1, topMessage);
        }
    }

    protected void showHelpOffer() {
        if (useTopLoader) {
            HorizontalPanel hP = new HorizontalPanel();
            hP.setSpacing(3);
            hP.add(playImgBundle.topArrow().createImage());
            Label helpLbl = new Label("Need help?");
            helpLbl.addStyleName("tag1");
            helpLbl.setWidth("100px");
            hP.add(helpLbl);
            hP.getElement().getStyle().setPropertyPx("marginLeft", 30);
            topMsgGrid.setWidget(0, 0, hP);

            // Clear message in 10 seconds
            Timer t = new Timer() {
                @Override
                public void run() {
                    topMsgGrid.clearCell(0, 0);
                }
            };
            t.schedule(1000*10);
        }
    }
}
