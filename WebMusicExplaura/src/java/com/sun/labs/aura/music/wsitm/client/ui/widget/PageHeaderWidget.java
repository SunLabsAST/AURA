/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

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
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
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
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.Oracles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author mailletf
 */
public class PageHeaderWidget extends Swidget implements HasListeners {

    private enum SubHeaderPanels {
        CONFIG,
        NONE
    }
    private SubHeaderPanels currSubHeaderPanel = SubHeaderPanels.NONE;

    private RoundedPanel roundedMainPanel;
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
    
    public PageHeaderWidget(ClientDataManager cdm) {
        super("pageHeader",cdm);
        this.cdm = cdm;
        menuItems = new ArrayList<MenuItem>();

        VerticalPanel vP = new VerticalPanel();
        vP.setWidth("100%");

        Label title = new Label("Search Inside the Music - The Music Explaura");
        title.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                History.newItem("searchHome:");
            }
        });
        title.setStyleName("title");
        title.addStyleName("titleC");
        vP.add(title);
        vP.add(getMainWidget());

        activeSubHeaderPanel = new FlowPanel();
        activeSubHeaderPanel.setWidth("100%");
        activeSubHeaderPanel.setVisible(false);
        vP.add(activeSubHeaderPanel);


        Label l = new Label("Config");
        l.addStyleName("pointer");
        l.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                setSubHeaderPanel(SubHeaderPanels.CONFIG);
            }
        });
        HorizontalPanel hP = new HorizontalPanel();
        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        hP.setStyleName("pageConfigHeader");
        hP.setWidth("100%");
        hP.add(l);

        RoundedPanel rp = new RoundedPanel(hP, RoundedPanel.BOTTOM, 2);
        rp.setCornerStyleName("popupColors");

        FlowPanel fP = new FlowPanel();
        fP.setStyleName("pageConfigMargin");
        fP.setWidth("40px");
        fP.add(rp);
        
        vP.add(fP);

        createConfigSubHeaderPanel();
        
        initWidget(vP);

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

    public RoundedPanel getMainWidget() {
        
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

        searchBoxContainerPanel = new FlowPanel();
        search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel);
        search.updateSuggestBox(Oracles.ARTIST);
        hP.add(search);        
        
        mainPanel.setWidget(0, 2, hP);
        mainPanel.getCellFormatter().getElement(0, 2).setAttribute("align", "right");

        //
        // Set the section menu
        mm = new MainMenu();
        cdm.getLoginListenerManager().addListener(mm);
        mainPanel.setWidget(0, 1, mm);

        populateMainPanel();
     
        roundedMainPanel = new RoundedPanel(mainPanel);
        roundedMainPanel.setCornerStyleName("pageHeaderBackground");
        return roundedMainPanel;
        
    }

    public void setMenuItems(ArrayList<MenuItem> mI) {
        this.menuItems = mI;
        mm.update();
    }

    public void updateSelectedMenuItem() {

    }

    private void populateMainPanel() {

        mainPanel.setWidget(0,0, new Label("Please wait while we fetch your session information..."));
        invokeGetUserSessionInfo();

    }

    private void invokeGetSimTypes() {
        AsyncCallback<HashMap<String, String>> callback =
                new AsyncCallback<HashMap<String, String>>() {

            public void onFailure(Throwable arg0) {
                Window.alert("Error fetching similarity types.");
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
                listbox.addChangeListener(new ChangeListener() {

                    public void onChange(Widget arg0) {

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
                // do some UI stuff to show success
                Popup.showInformationPopup("You are now logged out. Have a nice and productive day.");
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
            loggedLbl.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    History.newItem("userpref:");
                }
            });
            loggedLbl.addStyleName("headerMenuMedItem headerMenuMedItemC");
            hP.add(loggedLbl);

            VerticalPanel vP = new VerticalPanel();

            Label lnk = new Label("Logout");
            lnk.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    cdm.resetUser();
                    invokeTerminateSession();
                }
            });
            lnk.setStyleName("headerMenuTinyItem headerMenuTinyItemC");
            vP.add(lnk);

            hP.add(vP);

            HorizontalPanel buttonsPanel = new HorizontalPanel();
            buttonsPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            Image steerable = new SteeringWheelWidget(SteeringWheelWidget.wheelSize.SMALL, new ClickListener() {

                public void onClick(Widget arg0) {
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
    private void invokeGetUserSessionInfo(String userKey) {
        AsyncCallback callback = new AsyncCallback() {

            public void onSuccess(Object result) {

                ListenerDetails l = (ListenerDetails) result;
                updatePanelAfterLogin(l);
            }

            public void onFailure(Throwable caught) {
                Window.alert(caught.toString());
                populateLoginBox();
            }
        };

        try {
            musicServer.getNonOpenIdLogInDetails(userKey, callback);
        } catch (Exception ex) {
            populateLoginBox();
            Window.alert(ex.getMessage());
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
                    Window.alert("Error fetching listener information");
                    populateLoginBox();
                } else {
                    ListenerDetails l = (ListenerDetails) result;
                    updatePanelAfterLogin(l);
                }
            }

            public void onFailure(Throwable caught) {
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

        if (playButton != null) {
            playButton.onDelete();
        }

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
     * Fetch new similar artists. Used when similarity type is updated
     * @param artistID
     * @param refresh
     */
    private void invokeGetArtistInfo(String artistID) {

        if (artistID.startsWith("artist:")) {
            artistID = artistID.replaceAll("artist:", "");
        }

        AsyncCallback<ArrayList<ScoredC<ArtistCompact>>> callback = new AsyncCallback<ArrayList<ScoredC<ArtistCompact>>>() {

            public void onSuccess(ArrayList<ScoredC<ArtistCompact>> aC) {
                // do some UI stuff to show success
                if (aC != null) {
                    cdm.updateUpdatableWidgets(aC);
                } else {
                    Window.alert("An error occured while fetching the new recommendations.");
                }
            }

            public void onFailure(Throwable caught) {
                Window.alert("An error occured while fetching the new recommendations.");
            }
        };

        try {
            musicServer.getSimilarArtists(artistID, cdm.getCurrSimTypeName(), cdm.getCurrPopularity(), callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    public ArrayList<String> getTokenHeaders() {
        return new ArrayList<String>();
    }

    protected void initMenuItem() {
        // this does not have a menu
        menuItem = new MenuItem();
    }

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

            // Insert recommendations in list in random order
            aCList = new ArrayList<ArtistCompact>();
            int l = 0;
            for (ArtistCompact aC : cdm.getListenerDetails().getRecommendations()) {
                aCList.add(Random.nextInt(l++), aC);
            }

            g = new Grid(1,1);
            setNextRec(this);
            initWidget(g);
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

                    public void onClick(Widget sender) {
                        data.setNextRec(data);
                    }
                });
                doRemoveListeners();
                g.setWidget(0, 0, pB);
            }
        }

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
                        sLabel.addClickListener(mI.getClickListener());
                        sLabel.setStyleName("headerMenuMedItem headerMenuMedItemC");
                        mI.setLabel(sLabel);
                        hP.add(sLabel);
                    }
                }
            }
            p.setWidget(0, 0, hP);
        }

        public void onLogin(ListenerDetails lD) {
            loggedIn = true;
            update();
        }

        public void onLogout() {
            loggedIn = false;

            if (instantRecPlayWidget != null) {
                instantRecPlayWidget.doRemoveListeners();
            }

            update();
        }

        public void onDelete() {
            cdm.getLoginListenerManager().removeListener(this);
        }
    }

    public class SearchWidget extends AbstractSearchWidget {

        private ListBox searchSelection;

        public SearchWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, FlowPanel searchBoxContainerPanel) {

            super(musicServer, cdm, searchBoxContainerPanel, Oracles.ARTIST);

            searchBoxContainerPanel.add(WebLib.getLoadingBarWidget());

            searchSelection = new ListBox();
            searchSelection.addItem("For artist", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST.toString());
            searchSelection.addItem("By Tag", searchTypes.SEARCH_FOR_ARTIST_BY_TAG.toString());
            searchSelection.addItem("For Tag", searchTypes.SEARCH_FOR_TAG_BY_TAG.toString());

            searchSelection.addChangeListener(new ChangeListener() {

                public void onChange(Widget sender) {
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

            Button searchButton = new Button("Search", new ClickListener() {
                public void onClick(Widget sender) {
                    search();
                }
            });
            searchButton.setTabIndex(2);

            searchPanel.add(searchBoxContainerPanel);
            searchPanel.add(searchSelection);
            searchPanel.add(searchButton);
            this.initWidget(searchPanel);
        }

        public void search() {
            if (cdm.getCurrSimTypeName() == null || cdm.getCurrSimTypeName().equals("")) {
                Window.alert("Error. Cannot search without the similarity types.");
            } else {
                String query = getSearchBox().getText().toLowerCase();
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