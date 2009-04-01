/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.event.CommonTagsAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DDEClickHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.event.DEChangeHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.event.TagCloudListener;
import com.sun.labs.aura.music.wsitm.client.event.WebListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.items.RecsNTagsContainer;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuTagLabel;
import com.sun.labs.aura.music.wsitm.client.ui.HelpPopup.HELP_SECTIONS;
import com.sun.labs.aura.music.wsitm.client.ui.PerformanceTimer;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.bundles.VariaBundle;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.Oracles;
import com.sun.labs.aura.music.wsitm.client.ui.widget.DualRoundedPanel;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PopularitySelect;
import com.sun.labs.aura.music.wsitm.client.ui.widget.SwapableWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.ResizableTagWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagMeterWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidgetContainer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author mailletf
 */
public class SteeringSwidget extends Swidget {

    protected static VariaBundle varImgBundle = (VariaBundle) GWT.create(VariaBundle.class);

    private MainPanel mP;

    public SteeringSwidget(ClientDataManager cdm) {
        super("Steering", cdm);
        mP = new MainPanel();
        cdm.getLoginListenerManager().addListener(mP);
        initWidget(mP, true);
        cdm.setSteerableReset(true);
        update(History.getToken());
    }

    @Override
    public ArrayList<String> getTokenHeaders() {

        ArrayList<String> l = new ArrayList<String>();
        l.add("steering:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        menuItem = new MenuItem("Steering", MenuItem.getDefaultTokenClickHandler("steering:"), false, 1);
    }

    @Override
    public void update(String historyToken) {
        updateWindowTitle("Steerable recommendations");
        if (historyToken.startsWith("steering:")) {
            //
            // Only reset if artist id is in querystring and we aksed
            if (historyToken.length() > 9 && cdm.getSteerableReset()) {
                cdm.setSteerableReset(false);
                if (historyToken.startsWith("steering:userCloud") && cdm.isLoggedIn()) {
                    // Display all the user's tags in the right panel
                    mP.setUserTagPanel(true);
                    // Load the user's top tags in the steerable panel
                    mP.loadCloud(cdm.getListenerDetails().getUserTagCloud());
                // Load an artist's tags
                } else if (historyToken.startsWith("steering:art:")) {
                    mP.loadArtist(historyToken.substring(13));
                // Load tags passed by query string
                } else if (historyToken.startsWith("steering:cloud:")) {
                    mP.invokeLoadTagsFromQuery(historyToken.substring(15));
                } else {
                    mP.loadArtistCloud(historyToken.substring(9));
                }
            }
        }
    }

    @Override
    public void doRemoveListeners() {
        mP.doRemoveListeners();
        mP.onDelete();
    }

    /**
     * Display help when top header link is clicked
     */
    @Override
    public void displayHelp() {
        cdm.showHelp(HELP_SECTIONS.STEERING);
    }

    public class MainPanel extends Composite implements LoginListener, HasListeners {

        private dialogContainer savePanel;
        private dialogContainer loadPanel;

        private DockPanel dP;

        private Grid mainSearchTagPanel;
        private Grid mainUserTagPanel;
        private Grid mainTagPanel;
        private ItemInfoHierarchyWidget ihw; // personal tag cloud list in right panel

        private Grid mainArtistListPanel;
        private TagWidgetContainer tagLand;
        private SearchWidget search;
        private FlowPanel searchBoxContainerPanel;
        private HashMap<String, ScoredTag> currTagMap;
        private ArrayList<ScoredC<ArtistCompact>> currRecommendations = null;

        private PopularitySelect popSelect;
        
        private ListBox interfaceListbox;
        private String currLoadedTagWidget = "";

        /**
         * When a request for a recommendation update is made, if a call has 
         * already been made, wait for it to finish before making the next call
         */
        private boolean inRpc = false;
        private boolean updateRecRequest = false;

        public MainPanel() {
            dP = new DockPanel();

            // Left
            mainArtistListPanel = new Grid(1, 1);
            mainArtistListPanel.setWidth("300px");
            mainArtistListPanel.setWidget(0, 0, new Label("Add tags to your tag " +
                    "cloud to get recommendations"));

            HorizontalPanel hP = new HorizontalPanel();
            hP.setStyleName("h2");
            hP.setWidth("300px");
            hP.add(new SpannedLabel("Recommendations"));

            popSelect = new PopularitySelect() {

                @Override
                public void onSelectionChange(String newPopularity) {
                    invokeFetchNewRecommendations();
                }
            };
            hP.add(popSelect);
            
            Image viewTagInfluence = varImgBundle.loupe().createImage();
            viewTagInfluence.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    displayTagInfluence();
                }
            });
            hP.add(viewTagInfluence);

            dP.add(WebLib.createSection(hP, mainArtistListPanel), DockPanel.WEST);

            // Right (continued lower)
            mainSearchTagPanel = new Grid(2, 1);
            mainUserTagPanel = new Grid(1,1);

            mainTagPanel = new Grid(2,1);
            mainTagPanel.setWidth("185px");
            mainTagPanel.setWidget(1, 0, mainSearchTagPanel);

            // Create "add tag" menu
            FlowPanel fp = new FlowPanel();
            fp.setWidth("200px");
            fp.addStyleName("smallMenuBack");
            fp.getElement().getStyle().setProperty("textAlign", "center");
            SpannedLabel searchLabel = new SpannedLabel("Search");
            searchLabel.addStyleName("pointer");
            searchLabel.getElement().getStyle().setProperty("fontWeight", "bold");
            SpannedLabel userTagLabel = new SpannedLabel("Your tags");
            userTagLabel.addStyleName("pointer");

