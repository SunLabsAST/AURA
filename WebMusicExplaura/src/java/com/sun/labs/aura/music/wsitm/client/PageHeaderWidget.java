/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.Map;


/**
 *
 * @author mailletf
 */
public class PageHeaderWidget extends Composite {

    private MusicSearchInterfaceAsync musicServer;
    private Grid mainPanel;
    private TextBox txtbox;

    private ClientDataManager cdm;
    
    public PageHeaderWidget(ClientDataManager cdm) {
        initRPC();
        this.cdm=cdm;
        initWidget(getWidget());
    }
    
    private void initRPC() {
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
    
    @Override
    public Widget getWidget() {
        
        
        
        mainPanel = new Grid(1,3);
        mainPanel.setStyleName("pageHeader");
        mainPanel.setWidth("100%");
        
        txtbox = new TextBox();
        txtbox.addKeyboardListener(new KeyboardListener() {

            public void onKeyDown(Widget arg0, char arg1, int arg2) {
            }

            public void onKeyPress(Widget arg0, char keyCode, int arg2) {
                if (keyCode == KEY_ENTER) {
                    fetchUserInfo();
                }
            }

            public void onKeyUp(Widget arg0, char arg1, int arg2) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        
        Button b = new Button();
        b.setText("Set Last.FM user");
        b.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                fetchUserInfo();
            }
        });
        
        
        
        HorizontalPanel h = new HorizontalPanel();
        h.add(txtbox);
        h.add(b);
        mainPanel.setWidget(0, 0, h);
                
        return mainPanel;
        
    }

    private void fetchUserInfo() {
        mainPanel.clearCell(0, 0);
        HorizontalPanel h = new HorizontalPanel();
        h.setWidth("300px");
        h.add(new Image("ajax-ball.gif"));
        h.add(new Label("Fetching your user profile..."));
        mainPanel.setWidget(0, 1, h);

        invokeGetUserTagCloud(txtbox.getText());
    }
    
    private void invokeGetUserTagCloud(String lastfmUser) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                mainPanel.clearCell(0, 1);
                logInDetails lin = (logInDetails) result;
                ItemInfo[] tagCloud = lin.tags;
                Double maxScore = lin.maxScore;
                String favArtistName = lin.favArtistName;
                
                if (tagCloud==null || tagCloud.length==0) {
                    Window.alert("Error fetching your user information.");
                } else {
                    cdm.setTagCloud(tagCloud, txtbox.getText(), maxScore, favArtistName);
                    
                    mainPanel.setWidget(0,0,new Label("Logged in: "+cdm.getLastFmUser()));
                    
                    Label viewCloudLbl = new Label("View tag cloud");
                    viewCloudLbl.addClickListener(new ClickListener() {

                        public void onClick(Widget arg0) {
                            cdm.getSimpleSearchWidget().showTagCloud("Your tag cloud", cdm.getTagCloud());
                        }
                    });
                    
                    mainPanel.setWidget(0, 2, viewCloudLbl);
                    
                }
            }

            public void onFailure(Throwable caught) {
                //failureAction(caught);
                Window.alert(caught.toString());
            }
        };

        // (4) Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        //
        //  Provide your own name.
        try {
            musicServer.getUserTagCloud(lastfmUser, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }
}
