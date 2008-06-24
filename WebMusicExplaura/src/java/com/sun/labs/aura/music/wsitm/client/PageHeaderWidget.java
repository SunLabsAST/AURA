/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 *
 * @author mailletf
 */
public class PageHeaderWidget extends Swidget {

    private Grid mainPanel;
    private TextBox txtbox;

    // toolbar objects
    private ToolBar toolBar;
    TextToolItem recTypeToolItem;
    
    
    public PageHeaderWidget(ClientDataManager cdm) {
        super("pageHeader",cdm);
        this.cdm=cdm;
        initWidget(getWidget());
    }
    
    public Widget getWidget() {
        
        mainPanel = new Grid(1,3);
        mainPanel.getColumnFormatter().setWidth(0, "33%");
        mainPanel.getColumnFormatter().setWidth(1, "33%");
        mainPanel.getCellFormatter().getElement(0, 1).setAttribute("align", "center");
        mainPanel.getColumnFormatter().setWidth(2, "33%");
        mainPanel.setStyleName("pageHeader");
        mainPanel.setWidth("100%");

        //
        // Set the recommendation type toolbar
        HorizontalPanel hP = new HorizontalPanel();
        Label lbl = new Label("Recommendation type : ");
        lbl.setStyleName("headerMenuMed");
        hP.add(lbl);
        
        toolBar = new ToolBar();
        toolBar.setWidth(100);
        Info.display("toolbar",toolBar.getBaseStyle(), new Params());
        
        recTypeToolItem = new TextToolItem("Loading...");
        recTypeToolItem.setIconStyle("icon-menu-show");

        toolBar.add(recTypeToolItem);
        hP.add(toolBar);
        mainPanel.setWidget(0, 2, hP);
        mainPanel.getCellFormatter().getElement(0, 2).setAttribute("align", "right");

        invokeGetSimTypes();

        //
        // Set the section menu
        MainMenu mm = new MainMenu();
        registerLoginListener(mm);
        mainPanel.setWidget(0, 1, mm);

        populateMainPanel();
     
        return mainPanel;
        
    }

    private void populateMainPanel() {

        mainPanel.setWidget(0,0, new Label("Please wait while we fetch your session information..."));
        invokeGetUserSessionInfo();

    }

