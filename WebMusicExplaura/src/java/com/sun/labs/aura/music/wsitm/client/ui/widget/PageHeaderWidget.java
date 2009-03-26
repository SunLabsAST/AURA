/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.Swidget;
import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.RoundedPanel;
import com.sun.labs.aura.music.wsitm.client.ui.bundles.VariaBundle;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.Oracles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public class PageHeaderWidget extends Swidget implements HasListeners {

    private enum SubHeaderPanels {
        CONFIG,
        NONE
    }

    private static VariaBundle varImgBundle =
            (VariaBundle) GWT.create(VariaBundle.class);

    private SubHeaderPanels currSubHeaderPanel = SubHeaderPanels.NONE;

    //private RoundedPanel roundedMainPanel;
    private Grid mainPanel;
    private TextBox txtbox;

    private ArrayList<MenuItem> menuItems;
    private MainMenu mm;
    private PlayButton playButton;

    private InstantRecPlayWidget instantRecPlayWidget;

    private SearchWidget search;
    private FlowPanel searchBoxContainerPanel;

    private FlowPanel configSubHeaderPanel;
    private ListBox listbox;

    private FlowPanel activeSubHeaderPanel;
    
    public PageHeaderWidget(ClientDataManager tCdm) {
        super("pageHeader",tCdm);
        menuItems = new ArrayList<MenuItem>();

        VerticalPanel vP = new VerticalPanel();
        vP.setWidth("100%");
        vP.setSpacing(0);
        vP.add(getMainWidget());

        activeSubHeaderPanel = new FlowPanel();
        activeSubHeaderPanel.setWidth("100%");
        activeSubHeaderPanel.setVisible(false);
        activeSubHeaderPanel.getElement().getStyle().setPropertyPx("marginLeft", 8);
        vP.add(activeSubHeaderPanel);

        // Create buttons
        HorizontalPanel hP = new HorizontalPanel();
        hP.setStyleName("pageConfigMargin");
        hP.add(createHeaderButton("Help", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.getCurrSwidget().displayHelp();
            }
        }));
        hP.add(createHeaderButton("Config", new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                setSubHeaderPanel(SubHeaderPanels.CONFIG);
            }
        }));
        vP.add(hP);

        createConfigSubHeaderPanel();
        
        initWidget(vP);

    }

    private RoundedPanel createHeaderButton(String title, ClickHandler cH) {
        Label configSub = new Label(title);
        configSub.addStyleName("pointer");
        configSub.addClickHandler(cH);

        HorizontalPanel hP = new HorizontalPanel();
        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        hP.setStyleName("pageConfigHeader");
        hP.setWidth("40px");
        hP.add(configSub);

        RoundedPanel rp = new RoundedPanel(hP, RoundedPanel.BOTTOM, 2);
        rp.setCornerStyleName("popupColors");
        rp.getElement().getStyle().setPropertyPx("marginRight", 6);
        return rp;
    }

    private void createConfigSubHeaderPanel() {
        
        HorizontalPanel hP = new HorizontalPanel();
        hP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        hP.setStyleName("pageConfigHeader");
        hP.addStyleName("somePadding");
        hP.setWidth("100%");

        Label simLbl = new Label("Similarity type : ");
        simLbl.setStyleName("headerMenuMed headerMenuMedC");
        hP.add(simLbl);

        listbox = new ListBox(false);
        invokeGetSimTypes();
        listbox.addItem("Loading...");
        hP.add(listbox);

        RoundedPanel rp = new RoundedPanel(hP, RoundedPanel.BOTTOM, 2);
        rp.setCornerStyleName("popupColors");

        configSubHeaderPanel = new FlowPanel();
        configSubHeaderPanel.setStyleName("subMenuMargin");
        configSubHeaderPanel.setWidth("225px");
        configSubHeaderPanel.add(rp);
    }

    /**
     * Display or hide the requested subheaderpanel
     * @param p
     */
    private void setSubHeaderPanel(SubHeaderPanels p) {
        // If we want to hide the panel
        if (p == currSubHeaderPanel || p == SubHeaderPanels.NONE) {
            activeSubHeaderPanel.setVisible(false);
            currSubHeaderPanel = SubHeaderPanels.NONE;
        } else {
            if (p == SubHeaderPanels.CONFIG) {
                activeSubHeaderPanel.clear();
                activeSubHeaderPanel.add(configSubHeaderPanel);
            }
            activeSubHeaderPanel.setVisible(true);
            currSubHeaderPanel = p;
        }
    }

    public Grid getMainWidget() {
        
        mainPanel = new Grid(1,4);

        // Set the logo
        populateMainPanel();

        // Set the section menu
        mm = new MainMenu();
        cdm.getLoginListenerManager().addListener(mm);
        mainPanel.setWidget(0, 1, mm);

        // Set the recommendation type toolbar
        HorizontalPanel hP = new HorizontalPanel();

        searchBoxContainerPanel = new FlowPanel();
        search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel);
        search.updateSuggestBox(Oracles.ARTIST);
        hP.add(search);

        mainPanel.setWidget(0, 2, hP);

        // Set right filler
        Image rightFill = new Image("header_right_fill.gif");
        rightFill.setWidth("26px");
        mainPanel.setWidget(0, 3, rightFill);

        // Set mainPanel visual properties
        mainPanel.setHeight("66px");
        mainPanel.setWidth("100%");
        mainPanel.setStyleName("pageHeader");
        mainPanel.setCellSpacing(0);

        mainPanel.getCellFormatter().setWidth(0, 0, "256px");
        // this should be * but makes ie blow up so just set a big % to fill the space
        mainPanel.getCellFormatter().setWidth(0, 1, "80%");
        mainPanel.getCellFormatter().setWidth(0, 2, "100px");
        mainPanel.getCellFormatter().setWidth(0, 3, "28px");

        mainPanel.getCellFormatter().getElement(0, 0).getStyle().setProperty("backgroundImage", "url(header_left.png)");
        mainPanel.getCellFormatter().getElement(0, 1).getStyle().setProperty("backgroundImage", "url(header_middle.png)");
        mainPanel.getCellFormatter().getElement(0, 2).getStyle().setProperty("backgroundImage", "url(header_middle.png)");
        mainPanel.getCellFormatter().getElement(0, 3).getStyle().setProperty("backgroundImage", "url(header_right.png)");

        mainPanel.getCellFormatter().setHorizontalAlignment(0, 1, HorizontalPanel.ALIGN_CENTER);
        mainPanel.getCellFormatter().setHorizontalAlignment(0, 2, HorizontalPanel.ALIGN_RIGHT);
        mainPanel.getCellFormatter().setVerticalAlignment(0, 1, VerticalPanel.ALIGN_MIDDLE);
        mainPanel.getCellFormatter().setVerticalAlignment(0, 2, VerticalPanel.ALIGN_MIDDLE);

        mainPanel.getElement().getStyle().setPropertyPx("marginBottom", 0);

        return mainPanel;
    }

    public void setMenuItems(ArrayList<MenuItem> mI) {
        this.menuItems = mI;
        mm.update();
    }

    public void updateSelectedMenuItem() {

    }

    private void populateMainPanel() {

        //mainPanel.setWidget(0,0, new Label("Please wait while we fetch your session information..."));
        // mainPanel.setWidget(0,0, new Label("The Music Explaura"));
        //Label title = new Label("The Music Explaura");
        Image title = new Image("header_left_fill.gif");
        title.setWidth("266px");
        title.setHeight("65px");
        title.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                History.newItem("searchHome:");
            }
        });
        //title.setStyleName("title");
        //title.addStyleName("titleC");
        title.addStyleName("pointer");
        mainPanel.setWidget(0, 0, title);
        //invokeGetUserSessionInfo();
    }

    public void requestRefreshSimTypes() {
        invokeGetSimTypes();
    }

    private void invokeGetSimTypes() {
        AsyncCallback<HashMap<String, String>> callback =
                new AsyncCallback<HashMap<String, String>>() {

            public void onFailure(Throwable arg0) {
                Popup.showErrorPopup(arg0, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve the similarity types.", Popup.ERROR_LVL.NORMAL, null);
            }

            public void onSuccess(HashMap<String, String> arg0) {
                cdm.setSimTypes(arg0);
                
                listbox.clear();
                String[] keyArray = cdm.getSimTypes().keySet().toArray(new String[0]);
                for (int i=keyArray.length-1; i>=0; i--) {
                    listbox.addItem(keyArray[i], keyArray[i]);
                }
                listbox.setSelectedIndex(0);
                cdm.setCurrSimTypeName(listbox.getItemText(0));
                listbox.addChangeHandler(new ChangeHandler() {
                    @Override
                    public void onChange(ChangeEvent event) {

                        String newSelectName = listbox.getItemText(listbox.getSelectedIndex());

                        // If the selection has changed
                        if (!cdm.getCurrSimTypeName().equals(newSelectName)) {
                            cdm.setCurrSimTypeName(newSelectName);

                            if (!cdm.getCurrArtistID().equals("")) {
                                cdm.displayWaitIconUpdatableWidgets();
                                invokeGetArtistInfo(cdm.getCurrArtistID());
                            }
                        }
                    }
                });
            }
        };

        try {
            musicServer.getSimTypes(callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve similarity types.", Popup.ERROR_LVL.NORMAL, new Command() {
                @Override
                public void execute() {
                    invokeGetSimTypes();
                }
            });
        }
    }

    private void fetchUserInfo() {
        Image.prefetch("ajax-ball.gif");
        mainPanel.clearCell(0, 0);
        HorizontalPanel h = new HorizontalPanel();
        h.setWidth("300px");
        h.add(new Image("ajax-ball.gif"));
        Label lbl = new Label("Connecting...");
        lbl.setStyleName("headerMenuMed headerMenuMedC");
        h.add(lbl);
        mainPanel.setWidget(0, 0, h);

        //
        // Login with local db if user is not using an openid
        if (txtbox.getText().startsWith("test-") || txtbox.getText().endsWith(".com") ||
                txtbox.getText().endsWith(".net") || txtbox.getText().endsWith(".org")) {
            // Run in deffered command to let the progress image load
            DeferredCommand.addCommand(new Command(){ public void execute() {
                Window.Location.assign("./Login?app-openid-auth=true&app-openid-name=" + txtbox.getText());
            }});
        } else {
            invokeGetUserSessionInfo(txtbox.getText());
        }
    }

    private void invokeTerminateSession() {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                Popup.showInformationPopup("You are now logged out. Have a nice and productive day.");
                populateLoginBox();
            }

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "close your session.", Popup.ERROR_LVL.NORMAL, new Command() {
                    @Override
                    public void execute() {
                        invokeTerminateSession();
                    }
                });
            }
        };

        try {
            cdm.resetUser();
            musicServer.terminateSession(callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "close your session.", Popup.ERROR_LVL.NORMAL, new Command() {
                @Override
                public void execute() {
                    invokeTerminateSession();
                }
            });
        }
    }

    /**
     * Called after a successful login by the invoke methods that just received
     * the new ListenerDetails containing the login information. Updates page header UI
     * @param l
     */
    private void updatePanelAfterLogin(ListenerDetails l) {

        if (l!=null && l.isLoggedIn()) {

            cdm.setListenerDetails(l);

            String name;
            if (l.getNickName() != null) {
                name = l.getNickName();
            } else if (l.getRealName() != null) {
                name = l.getRealName();
            } else {
                name = l.getOpenId();
            }

            HorizontalPanel hP = new HorizontalPanel();
            hP.setSpacing(4);
            Label loggedLbl = new Label(name);
            loggedLbl.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent ce) {
                    History.newItem("userpref:");
                }
            });
            loggedLbl.addStyleName("headerMenuMedItem headerMenuMedItemC");
            hP.add(loggedLbl);

            VerticalPanel vP = new VerticalPanel();

            Label lnk = new Label("Logout");
            lnk.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent ce) {
                    cdm.resetUser();
                    invokeTerminateSession();
                }
            });
            lnk.setStyleName("headerMenuTinyItem headerMenuTinyItemC");
            vP.add(lnk);

            hP.add(vP);

            HorizontalPanel buttonsPanel = new HorizontalPanel();
            buttonsPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            Image steerable = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.SMALL, new ClickHandler() {
                @Override
                public void onClick(ClickEvent ce) {
                    cdm.setSteerableReset(true);
                    History.newItem("steering:userCloud");
                }
            });
            steerable.setTitle("Steerable recommendations starting with your personal tag cloud");
            buttonsPanel.add(steerable);

            // Plays a random recommendation
            instantRecPlayWidget = new InstantRecPlayWidget();
            if (instantRecPlayWidget != null) {
                buttonsPanel.add(instantRecPlayWidget);
            }

            hP.add(buttonsPanel);
            mainPanel.setWidget(0, 0, hP);
        } else {
            populateLoginBox();
        }
    }

    /**
     * Get user info for a non openid user
     * @param userKey user key
     */
    private void invokeGetUserSessionInfo(final String userKey) {

        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                ListenerDetails l = (ListenerDetails) result;
                updatePanelAfterLogin(l);
            }

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve your session information.", Popup.ERROR_LVL.NORMAL, new DECommand<String>(userKey) {
                    @Override
                    public void execute() {
                        invokeGetUserSessionInfo(data);
                    }
                });
                populateLoginBox();
            }
        };

        try {
            musicServer.getNonOpenIdLogInDetails(userKey, callback);
        } catch (Exception ex) {
            populateLoginBox();
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve your session information.", Popup.ERROR_LVL.NORMAL, new DECommand<String>(userKey) {

                @Override
                public void execute() {
                    invokeGetUserSessionInfo(data);
                }
            });
            populateLoginBox();
        }
    }

    /**
     * Get user info for a potentially logged in user. This will log in a user
     * who has just entered his openid info after being redirected here from the
     * openid servlet
     */
    private void invokeGetUserSessionInfo() {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {
                if (result == null) {
                    Popup.showErrorPopup("", Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "retrieve your session information.", Popup.ERROR_LVL.NORMAL,
                            new Command() {
                        @Override
                        public void execute() {
                            invokeGetUserSessionInfo();
                        }
                    });
                    populateLoginBox();
                } else {
                    ListenerDetails l = (ListenerDetails) result;
                    updatePanelAfterLogin(l);
                }
            }

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve your session information.", Popup.ERROR_LVL.NORMAL, new Command() {
                    @Override
                    public void execute() {
                        invokeGetUserSessionInfo();
                    }
                });
            }
        };

        try {
            musicServer.getLogInDetails(callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve your session information.", Popup.ERROR_LVL.NORMAL, new Command() {
                @Override
                public void execute() {
                    invokeGetUserSessionInfo();
                }
            });
        }
    }

    private void populateLoginBox() {

        if (playButton != null) {
            playButton.onDelete();
        }

        txtbox = new TextBox();
        txtbox.setText(Cookies.getCookie("app-openid-uniqueid"));
        txtbox.setStyleName("openidField");
        txtbox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == 13) {
                    fetchUserInfo();
                }
            }
        });

        Button b = new Button();
        b.setText("Login with your openID");
        b.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                fetchUserInfo();
            }
        });

        HorizontalPanel h = new HorizontalPanel();
        h.add(txtbox);
        h.add(b);
        mainPanel.setWidget(0, 0, h);

    }

    /**
     * Fetch new similar artists. Used when similarity type is updated
     * @param artistID
     * @param refresh
     */
    private void invokeGetArtistInfo(String artistID) {

        if (artistID.startsWith("artist:")) {
            artistID = artistID.replaceAll("artist:", "");
        }

        AsyncCallback<ArrayList<ScoredC<ArtistCompact>>> callback =
                new AsyncCallback<ArrayList<ScoredC<ArtistCompact>>>() {

            public void onSuccess(ArrayList<ScoredC<ArtistCompact>> aC) {
                if (aC != null) {
                    cdm.updateUpdatableWidgets(aC);
                } else {
                    Popup.showErrorPopup("Returned list was null.",
                        Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve the new recommendations.", Popup.ERROR_LVL.NORMAL, null);
                }
            }

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve the new recommendations.", Popup.ERROR_LVL.NORMAL, null);
            }
        };

        try {
            musicServer.getSimilarArtists(artistID, cdm.getCurrSimTypeName(),
                    cdm.getCurrPopularity(), callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                "retrieve the new recommendations.", Popup.ERROR_LVL.NORMAL, null);
        }
    }

    @Override
    public ArrayList<String> getTokenHeaders() {
        return new ArrayList<String>();
    }

    @Override
    protected void initMenuItem() {
        // this does not have a menu
        menuItem = new MenuItem();
    }

    @Override
    public void doRemoveListeners() {
        mm.onDelete();
        if (playButton != null) {
            cdm.getMusicProviderSwitchListenerManager().removeListener(playButton);
        }
    }

    private class InstantRecPlayWidget extends Composite implements HasListeners {

        private ArrayList<ArtistCompact> aCList;
        private Grid g;
        private int currIndex = 0;

        public InstantRecPlayWidget() {

            ArtistCompact[] recs = cdm.getListenerDetails().getRecommendations();

            if (recs!=null && recs.length>0) {
                // Insert recommendations in list in random order
                aCList = new ArrayList<ArtistCompact>();
                int l = 0;
                for (ArtistCompact aC : recs) {
                    aCList.add(Random.nextInt(l++), aC);
                }

                g = new Grid(1,1);
                setNextRec(this);
                initWidget(g);
            } else {
                initWidget(new Label(""));
            }
        }

        private void setNextRec(InstantRecPlayWidget w) {

            if (currIndex>=aCList.size()) {
                g.setWidget(0, 0, new Label(""));
            } else {
                PlayButton pB = new PlayButton(cdm, aCList.get(currIndex++),
                        PlayButton.PLAY_ICON_SIZE.SMALL, musicServer);
                cdm.getMusicProviderSwitchListenerManager().addListener(pB);
                // Add click listener that will change recommendation on click
                pB.addClickListener(new DataEmbededClickListener<InstantRecPlayWidget>(w) {
                    @Override
                    public void onClick(Widget sender) {
                        data.setNextRec(data);
                    }
                });
                doRemoveListeners();
                g.setWidget(0, 0, pB);
            }
        }

        @Override
        public void doRemoveListeners() {
            Widget w = g.getWidget(0, 0);
            if (w instanceof PlayButton) {
                ((PlayButton)w).onDelete();
            }
        }

    }

    public class MainMenu extends Composite implements LoginListener {

        private Grid p;
        private boolean loggedIn = false;

        public MainMenu() {
            p = new Grid(1,1);
            update();
            initWidget(p);
        }

        @Override
        public Widget getWidget() {
            return p;
        }

        private void update() {
            HorizontalPanel hP = new HorizontalPanel();
            hP.setSpacing(8);

            if (menuItems !=null && menuItems.size()>0) {
                Collections.sort(menuItems, MenuItem.getOrderComparator());
                for (MenuItem mI : menuItems) {
                    if (!mI.mustBeLoggedIn() || (mI.mustBeLoggedIn() && loggedIn)) {
                        Label sLabel = new Label(mI.getName());
                        sLabel.addClickHandler(mI.getClickHandler());
                        sLabel.setStyleName("headerMenuMedItem headerMenuMedItemC");
                        mI.setLabel(sLabel);
                        hP.add(sLabel);
                    }
                }
            }
            p.setWidget(0, 0, hP);
        }

        @Override
        public void onLogin(ListenerDetails lD) {
            loggedIn = true;
            update();
        }

        @Override
        public void onLogout() {
            loggedIn = false;

            if (instantRecPlayWidget != null) {
                instantRecPlayWidget.doRemoveListeners();
            }

            update();
        }

        @Override
        public void onDelete() {
            cdm.getLoginListenerManager().removeListener(this);
        }
    }

    public class SearchWidget extends AbstractSearchWidget {

        private ListBox searchSelection;

        public SearchWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, FlowPanel searchBoxContainerPanel) {

            super(musicServer, cdm, searchBoxContainerPanel, Oracles.ARTIST, "pageHeaderSearchBox");

            searchBoxContainerPanel.add(WebLib.getSunLoaderWidget());

            searchSelection = new ListBox();
            searchSelection.addItem("For artist", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST.toString());
            searchSelection.addItem("By Tag", searchTypes.SEARCH_FOR_ARTIST_BY_TAG.toString());
            searchSelection.addItem("For Tag", searchTypes.SEARCH_FOR_TAG_BY_TAG.toString());
            searchSelection.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    if (getSearchType() == searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST) {
                        updateSuggestBox(Oracles.ARTIST);
                    } else {
                        updateSuggestBox(Oracles.TAG);
                    }
                }
            });

            //updateSuggestBox(Oracles.ARTIST);  -- done in constructor
            setText("", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);

            HorizontalPanel searchPanel = new HorizontalPanel();
            searchPanel.setStyleName("searchPanel");
            searchPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            searchPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);

            Image searchButton = varImgBundle.searchButton().createImage();
            searchButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent ce) {
                    search();
                }
            });

            searchPanel.add(searchBoxContainerPanel);
            searchPanel.add(searchSelection);
            searchPanel.add(searchButton);
            this.initWidget(searchPanel);
        }

        @Override
        public void search() {
            if (cdm.getCurrSimTypeName() == null || cdm.getCurrSimTypeName().equals("")) {
                Popup.showErrorPopup("", Popup.ERROR_MSG_PREFIX.NONE, 
                        "Error. Cannot search without the similarity types.", 
                        Popup.ERROR_LVL.NORMAL, null);
            } else {
                String query = getSearchBox().getText();
                if (!validateQuery(query)) {
                    return;
                }

                query = query.toLowerCase();
                searchTypes currST = getSearchType();
                if (currST == searchTypes.SEARCH_FOR_TAG_BY_TAG) {
                    History.newItem("tagSearch:"+query);
                } else if (currST == searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST) {
                    History.newItem("artistSearch:"+query);
                } else {
                    History.newItem("artistSearchByTag::"+query);
                }
            }
        }

        @Override
        protected searchTypes getSearchType() {
            int index = searchSelection.getSelectedIndex();
            if (index==0) {
                return searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST;
            } else if (index==1) {
                return searchTypes.SEARCH_FOR_ARTIST_BY_TAG;
            } else {
                return searchTypes.SEARCH_FOR_TAG_BY_TAG;
            }
        }

        @Override
        public void setSearchType(searchTypes searchType) {
            if (searchType == searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST) {
                searchSelection.setSelectedIndex(0);
            } else if (searchType == searchTypes.SEARCH_FOR_ARTIST_BY_TAG) {
                searchSelection.setSelectedIndex(1);
            } else {
                searchSelection.setSelectedIndex(2);
            }
        }
    }

}