            searchLabel.addClickHandler(new DDEClickHandler<SpannedLabel, SpannedLabel>(searchLabel, userTagLabel) {
                @Override
                public void onClick(ClickEvent event) {
                    mainTagPanel.setWidget(1, 0, mainSearchTagPanel);
                    sndData.getElement().getStyle().setProperty("fontWeight", "normal");
                    data.getElement().getStyle().setProperty("fontWeight", "bold");
                }
            });            
            userTagLabel.addClickHandler(new DDEClickHandler<SpannedLabel, SpannedLabel>(searchLabel, userTagLabel) {
                @Override
                public void onClick(ClickEvent event) {
                    mainTagPanel.setWidget(1, 0, mainUserTagPanel);
                    sndData.getElement().getStyle().setProperty("fontWeight", "bold");
                    data.getElement().getStyle().setProperty("fontWeight", "normal");
                }
            });

            fp.add(searchLabel);
            fp.add(new SpannedLabel(" - "));
            fp.add(userTagLabel);

            mainTagPanel.setWidget(0, 0, fp);
            setUserTagPanel(cdm.isLoggedIn());

            dP.add(new DualRoundedPanel("Add tag", mainTagPanel), DockPanel.EAST);

            // North
            HorizontalPanel mainNorthMenuPanel = new HorizontalPanel();
            mainNorthMenuPanel.setWidth("100%");
            mainNorthMenuPanel.setSpacing(5);
            mainNorthMenuPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

            /*
             * Save and load tag cloud is not implemented yet
            Button saveButton = new Button("Save");
            saveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showSaveDialog();
                }
            });
            mainNorthMenuPanel.add(saveButton);

            Button loadButton = new Button("Load");
            loadButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showLoadDialog();
                }
            });
            mainNorthMenuPanel.add(loadButton);
             */