    private void invokeGetSimTypes() {
        AsyncCallback callback = new AsyncCallback() {

            public void onFailure(Throwable arg0) {
                Window.alert("Error fetching similarity types.");
            }

            public void onSuccess(Object arg0) {
                cdm.setSimTypes((Map<String, String>) arg0);
                
                Menu menu = new Menu();
                boolean firstElem = true;
                for (String name : cdm.getSimTypes().keySet()) {
                    CheckMenuItem r = new CheckMenuItem(name);
                    r.setGroup("recType");
                    r.setItemId(name);
                    r.setToolTip(cdm.getSimTypes().get(name));
                    r.addSelectionListener(new SelectionListener<MenuEvent>() {

                        public void componentSelected(MenuEvent arg0) {
                            // If the selection has changed
                            if (!cdm.getCurrSimTypeName().equals(((CheckMenuItem)arg0.item).getItemId())) {
                                recTypeToolItem.setText(((CheckMenuItem)arg0.item).getItemId());
                                cdm.setCurrSimTypeName(((CheckMenuItem)arg0.item).getItemId());

                                if (!cdm.getCurrArtistID().equals("")) {
                                    cdm.displayWaitIconUpdatableWidgets();
                                    invokeGetArtistInfo(cdm.getCurrArtistID(),false);
                                }
                            }
                        }
                    });
                    
                    if (firstElem) {
                        r.setChecked(true);
                        cdm.setCurrSimTypeName(name);
                        recTypeToolItem.setText(name);
                        firstElem=false;
                    } else {
                        r.setChecked(false);
                    }
                    
                    menu.add(r);
                }
                recTypeToolItem.setMenu(menu);
            }
        };

        try {
            musicServer.getSimTypes(callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private void fetchUserInfo() {
        Image.prefetch("ajax-ball.gif");
        mainPanel.clearCell(0, 0);
        HorizontalPanel h = new HorizontalPanel();
        h.setWidth("300px");
        h.add(new Image("ajax-ball.gif"));
        Label lbl = new Label("Connecting...");
        lbl.setStyleName("headerMenuMed");
        h.add(lbl);
        mainPanel.setWidget(0, 0, h);

        // Run in deffered command to let the progress image load
        DeferredCommand.addCommand(new Command(){ public void execute(){
            Window.Location.assign("./Login?app-openid-auth=true&app-openid-name=" + txtbox.getText());
        }});
    }

    private void invokeTerminateSession() {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                Info.display("Information", "You are now logged out. Have a nice and productive day.", new Params());
                populateLoginBox();
            }

            public void onFailure(Throwable caught) {
                //failureAction(caught);
                Window.alert(caught.toString());
            }
        };

        try {
            cdm.resetUser();
            musicServer.terminateSession(callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private void invokeGetUserTagCloud(String lastfmUser) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                mainPanel.clearCell(0, 1);
                ListenerDetails lin = (ListenerDetails) result;
                if (lin==null || lin.userTags==null || lin.userTags.length==0) {
                    Window.alert("Error fetching your user information.");
                    cdm.resetUser();
                    populateMainPanel();
                } else {
                
                    ItemInfo[] tagCloud = lin.userTags;
                    cdm.setTagCloud(tagCloud, txtbox.getText(), lin.favArtistDetails);
                    
                    mainPanel.setWidget(0,0,new Label("Logged in: "+cdm.getLastFmUser()));
                    
                    Label viewCloudLbl = new Label("View tag cloud");
                    viewCloudLbl.setHorizontalAlignment(Label.ALIGN_RIGHT);
                    viewCloudLbl.addClickListener(new ClickListener() {

                        public void onClick(Widget arg0) {
                            cdm.getSimpleSearchWidget().showTagCloud("Your your tag cloud", cdm.getTagCloud());
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

        try {
            musicServer.getUserTagCloud(lastfmUser, cdm.getCurrSimTypeName(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private void invokeGetUserSessionInfo() {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {

                ListenerDetails l = (ListenerDetails) result;
                cdm.setListenerDetails(l);
                if (l.loggedIn) {
                    String name;
                    if (l.nickName != null) {
                        name = l.nickName;
                    } else {
                        name = l.realName;
                    }

                    HorizontalPanel hP = new HorizontalPanel();
                    hP.setSpacing(4);
                    Label loggedLbl = new Label(name);
                    loggedLbl.addClickListener(new ClickListener() {

                        public void onClick(Widget arg0) {
                            History.newItem("userpref:");
                        }
                    });
                    loggedLbl.addStyleName("headerMenuMedItem");
                    hP.add(loggedLbl);

                    VerticalPanel vP = new VerticalPanel();

                    /*
                    Label lnk = new Label("Edit profile");
                    lnk.addClickListener(new ClickListener() {

                        public void onClick(Widget arg0) {
                            History.newItem("userpref:");
                        }
                    });
                    lnk.setStyleName("headerMenuTinyItem");
                    vP.add(lnk);
                    */

                    Label lnk = new Label("Logout");
                    lnk.addClickListener(new ClickListener() {

                        public void onClick(Widget arg0) {
                            cdm.resetUser();
                            invokeTerminateSession();
                        }
                    });
                    lnk.setStyleName("headerMenuTinyItem");
                    vP.add(lnk);

                    hP.add(vP);

                    mainPanel.setWidget(0, 0, hP);

                } else {
                    populateLoginBox();
                }
            }

            public void onFailure(Throwable caught) {
                //failureAction(caught);
                Window.alert(caught.toString());
            }
        };

        try {
            musicServer.getLogInDetails(callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    private void populateLoginBox() {

        txtbox = new TextBox();
        txtbox.setText(Cookies.getCookie("app-openid-uniqueid"));
        txtbox.addKeyboardListener(new KeyboardListener() {

            public void onKeyPress(Widget arg0, char keyCode, int arg2) {
                if (keyCode == KEY_ENTER) {
                    fetchUserInfo();
                }
            }

            public void onKeyDown(Widget arg0, char arg1, int arg2) {
            }

            public void onKeyUp(Widget arg0, char arg1, int arg2) {
            }
        });

        Button b = new Button();
        b.setText("Login with your openID");
        b.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                fetchUserInfo();
            }
        });

        HorizontalPanel h = new HorizontalPanel();
        h.add(txtbox);
        h.add(b);
        mainPanel.setWidget(0, 0, h);

    }

    /**
     * Fetch artist details. Used when similarity type is updated
     * @param artistID
     * @param refresh
     */
    private void invokeGetArtistInfo(String artistID, boolean refresh) {

        if (artistID.startsWith("artist:")) {
            artistID = artistID.replaceAll("artist:", "");
        }

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                // do some UI stuff to show success
                ArtistDetails artistDetails = (ArtistDetails) result;
                if (artistDetails != null && artistDetails.isOK()) {
                    cdm.updateUpdatableWidgets(artistDetails);
                } else {
                    Window.alert("An error occured while fetching the new recommendations.");
                }
            }

            public void onFailure(Throwable caught) {
                Window.alert("An error occured while fetching the new recommendations.");
            }
        };

        try {
            musicServer.getArtistDetails(artistID, refresh, cdm.getCurrSimTypeName(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    public List<String> getTokenHeaders() {
        return new LinkedList<String>();
    }

    public class MainMenu extends LoginListener {

        private Grid p;
        private boolean loggedIn=false;

        public MainMenu() {
            p = new Grid(1,1);
            update();
            initWidget(p);
        }

        public Widget getWidget() {
            return p;
        }

        private void update() {

            HorizontalPanel hP = new HorizontalPanel();
            hP.setSpacing(8);

            Label sLabel = new Label("Search");
            sLabel.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    History.newItem(cdm.getCurrSearchWidgetToken());
                }
            });
            sLabel.setStyleName("headerMenuMedItem");
            hP.add(sLabel);

            if (loggedIn) {
                sLabel = new Label("Dashboard");
                sLabel.addClickListener(new ClickListener() {

                    public void onClick(Widget arg0) {
                        History.newItem("dashboard:");
                    }
                });
                sLabel.setStyleName("headerMenuMedItem");
                hP.add(sLabel);
            } 

            p.setWidget(0, 0, hP);
        }

        public void onLogin(ListenerDetails lD) {
            loggedIn=true;
            update();
        }

        public void onLogout() {
            loggedIn=false;
            update();
        }
    }

}