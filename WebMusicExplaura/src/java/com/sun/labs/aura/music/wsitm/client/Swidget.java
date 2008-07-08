/*
 * Swidget.java
 *
 * Created on March 7, 2007, 5:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author plamere
 */
public abstract class Swidget extends Composite {

    private String name;

    protected MusicSearchInterfaceAsync musicServer;
    protected ClientDataManager cdm;

    private Set<LoginListener> listeners;

    public Swidget(String name, ClientDataManager cdm) {
        this.name = name;
        this.cdm = cdm;
        listeners = new HashSet<LoginListener>();
        initRPC();
    }

    /**
     * Returns a list of all the token headers associated with this swidget
     * @return
     */
    public abstract List<String> getTokenHeaders();

    /**
     * Gets called when the swidget is displayed. By default, this does nothing.
     * Usefull for swidgets that might block out content out content is user is
     * not logged in and still have that cached when he returns and might have
     * logged in.
     */
    public void update() {}

    public final String getName() {
        return name;
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

    protected Widget getMustBeLoggedInWidget() {
        return new Label("Sorry but you must be logged in to access this page.");
    }

    protected void registerLoginListener(LoginListener ll) {
        listeners.add(ll);
    }

    protected void removeLoginListener(LoginListener ll) {
        listeners.remove(ll);
    }

    protected void removeAllLoginListeners() {
        listeners = new HashSet<LoginListener>();
    }

    public void triggerLogin(ListenerDetails lD) {
        for (LoginListener ll : listeners) {
            ll.onLogin(lD);
        }
    }

    public void triggerLogout() {
        for (LoginListener ll : listeners) {
            ll.onLogout();
        }
    }

}