            Button resetButton = new Button("Erase all tags");
            resetButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    tagLand.removeAllItems(true);
                }
            });
            mainNorthMenuPanel.add(resetButton);

            Button viewCloudButton = new Button("View expanded cloud");
            viewCloudButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (currTagMap == null || currTagMap.isEmpty()) {
                        Popup.showInformationPopup("Cannot display expanded representation; you must " +
                                "add tags in your cloud first.");
                    } else {
                        HashMap<String, ScoredTag> map = currTagMap;
                        ItemInfo[] iI = new ItemInfo[map.size()];
                        int index = 0;
                        for (String s : map.keySet()) {
                            iI[index++] = new ItemInfo(ClientDataManager.nameToKey(s),
                                    s, map.get(s).getScore(), map.get(s).getScore());
                        }
                        TagDisplayLib.showTagCloud("Expanded representation of tag cloud",
                                iI, TagDisplayLib.ORDER.SHUFFLE, cdm);
                    }
                }
            });
            mainNorthMenuPanel.add(viewCloudButton);

            HorizontalPanel interfaceSelectPanel = new HorizontalPanel();
            Label interfaceLabel = new SpannedLabel("Interface: ");
            interfaceListbox = new ListBox(false);
            interfaceListbox.addItem("Cloud");
            interfaceListbox.addItem("Meter");
            interfaceListbox.addChangeHandler(new DEChangeHandler<ListBox>(interfaceListbox) {
                @Override
                public void onChange(ChangeEvent event) {
                    swapTagWidget(data.getItemText(data.getSelectedIndex()));
                }
            });
            interfaceSelectPanel.add(interfaceLabel);
            interfaceSelectPanel.add(interfaceListbox);
            mainNorthMenuPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
            mainNorthMenuPanel.add(interfaceSelectPanel);


            dP.add(mainNorthMenuPanel, DockPanel.NORTH);

            //
            // North 2
            tagLand = new TagWidgetContainer(this, cdm);
            tagLand.init(new ResizableTagWidget(this, cdm,
                    tagLand.getSharedCloudArtistMenu(), tagLand.getSharedCloudTagMenu()));
            currLoadedTagWidget = "Cloud";
            cdm.getSteerableTagCloudExternalController().setTagWidget(tagLand);
            dP.add(tagLand, DockPanel.NORTH);

            // Right again
            searchBoxContainerPanel = new FlowPanel();
            mainSearchTagPanel.setWidth("185px");
            mainSearchTagPanel.setWidget(1, 0, new Label("Search for tags to add using the above search box"));

            search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel, mainSearchTagPanel, tagLand);
            search.updateSuggestBox(Oracles.TAG);
            mainSearchTagPanel.setWidget(0, 0, search);

            initWidget(dP);
        }

        public void swapTagWidget(String widgetName) {
            if (!currLoadedTagWidget.equals(widgetName)) {
                if (widgetName.equals("Cloud")) {
                    tagLand.swapTagWidget(new ResizableTagWidget(this, cdm,
                            tagLand.getSharedCloudArtistMenu(), tagLand.getSharedCloudTagMenu()));
                    currLoadedTagWidget = "Cloud";
                } else {
                    tagLand.swapTagWidget(new TagMeterWidget(this, cdm));
                    currLoadedTagWidget = "Meter";
                }
            }
        }

        public void showLoadDialog() {

            if (loadPanel==null) {

                PopupPanel popup = Popup.getPopupPanel();

                loadPanel = new dialogContainer(new Label("Your saved tag clouds will soon be listed here."), popup);
                Popup.showRoundedPopup(loadPanel.getWidget(), "Load tag cloud", loadPanel.getPopupPanel(), 450);
            } else {
                loadPanel.getPopupPanel().show();
            }
            

        }

        public void showSaveDialog() {

            if (currTagMap==null || currTagMap.isEmpty()) {
                Popup.showInformationPopup("Sorry. You cannot save an empty tag cloud.");
            } else {

                if (savePanel==null) {

                    PopupPanel popup = Popup.getPopupPanel();

                    Grid g = new Grid(2,2);
                    g.setWidth("100%");
                    g.addStyleName("popupColors");
                    g.setWidget(0, 0, new Label("Tag cloud name : "));
                    TextBox tb = new TextBox();
                    g.setWidget(0, 1, tb);

                    g.setWidget(1, 0, new Label("Visibility : "));
                    ListBox visLb = new ListBox();
                    visLb.addItem("Public");
                    visLb.addItem("Private");
                    g.setWidget(1, 1, visLb);

                    VerticalPanel vP = new VerticalPanel();
                    vP.add(g);

                    Button b = new Button();
                    b.setText("Save");
                    b.addClickHandler(new DEClickHandler<PopupPanel>(popup) {
                        @Override
                        public void onClick(ClickEvent event) {
                            // save cloud
                            data.hide();
                        }
                    });
                    vP.add(b);

                    savePanel = new dialogContainer(vP, popup);
                    Popup.showRoundedPopup(savePanel.getWidget(), "Save tag cloud", savePanel.getPopupPanel(), 450);
                } else {
                    savePanel.getPopupPanel().show();
                }
            }
        }

        /**
         * Display popup of curr tags' influence on the current recommendations
         */
        private void displayTagInfluence() {

            if (currRecommendations == null) {
                Popup.showInformationPopup("Cannot display tag influence cloud; you must add tags in your cloud first.");
                return;
            }

            // Add every positive-valued tag in tag cloud to influence map so
            // that tags that have no influence will be shown as negative
            HashMap<String, Double> tagInfluenceMap = new HashMap<String, Double>();
            for (String s : currTagMap.keySet()) {
                if (currTagMap.get(s).getScore() > 0) {
                    tagInfluenceMap.put(s, -0.01);
                }
            }

            double maxScore = 0;
            double newVal = 0;
            for (ScoredC<ArtistCompact> aC : currRecommendations) {
                for (ItemInfo iI : aC.getItem().getDistinctiveTags()) {
                    // @todo remove lowercase when engine fixed
                    String name = iI.getItemName().toLowerCase();
                    if (currTagMap.containsKey(name)) {
                        newVal = tagInfluenceMap.get(name) + iI.getScore();
                        tagInfluenceMap.put(name, newVal);

                        if (newVal > maxScore) {
                            maxScore = newVal;
                        }
                    }
                }
            }

            ItemInfo[] tagArray = new ItemInfo[tagInfluenceMap.size()];
            int index = 0;
            for (String tagName : tagInfluenceMap.keySet()) {
                double val = tagInfluenceMap.get(tagName) / maxScore * currTagMap.get(tagName).getScore();
                tagArray[index++] = new ItemInfo(ClientDataManager.nameToKey(tagName), tagName, val, val);
            }
            TagDisplayLib.showTagCloud("Tags' influence on generated recommendations", tagArray, TagDisplayLib.ORDER.SHUFFLE, cdm);
        }

        public void displayNewRecommendations(ArrayList<ScoredC<ArtistCompact>> aCList) {
            // Remove listeners if we had an ArtistListWidget previously loaded
            if (mainArtistListPanel.getWidget(0, 0) != null &&
                    mainArtistListPanel.getWidget(0, 0) instanceof ArtistCloudArtistListWidget) {
                ((ArtistListWidget) mainArtistListPanel.getWidget(0, 0)).doRemoveListeners();
            }

            mainArtistListPanel.setWidget(0, 0,
                    new ArtistCloudArtistListWidget(musicServer, cdm, aCList, tagLand));
            
            hideLoader();

            if (aCList != null && aCList.size() > 0) {
                currRecommendations = aCList;
            } else {
                currRecommendations = null;
            }
        }

        public void invokeFetchNewRecommendations() {
            PerformanceTimer.start("newRecommendations");

            if (inRpc) {
                updateRecRequest = true;
                return;
            } else {
                inRpc = true;
                updateRecRequest = false;
            }

            AsyncCallback<ArrayList<ScoredC<ArtistCompact>>> callback = new AsyncCallback<ArrayList<ScoredC<ArtistCompact>>>() {

                public void onSuccess(ArrayList<ScoredC<ArtistCompact>> aCList) {
                    PerformanceTimer.stop("newRecommendationsGetData");
                    PerformanceTimer.start("newRecommendationsRedraw");

                    displayNewRecommendations(aCList);

                    inRpc = false;
                    if (updateRecRequest) {
                        invokeFetchNewRecommendations();
                    }

                    PerformanceTimer.stop("newRecommendationsRedraw");
                    PerformanceTimer.stop("newRecommendations");
                    WebLib.trackPageLoad("steerUpdate");
                }

                public void onFailure(Throwable caught) {
                    PerformanceTimer.stop("newRecommendationsGetData");
                    PerformanceTimer.stop("newRecommendations");

                    inRpc = false;
                    hideLoader();

                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "retrieve updated recommendations.", Popup.ERROR_LVL.NORMAL,
                            new Command() {
                                @Override
                                public void execute() {
                                    invokeFetchNewRecommendations();
                                }
                            });
                }
            };

            showLoader();

            try {
                currTagMap = tagLand.getTagMap();
                PerformanceTimer.start("newRecommendationsGetData");
                musicServer.getSteerableRecommendations(currTagMap, popSelect.getSelectedValue(), callback);
            } catch (Exception ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve updated recommendations.", Popup.ERROR_LVL.NORMAL,
                        new Command() {
                            @Override
                            public void execute() {
                                invokeFetchNewRecommendations();
                            }
                        });
            }
        }

        /**
         * Display the user's tags in the right panel
         * @param loggedIn is false, a message explaining to the user he must be logged in will be displayed
         */
        public void setUserTagPanel(boolean loggedIn) {

            // Remove listeners if set
            if (ihw!=null) {
                ihw.doRemoveListeners();
            }

            if (loggedIn) {
                ItemInfo[] userCloud = cdm.getListenerDetails().getUserTagCloud();
                if (userCloud!=null && userCloud.length>0) {
                    ihw = new ItemInfoHierarchyWidget(userCloud, tagLand);
                    mainUserTagPanel.setWidget(0, 0, ihw);
                } else {
                    Label msg = new Label("Your user cloud is empty");
                    msg.setStyleName("smallItalicExplanation");
                    mainUserTagPanel.setWidget(0, 0, msg);
                }
            } else {
                VerticalPanel vP = new VerticalPanel();
                vP.setStyleName("smallItalicExplanation");
                vP.add(new Label("Login to have your personal"));
                vP.add(new Label("tags listed here"));
                mainUserTagPanel.setWidget(0, 0, vP);
            }
        }

        @Override
        public void onLogin(ListenerDetails lD) {
            setUserTagPanel(true);
        }

        @Override
        public void onLogout() {
            setUserTagPanel(false);
        }

        public void invokeLoadTagsFromQuery(String query) {

            AsyncCallback<RecsNTagsContainer> callback =
                    new AsyncCallback<RecsNTagsContainer>() {

                public void onSuccess(RecsNTagsContainer r) {
                    if (r != null) {
                        tagLand.removeAllItems(false);
                        tagLand.addTags(r.tagMap);
                        displayNewRecommendations(r.recs);
                    } else {
                        Popup.showErrorPopup("Returned recommendations were null.", Popup.ERROR_MSG_PREFIX.NONE,
                                "An unknown error occured while loading your custom tag cloud.", Popup.ERROR_LVL.NORMAL, null);
                    }
                }

                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "load your custom tag cloud.", Popup.ERROR_LVL.NORMAL, null);
                }
            };

            showLoader();
            try {
                musicServer.getRecommendationsFromString(query, callback);
            } catch (Exception ex) {
                    Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "load your custom tag cloud.", Popup.ERROR_LVL.NORMAL, null);
            }

        }

        public void loadCloud(ItemInfo[] cloud) {
            tagLand.removeAllItems(true);
            if (cloud != null && cloud.length > 0) {
                tagLand.addTags(cloud, TagWidget.NBR_TOP_TAGS_TO_ADD);
            } else {
                Popup.showInformationPopup("Your user cloud is empty; no tags to add.");
            }
            History.newItem("steering:");
        }

        public void loadArtistCloud(String artistId) {
            tagLand.removeAllItems(false);
            if (artistId.startsWith("steering:")) {
                artistId = artistId.substring(artistId.indexOf(":") + 1);
            }
            invokeGetDistincitveTagsService(artistId);
            invokeGetArtistCompactService(artistId, new AsyncCallback<ArtistCompact>() {

                public void onSuccess(ArtistCompact aC) {
                    if (aC != null) {
                        search.displayArtist(new ItemInfo(aC.getId(), aC.getName(), aC.getNormPopularity(), aC.getNormPopularity()));
                    } else {
                        Popup.showErrorPopup("Returned object was null.",
                                Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                "retrieve the artist's tag cloud.", Popup.ERROR_LVL.NORMAL, null);
                    }
                }

                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve the artist's tag cloud.", Popup.ERROR_LVL.NORMAL, null);
                }
            });
        }

        public void loadArtist(String artistId) {
            tagLand.removeAllItems(false);
            if (artistId.startsWith("steering:art:")) {
                artistId = artistId.substring(artistId.indexOf(":") + 5);
            }
            invokeGetArtistCompactService(artistId, new AsyncCallback<ArtistCompact>() {

                public void onSuccess(ArtistCompact aC) {
                    if (aC != null) {
                        tagLand.addArtist(aC, 0);
                        search.displayArtist(new ItemInfo(aC.getId(), aC.getName(), aC.getNormPopularity(), aC.getNormPopularity()));
                    } else {
                        Popup.showErrorPopup("Returned object was null.",
                                Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                "retrieve the artist's details.", Popup.ERROR_LVL.NORMAL, null);
                    }
                }

                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve the artist's details.", Popup.ERROR_LVL.NORMAL, null);
                }
            });
        }

        private void invokeGetDistincitveTagsService(final String artistID) {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    ItemInfo[] results = (ItemInfo[]) result;
                    if (results != null) {
                        if (results.length == 0) {
                            Popup.showInformationPopup("No tags found for artist");
                        } else {
                            tagLand.addTags(results, TagWidget.NBR_TOP_TAGS_TO_ADD);
                        }
                    } else {
                        Popup.showErrorPopup("Returned tag list was null.", Popup.ERROR_MSG_PREFIX.NONE,
                                "An unknown error occured while loading the artist's tag cloud.", Popup.ERROR_LVL.NORMAL,
                                new DECommand<String>(artistID) {
                                    @Override
                                    public void execute() {
                                        invokeGetDistincitveTagsService(data);
                                    }
                                });
                    }
                }

                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "the artist's tag cloud.", Popup.ERROR_LVL.NORMAL,
                            new DECommand<String>(artistID) {
                                @Override
                                public void execute() {
                                    invokeGetDistincitveTagsService(data);
                                }
                            });
                }
            };

            try {
                musicServer.getDistinctiveTags(artistID, 30, callback);
            } catch (Exception ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "the artist's tag cloud.", Popup.ERROR_LVL.NORMAL,
                        new DECommand<String>(artistID) {
                            @Override
                            public void execute() {
                                invokeGetDistincitveTagsService(data);
                            }
                        });
            }
        }

        private void invokeGetArtistCompactService(String artistId, AsyncCallback<ArtistCompact> callback) {
            try {
                musicServer.getArtistCompact(artistId, callback);
            } catch (Exception ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "the artist's details.", Popup.ERROR_LVL.NORMAL,
                        new DDECommand<String, AsyncCallback<ArtistCompact>>(artistId, callback) {
                            @Override
                            public void execute() {
                                invokeGetArtistCompactService(data, sndData);
                            }
                        });

            }
        }

        public void onDelete() {
            cdm.getLoginListenerManager().removeListener(this);
        }

        public void doRemoveListeners() {
            onDelete();
            search.doRemoveListeners();
        }
    }

    /**
     * Class containing dialog dialogs
     */
    private class dialogContainer {

        private Widget w;
        private PopupPanel popup;

        public dialogContainer(Widget w, PopupPanel popup) {
            this.w = w;
            this.popup = popup;
        }

        public Widget getWidget() {
            return w;
        }

        public PopupPanel getPopupPanel() {
            return popup;
        }

    }

    private class ItemInfoHierarchyWidget extends Composite implements HasListeners {

        private Grid mainGrid;
        private ItemInfo[] mainItems;
        private ItemInfo[] subItems = null;
        private HasListeners listenerContainer;
        private TagWidget tagLand;

        public ItemInfoHierarchyWidget(ItemInfo[] iI, TagWidget tagLand) {
            mainItems = iI;
            this.tagLand = tagLand;

            mainGrid = new Grid(2, 1);
            if (mainItems.length > 1) {
                displayMainItems();
            } else {
                displayDetails(mainItems[0], false);
            }

            initWidget(mainGrid);
        }

        /**
         * Called when user clicks on one of the main items. Will display the
         * details for this item along with a header allowing the user to go back
         * to the main item list
         * @param iI
         */
        public void displayDetails(ItemInfo iI, boolean showBackButton) {

            Grid g = new Grid(2,1);
            g.getCellFormatter().setHeight(0, 0, "30px");
            g.getCellFormatter().setHorizontalAlignment(1, 0, HorizontalPanel.ALIGN_CENTER);
            g.setWidget(1, 0, WebLib.getSunLoaderWidget(false));
            mainGrid.setWidget(1, 0, g);
            invokeGetDistincitveTagsService(iI.getId());

            VerticalPanel vP = new VerticalPanel();
            vP.setStyleName("pageHeader pageHeaderBackground");
            vP.setWidth("185px");

            //
            // Add buttons
            HorizontalPanel smallMenuPanel = new HorizontalPanel();
            smallMenuPanel.setSpacing(4);
            if (showBackButton) {
                Label backButton = new Label("Back");
                backButton.setStyleName("headerMenuTinyItem headerMenuTinyItemC");
                backButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        displayMainItems();
                    }
                });
                smallMenuPanel.add(backButton);
                smallMenuPanel.add(new SpannedLabel("   "));
            }

            Label addAllButton = new Label("Add top tags");
            addAllButton.setStyleName("headerMenuTinyItem headerMenuTinyItemC");
            addAllButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                    if (subItems != null) {
                        tagLand.addTags(subItems, TagWidget.NBR_TOP_TAGS_TO_ADD);
                    } else {
                        Popup.showInformationPopup("Add all tags - subitems is null");
                    }
                }
            });
            smallMenuPanel.add(addAllButton);
            smallMenuPanel.add(new SpannedLabel("   "));

            Label addArtistButton = new Label("Add artist");
            addArtistButton.setStyleName("headerMenuTinyItem headerMenuTinyItemC");
            addArtistButton.addClickHandler(new DDEClickHandler<Label, String>(addArtistButton, iI.getId()) {
                @Override
                public void onClick(ClickEvent event) {
                
                    data.setText("Processing...");
                    invokeGetArtistCompactService(sndData, new DEAsyncCallback<Label, ArtistCompact>(data) {

                        @Override
                        public void onSuccess(ArtistCompact aC) {
                            if (aC != null) {
                                tagLand.addArtist(aC, 0);
                            } else {
                                Popup.showErrorPopup("Returned object was null.",
                                        Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                        "retrieve the artist's details.", Popup.ERROR_LVL.NORMAL, null);
                            }
                            data.setText("Add artist");
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                "retrieve the artist's details.", Popup.ERROR_LVL.NORMAL, null);
                        }
                    });
                }
            });
            smallMenuPanel.add(addArtistButton);

            vP.add(smallMenuPanel);

            //
            // Add title
            FlowPanel titlePanel = new FlowPanel();
            SpannedLabel title = new SpannedLabel("Tags for ");
            title.addStyleName("titleC");
            title.getElement().getStyle().setPropertyPx("marginRight", 2);
            SpannedLabel titleName = new SpannedLabel(iI.getItemName());
            titleName.addStyleName("tagPop2");
            titlePanel.add(title);
            titlePanel.add(titleName);
            vP.add(titlePanel);
            RoundedPanel rP = new RoundedPanel(vP);
            rP.setCornerStyleName("pageHeaderBackground");
            mainGrid.setWidget(0, 0, rP);
        }

        public void displayMainItems() {
            PopSortedMultiWordSuggestOracle uS = cdm.getArtistOracle();
            subItems = null;
            VerticalPanel vP = new VerticalPanel();

            Label explanation = new Label("Click on an artist's name to display its most distinctive tags");
            explanation.setStyleName("smallItalicExplanation");
            explanation.getElement().setAttribute("style", "margin-bottom: 5px");
            vP.add(explanation);

            for (ItemInfo item : mainItems) {
                uS.add(item.getItemName(), item.getPopularity());
                HorizontalPanel hP = new HorizontalPanel();

                Label addButton = new Label("Add");
                SwapableWidget sW = new SwapableWidget(addButton, WebLib.getSunLoaderWidget(false));
                addButton.setStyleName("recoTags");
                addButton.addStyleName("pointer");
                addButton.getElement().setAttribute("style", "margin-right: 5px");
                addButton.addClickHandler(new DDEClickHandler<String, SwapableWidget>(item.getId(), sW) {
                    @Override
                    public void onClick(ClickEvent event) {

                        sndData.showWidget(SwapableWidget.LoadableWidget.W2);

                        invokeGetArtistCompactService(data, new DEAsyncCallback<SwapableWidget, ArtistCompact>(sndData) {

                            public void onSuccess(ArtistCompact aC) {
                                tagLand.addArtist(aC, 0);
                                data.showWidget(SwapableWidget.LoadableWidget.W1);
                            }

                            public void onFailure(Throwable caught) {
                                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                    "the artist's details.", Popup.ERROR_LVL.NORMAL, null);
                            }
                        });
                    }
                });
                hP.add(sW);

                Label itemName = new Label(item.getItemName());
                itemName.setStyleName("pointer");
                itemName.addClickHandler(new DEClickHandler<ItemInfo>(item) {
                        @Override
                        public void onClick(ClickEvent event) {

                        displayDetails(data, true);
                    }
                });
                hP.add(itemName);
                vP.add(hP);
            }
            mainGrid.setWidget(0, 0, vP);
            mainGrid.setWidget(1, 0, new Label(""));
        }

        private void invokeGetArtistCompactService(String artistID, AsyncCallback callback) {
            try {
                musicServer.getArtistCompact(artistID, callback);
            } catch (Exception ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "retrieve the artist's details.", Popup.ERROR_LVL.NORMAL,
                        new DDECommand<String, AsyncCallback>(artistID, callback) {
                            @Override
                            public void execute() {
                                invokeGetArtistCompactService(data, sndData);
                            }
                        });
            }
        }

        private void invokeGetDistincitveTagsService(final String artistID) {

            AsyncCallback<ItemInfo[]> callback = new AsyncCallback<ItemInfo[]>() {

                public void onSuccess(ItemInfo[] results) {
                    doRemoveListeners();
                    if (results != null) {
                        if (results.length == 0) {
                            mainGrid.setWidget(1, 0, new Label("No tags found"));
                        } else {

                            // Add tags to oracle
                            PopSortedMultiWordSuggestOracle uS = cdm.getTagOracle();
                            for (ItemInfo iI : results) {
                                uS.add(iI.getItemName(), iI.getPopularity());
                            }

                            subItems = results;
                            SortableItemInfoList sIIL = new SortableItemInfoList(results) {

                                protected void onItemClick(ItemInfo i) {
                                    tagLand.addTag(i, 0, true);
                                }
                            };
                            listenerContainer = sIIL;
                            mainGrid.getCellFormatter().setHorizontalAlignment(1, 0, HorizontalPanel.ALIGN_CENTER);
                            mainGrid.setWidget(1, 0, sIIL);
                        }
                    } else {
                        mainGrid.setWidget(1, 0, new Label("An unknown error occured."));
                    }
                }

                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                            "retrieve the artist's tags.", Popup.ERROR_LVL.NORMAL, new DECommand<String>(artistID) {
                        @Override
                        public void execute() {
                            invokeGetDistincitveTagsService(data);
                        }
                    });
                }
            };

            try {
                musicServer.getDistinctiveTags(artistID, 30, callback);
            } catch (Exception ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve the artist's tags.", Popup.ERROR_LVL.NORMAL, null);

            }
        }

        @Override
        public void doRemoveListeners() {
            if (listenerContainer != null) {
                listenerContainer.doRemoveListeners();
            }
            listenerContainer = null;
        }
    }

    private abstract class SortableItemInfoList extends Composite implements HasListeners {

        private Grid mainPanel;
        private ArrayList<ItemInfo> iI;
        private LinkedList<WebListener> webListeners;
        private double maxValue = 0;

        public SortableItemInfoList(ItemInfo[] iI) {

            webListeners = new LinkedList<WebListener>();
            this.iI = ItemInfo.arrayToList(iI);

            mainPanel = new Grid(iI.length + 1, 2);
            mainPanel.getColumnFormatter().setWidth(0, "100px");

            //
            // Add the title line
            Label nameLbl = new Label("Name");
            nameLbl.addStyleName("pointer");
            nameLbl.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ((Label) mainPanel.getWidget(0, 0)).setText("Name*");
                    ((Label) mainPanel.getWidget(0, 1)).setText("Popularity");
                    populateMainPanel(ItemInfo.getNameSorter());
                }
            });
            mainPanel.setWidget(0, 0, nameLbl);

            Label popLbl = new Label("Popularity*");
            popLbl.addStyleName("pointer");
            popLbl.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ((Label) mainPanel.getWidget(0, 0)).setText("Name");
                    ((Label) mainPanel.getWidget(0, 1)).setText("Popularity*");
                    populateMainPanel(ItemInfo.getPopularitySorter());
                }
            });
            mainPanel.setWidget(0, 1, popLbl);

            populateMainPanel(ItemInfo.getPopularitySorter());
            initWidget(mainPanel);
        }

        private void populateMainPanel(Comparator<ItemInfo> c) {

            // Add all the items
            Collections.sort(iI, c);
            int lineIndex = 1;
            // Normalise by this result set's biggest value. The first time this function
            // is called, it will be with a popularity sorter giving us the maxValue
            if (maxValue == 0) {
                maxValue = iI.get(0).getPopularity();
            }
            for (ItemInfo i : iI) {
                TagCloudListeningTag tagLbl = new TagCloudListeningTag(i);
                tagLbl.addClickHandler(new DEClickHandler<ItemInfo>(i) {
                    @Override
                    public void onClick(ClickEvent event) {
                        onItemClick(data);
                    }
                });
                tagLbl.setStyleName("smallTagClick");
                if (cdm.getSteerableTagCloudExternalController().containsItem(i.getId())) {
                    tagLbl.addStyleName("tagColorAdded");
                }
                cdm.getTagCloudListenerManager().addListener(i.getId(), tagLbl);
                webListeners.add(tagLbl);
                mainPanel.setWidget(lineIndex, 0, tagLbl);
                mainPanel.setWidget(lineIndex, 1, WebLib.getSmallPopularityWidget(i.getPopularity() / maxValue, 75, true, false));
                lineIndex++;
            }
        }

        public void doRemoveListeners() {
            for (WebListener wL : webListeners) {
                wL.onDelete();
            }
            webListeners.clear();
        }

        protected abstract void onItemClick(ItemInfo i);

        private class TagCloudListeningTag extends ContextMenuTagLabel implements TagCloudListener {

            public TagCloudListeningTag(ItemInfo tag) {
                super(tag, cdm);
            }

            public void onTagAdd(String tagId) {
                addStyleName("tagColorAdded");
            }

            public void onTagDelete(String tagId) {
                removeStyleName("tagColorAdded");
            }

            public void onDelete() {
                cdm.getTagCloudListenerManager().removeListener(tag.getId(), this);
            }

            public void onTagDeleteAll() {
                onTagDelete("");
            }
        }
    }

    private class SearchWidget extends AbstractSearchWidget implements HasListeners {

        private Grid mainTagPanel;
        private TagWidget tagLand;
        private HasListeners listenersContainer;
        private SearchTypeRadioButton[] searchButtons;

        public SearchWidget(MusicSearchInterfaceAsync musicServer,
                ClientDataManager cdm, Panel searchBoxContainerPanel, Grid mainTagPanel,
                TagWidget tagLand) {

            super(musicServer, cdm, searchBoxContainerPanel, Oracles.TAG, "pageHeaderSearchBox");

            this.mainTagPanel = mainTagPanel;
            this.tagLand = tagLand;

            searchBoxContainerPanel.add(WebLib.getSunLoaderWidget(false));

            HorizontalPanel searchType = new HorizontalPanel();
            searchType.setSpacing(5);
            searchButtons = new SearchTypeRadioButton[2];
            searchButtons[0] = new SearchTypeRadioButton("searchType", "For Tag", searchTypes.SEARCH_FOR_TAG_BY_TAG);
            searchButtons[1] = new SearchTypeRadioButton("searchType", "By Artist", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);
            searchButtons[0].setValue(true);

            searchButtons[1].addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateSuggestBox(Oracles.ARTIST);
                }
            });
            searchButtons[0].addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateSuggestBox(Oracles.TAG);
                }
            });

            //updateSuggestBox(Oracles.TAG); -- done in constructor
            setText("", searchTypes.SEARCH_FOR_TAG_BY_TAG);
            setSuggestBoxWidth(180);

            for (int i = 0; i < searchButtons.length; i++) {
                searchType.add(searchButtons[i]);
                searchButtons[i].getElement().setAttribute("style", "font-size: 12px");
            }
            searchType.setWidth("100%");
            searchType.setStyleName("searchPanel");

            VerticalPanel searchPanel = new VerticalPanel();
            searchPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

            VerticalPanel leftP = new VerticalPanel();
            leftP.setHeight("100%");
            leftP.setWidth("100%");
            leftP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
            leftP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            leftP.add(searchBoxContainerPanel);
            searchPanel.add(leftP);
            searchPanel.add(searchType);
            this.initWidget(searchPanel);
            this.setWidth("185px");
        }

        @Override
        public void search() {
            String query = getSearchBox().getText();
            if (!validateQuery(query)) {
                return;
            }
            
            mainTagPanel.setWidget(1, 0, WebLib.getSunLoaderWidget(false));
            if (getCurrLoadedOracle() == Oracles.TAG) {
                invokeTagSearchService(query.toLowerCase());
            } else {
                invokeArtistSearchService(query.toLowerCase());
            }
        }

        /**
         * Display the supplied artist's distincitve tags
         * @param a
         */
        public void displayArtist(ItemInfo a) {
            ItemInfo[] aA = new ItemInfo[1];
            aA[0] = a;

            doRemoveListeners();
            ItemInfoHierarchyWidget iihw = new ItemInfoHierarchyWidget(aA, tagLand);
            listenersContainer = iihw;
            mainTagPanel.setWidget(1, 0, iihw);
        }

        private void invokeArtistSearchService(String searchText) {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    SearchResults sr = (SearchResults) result;
                    if (sr != null && sr.isOK()) {
                        ItemInfo[] results = sr.getItemResults(null);
                        doRemoveListeners();
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No match for " + sr.getQuery()));
                        } else {
                            ItemInfoHierarchyWidget iihw = new ItemInfoHierarchyWidget(results, tagLand);
                            listenersContainer = iihw;
                            mainTagPanel.setWidget(1, 0, iihw);
                        }
                    } else {
                        if (sr == null) {
                            Popup.showErrorPopup("Resultset is null. There were " +
                                    "probably no artists found.", Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                "perform the artist search.", Popup.ERROR_LVL.NORMAL, null);
                        } else {
                            Popup.showErrorPopup(sr.getStatus(), Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                "perform the artist search.", Popup.ERROR_LVL.NORMAL, null);
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "perform artist search.", Popup.ERROR_LVL.NORMAL, null);
                }
            };

            try {
                musicServer.artistSearch(searchText, 10, callback);
            } catch (Exception ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "perform artist search.", Popup.ERROR_LVL.NORMAL, null);
            }
        }

        private void invokeTagSearchService(String searchText) {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    SearchResults sr = (SearchResults) result;
                    if (sr != null && sr.isOK()) {
                        ItemInfo[] results = sr.getItemResults(null);
                        doRemoveListeners();
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No Match for " + sr.getQuery()));
                        } else {
                            SortableItemInfoList siil = new SortableItemInfoList(results) {
                                @Override
                                protected void onItemClick(ItemInfo i) {
                                    tagLand.addTag(i, 0, true);
                                }
                            };
                            listenersContainer = siil;
                            mainTagPanel.setWidget(1, 0, siil);
                        }
                    } else {
                        if (sr == null) {
                            Popup.showErrorPopup("Resultset is null. There were probably no tags found.",
                                    Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                    "perform tag search.", Popup.ERROR_LVL.NORMAL, null);
                        } else {
                            Popup.showErrorPopup(sr.getStatus(),
                                    Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                                    "perform tag search.", Popup.ERROR_LVL.NORMAL, null);
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "perform tag search.", Popup.ERROR_LVL.NORMAL, null);
                }
            };

            mainTagPanel.setWidget(1, 0, WebLib.getSunLoaderWidget(false));

            try {
                musicServer.tagSearch(searchText, 100, callback);
            } catch (Exception ex) {
                Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "perform tag search.", Popup.ERROR_LVL.NORMAL, null);
            }
        }

        @Override
        public void doRemoveListeners() {
            if (listenersContainer != null) {
                listenersContainer.doRemoveListeners();
                listenersContainer = null;
            }
        }

        @Override
        protected searchTypes getSearchType() {
            for (SearchTypeRadioButton rB : searchButtons) {
                if (rB.isChecked()) {
                    return rB.getSearchType();
                }
            }
            return null;
        }

        @Override
        public void setSearchType(searchTypes searchType) {
            for (SearchTypeRadioButton rB : searchButtons) {
                rB.setChecked(rB.getSearchType() == searchType);
            }
        }
    }

    private class ArtistCloudArtistListWidget extends ArtistListWidget {

        private TagWidget tagLand;

        public ArtistCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
                ClientDataManager cdm, ArrayList<ScoredC<ArtistCompact>> aCList, TagWidget tagLand) {

            super(musicServer, cdm, aCList, cdm.isLoggedIn(), false);
            this.tagLand = tagLand;
        }
        
        public ArtistCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
                ClientDataManager cdm, ArtistCompact[] aDArray, TagWidget tagLand) {

            super(musicServer, cdm, aDArray, cdm.isLoggedIn(), false);
            this.tagLand = tagLand;
        }

        @Override
        public void onTagClick(ItemInfo tag) {
            tagLand.addTag(tag, 0, true);
        }

        @Override
        public void openWhyPopup(SwapableTxtButton why) {
            why.showLoad();
            TagDisplayLib.invokeGetCommonTags(tagLand.getTagMap(), why.getId(),
                    musicServer, cdm,
                    new CommonTagsAsyncCallback(why, "Common tags between your cloud and " + why.getName(), cdm) {
                    });
        }

        @Override
        public void openDiffPopup(DiffButton diff) {
            ItemInfo[] steerTags = ItemInfo.mapToArray(tagLand.getTagMap());
            TagDisplayLib.showDifferenceCloud("Difference cloud between your tag cloud and " + diff.getName(),
                    steerTags, diff.getDistinctiveTags(), cdm);

        }
    }

}