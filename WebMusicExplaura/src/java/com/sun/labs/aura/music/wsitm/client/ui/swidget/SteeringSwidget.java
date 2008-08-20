/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededChangeListener;
import com.sun.labs.aura.music.wsitm.client.event.CommonTagsAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.ui.widget.ArtistListWidget;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.HasListeners;
import com.sun.labs.aura.music.wsitm.client.event.TagCloudListener;
import com.sun.labs.aura.music.wsitm.client.event.WebListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenuTagLabel;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.Oracles;
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
import java.util.List;

/**
 *
 * @author mailletf
 */
public class SteeringSwidget extends Swidget implements HistoryListener {

    private MainPanel mP;

    public SteeringSwidget(ClientDataManager cdm) {
        super("Steering", cdm);
        History.addHistoryListener(this);
        mP = new MainPanel();
        cdm.getLoginListenerManager().addListener(mP);
        initWidget(mP);
        cdm.setSteerableReset(true);
        onHistoryChanged(History.getToken());
    }

    @Override
    public ArrayList<String> getTokenHeaders() {

        ArrayList<String> l = new ArrayList<String>();
        l.add("steering:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        menuItem = new MenuItem("Steering",MenuItem.getDefaultTokenClickListener("steering:"),false,1);
    }

    public void onHistoryChanged(String historyToken) {
        if (historyToken.startsWith("steering:")) {
            //
            // Only reset if artist id is in querystring and we aksed
            if (historyToken.length() > 9 && cdm.getSteerableReset()) {
                cdm.setSteerableReset(false);
                if (historyToken.startsWith("steering:userCloud") && cdm.isLoggedIn()) {
                    mP.loadCloud(cdm.getListenerDetails().getUserTagCloud());
                } else if (historyToken.startsWith("steering:art:")) {
                    mP.loadArtist(historyToken.substring(13));
                } else {
                    mP.loadArtistCloud(historyToken.substring(9));
                }
            }
        }
    }

    public void doRemoveListeners() {
        mP.doRemoveListeners();
        mP.onDelete();
    }

    public class MainPanel extends Composite implements LoginListener, HasListeners {

        private DockPanel dP;
        private Grid mainTagPanel;
        private Grid mainArtistListPanel;
        private TagWidgetContainer tagLand;
        private VerticalPanel savePanel;
        private SearchWidget search;
        private FlowPanel searchBoxContainerPanel;

        private FlowPanel refreshingPanel;

        private HashMap<String, Double> currTagMap;
        private ArtistCompact[] currRecommendations = null;
     
        private ListBox listbox;
        private String currLoadedTagWidget = "";

        public MainPanel() {
            dP = new DockPanel();

            // Left
            mainArtistListPanel = new Grid(1, 1);
            mainArtistListPanel.setWidth("300px");
            mainArtistListPanel.setWidget(0, 0, new Label("Add tags to your tag cloud to get recommendations"));

            HorizontalPanel hP = new HorizontalPanel();
            hP.setStyleName("h2");
            hP.setWidth("300px");
            hP.add(new SpannedLabel("Recommendations"));

            Image viewTagInfluence = new Image("loupe.gif");
            viewTagInfluence.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    displayTagInfluence();
                }
            });
            hP.add(viewTagInfluence);

            refreshingPanel = new FlowPanel();
            refreshingPanel.add(new Image("ajax-loader-small.gif "));
            refreshingPanel.setVisible(false);

            hP.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
            hP.add(refreshingPanel);

            dP.add(WebLib.createSection(hP, mainArtistListPanel), DockPanel.WEST);

            // Right (continued lower)
            mainTagPanel = new Grid(2, 1);
            dP.add(WebLib.createSection("Add tag", mainTagPanel), DockPanel.EAST);

            // Save panel
            savePanel = new VerticalPanel();
            HorizontalPanel saveNamePanel = new HorizontalPanel();
            saveNamePanel.add(new Label("Name:"));
            saveNamePanel.add(new TextBox());
            savePanel.add(saveNamePanel);
            //savePanel.add(new TagInputWidget("tag cloud"));
            savePanel.setVisible(false);

            // North
            HorizontalPanel mainNorthMenuPanel = new HorizontalPanel();
            mainNorthMenuPanel.setSpacing(5);

            Button saveButton = new Button("Save this cloud");
            saveButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    savePanel.setVisible(!savePanel.isVisible());
                }
            });
            mainNorthMenuPanel.add(saveButton);

            Button resetButton = new Button("Erase all tags");
            resetButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    tagLand.removeAllItems(true);
                }
            });
            mainNorthMenuPanel.add(resetButton);

            Button viewCloudButton = new Button("View atomic cloud");
            viewCloudButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    if (currTagMap == null || currTagMap.isEmpty()) {
                        Info.display("Steerable recommendations", 
                                "Cannot display atomic representation; you must " +
                                "add tags in your cloud first.", new Params());
                    } else {
                        HashMap<String, Double> map = currTagMap;
                        ItemInfo[] iI = new ItemInfo[map.size()];
                        int index = 0;
                        for (String s : map.keySet()) {
                            iI[index++] = new ItemInfo(ClientDataManager.nameToKey(s), s, map.get(s), map.get(s));
                        }
                        TagDisplayLib.showTagCloud("Atomic representation of tag cloud",
                                iI, TagDisplayLib.ORDER.SHUFFLE, cdm);
                    }
                }
            });
            mainNorthMenuPanel.add(viewCloudButton);

            HorizontalPanel interfaceSelectPanel = new HorizontalPanel();
            Label interfaceLabel = new SpannedLabel("Interface: ");
            listbox = new ListBox(false);
            listbox.addItem("Cloud");
            listbox.addItem("Meter");
            listbox.addChangeListener(new DataEmbededChangeListener<ListBox>(listbox) {

                public void onChange(Widget arg0) {
                    swapTagWidget(data.getItemText(data.getSelectedIndex()));
                }
            });
            interfaceSelectPanel.add(interfaceLabel);
            interfaceSelectPanel.add(listbox);
            mainNorthMenuPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
            mainNorthMenuPanel.add(interfaceSelectPanel);


            dP.add(mainNorthMenuPanel, DockPanel.NORTH);
            dP.add(savePanel, DockPanel.NORTH);

            //
            // North 2
            tagLand = new TagWidgetContainer(this, cdm);
            tagLand.init(new ResizableTagWidget(this, cdm, tagLand.getSharedCloudArtistMenu()));
            currLoadedTagWidget = "Cloud";
            cdm.getSteerableTagCloudExternalController().setTagWidget(tagLand);
            dP.add(tagLand, DockPanel.NORTH);

            // Right again
            searchBoxContainerPanel = new FlowPanel();
            mainTagPanel.setWidth("185px");
            mainTagPanel.setWidget(1, 0, new Label("Search for tags to add using the above search box"));

            search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel, mainTagPanel, tagLand);
            search.updateSuggestBox(Oracles.TAG);
            mainTagPanel.setWidget(0, 0, search);

            initWidget(dP);
        }

        public void swapTagWidget(String widgetName) {
            if (!currLoadedTagWidget.equals(widgetName)) {
                if (widgetName.equals("Cloud")) {
                    tagLand.swapTagWidget(new ResizableTagWidget(this, cdm, tagLand.getSharedCloudArtistMenu()));
                    currLoadedTagWidget = "Cloud";
                } else {
                    tagLand.swapTagWidget(new TagMeterWidget(this, cdm));
                    currLoadedTagWidget = "Meter";
                }
            }
        }

        /**
         * Display popup of curr tags' influence on the current recommendations
         */
        private void displayTagInfluence() {

            if (currRecommendations == null) {
                Info.display("Steerable recommendations", "Cannot display tag influence cloud; you must add tags in your cloud first.", new Params());
                return;
            }

            // Add every positive-valued tag in tag cloud to influence map so
            // that tags that have no influence will be shown as negative
            HashMap<String, Double> tagInfluenceMap = new HashMap<String, Double>();
            for (String s : currTagMap.keySet()) {
                if (currTagMap.get(s) > 0) {
                    tagInfluenceMap.put(s, -0.01);
                }
            }

            double maxScore = 0;
            double newVal = 0;
            for (ArtistCompact aC : currRecommendations) {
                for (ItemInfo iI : aC.getDistinctiveTags()) {
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
                double val = tagInfluenceMap.get(tagName) / maxScore * currTagMap.get(tagName);
                tagArray[index++] = new ItemInfo(ClientDataManager.nameToKey(tagName), tagName, val, val);
            }
            TagDisplayLib.showTagCloud("Tags' influence on generated recommendations", tagArray, TagDisplayLib.ORDER.SHUFFLE, cdm);
        }

        public void invokeFetchNewRecommendations() {
            AsyncCallback<ArtistCompact[]> callback = new AsyncCallback<ArtistCompact[]>() {

                public void onSuccess(ArtistCompact[] aCArray) {

                    // Remove listeners if we had an ArtistListWidget previously loaded
                    if (mainArtistListPanel.getWidget(0, 0) != null &&
                            mainArtistListPanel.getWidget(0, 0) instanceof ArtistCloudArtistListWidget) {
                        ((ArtistListWidget)mainArtistListPanel.getWidget(0, 0)).doRemoveListeners();
                    }

                    mainArtistListPanel.setWidget(0, 0,
                            new ArtistCloudArtistListWidget(musicServer, cdm, aCArray, tagLand));
                    refreshingPanel.setVisible(false);

                    if (aCArray != null && aCArray.length > 0) {
                        currRecommendations = aCArray;
                    } else {
                        currRecommendations = null;
                    }

                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            refreshingPanel.setVisible(true);

            try {
                currTagMap = tagLand.getTagMap();
                musicServer.getSteerableRecommendations(currTagMap, callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }

        public void onLogin(ListenerDetails lD) {
        }

        public void onLogout() {
        }

        public void loadCloud(ItemInfo[] cloud) {
            tagLand.removeAllItems(true);
            if (cloud != null && cloud.length > 0) {
                tagLand.addTags(cloud, TagWidget.NBR_TOP_TAGS_TO_ADD);
            } else {
                Info.display("Steerable recommendations", "Your user cloud is empty; no tags to add.", new Params());
            }
            History.newItem("steering:");
        }

        public void loadArtistCloud(String artistId) {
            tagLand.removeAllItems(false);
            if (artistId.startsWith("steering:")) {
                artistId = artistId.substring(artistId.indexOf(":")+1);
            }
            invokeGetDistincitveTagsService(artistId);
            invokeGetArtistCompactService(artistId, new AsyncCallback<ArtistCompact>() {

                public void onSuccess(ArtistCompact aC) {
                    if (aC != null) {
                        search.displayArtist(new ItemInfo(aC.getId(), aC.getName(), aC.getNormPopularity(), aC.getNormPopularity()));
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            });
        }
        
        public void loadArtist(String artistId) {
            tagLand.removeAllItems(false);
            if (artistId.startsWith("steering:art:")) {
                artistId = artistId.substring(artistId.indexOf(":")+5);
            }
            invokeGetArtistCompactService(artistId, new AsyncCallback<ArtistCompact>() {

                public void onSuccess(ArtistCompact aC) {
                    if (aC != null) {
                        tagLand.addArtist(aC, 0);  
                        search.displayArtist(new ItemInfo(aC.getId(), aC.getName(), aC.getNormPopularity(), aC.getNormPopularity()));
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            });
        }

        private void invokeGetDistincitveTagsService(String artistID) {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    ItemInfo[] results = (ItemInfo[]) result;
                    if (results != null) {
                        if (results.length == 0) {
                            Window.alert("No tags found for artist");
                        } else {
                            tagLand.addTags(results, TagWidget.NBR_TOP_TAGS_TO_ADD);
                        }
                    } else {
                        Window.alert("An unknown error occured while loading the artist's tag cloud");
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            try {
                musicServer.getDistinctiveTags(artistID, 30, callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());

            }
        }

        private void invokeGetArtistCompactService(String artistId, AsyncCallback<ArtistCompact> callback) {
            try {
                musicServer.getArtistCompact(artistId, callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
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

    private class ItemInfoHierarchyWidget extends Composite implements HasListeners {

        private Grid mainGrid;
        private ItemInfo[] mainItems;
        private ItemInfo[] subItems = null;

        private HasListeners listenerContainer;
        private TagWidget tagLand;

        public ItemInfoHierarchyWidget(ItemInfo[] iI, TagWidget tagLand) {
            mainItems = iI;
            this.tagLand = tagLand;

            mainGrid = new Grid(2,1);
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
            mainGrid.setWidget(1, 0, WebLib.getLoadingBarWidget());
            invokeGetDistincitveTagsService(iI.getId());

            VerticalPanel hP = new VerticalPanel();
            hP.setStyleName("pageHeader");
            hP.setWidth("185px");

            //
            // Add buttons
            HorizontalPanel smallMenuPanel = new HorizontalPanel();
            smallMenuPanel.setSpacing(4);
            if (showBackButton) {
                Label backButton = new Label("Back");
                backButton.setStyleName("headerMenuTinyItem");
                backButton.addClickListener(new ClickListener() {

                    public void onClick(Widget arg0) {
                        displayMainItems();
                    }
                });
                smallMenuPanel.add(backButton);
                smallMenuPanel.add(new SpannedLabel("   "));
            }

            Label addAllButton = new Label("Add top tags");
            addAllButton.setStyleName("headerMenuTinyItem");
            addAllButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    if (subItems != null) {
                        tagLand.addTags(subItems, TagWidget.NBR_TOP_TAGS_TO_ADD);
                    } else {
                        Info.display("Add all tags", "subitems is null", new Params());
                    }
                }
            });
            smallMenuPanel.add(addAllButton);
            smallMenuPanel.add(new SpannedLabel("   "));

            Label addArtistButton = new Label("Add artist");
            addArtistButton.setStyleName("headerMenuTinyItem");
            addArtistButton.addClickListener(new DualDataEmbededClickListener<Label, String>(addArtistButton, iI.getId()) {

                public void onClick(Widget arg0) {
                    data.setText("Processing...");
                    invokeGetArtistCompactService(sndData, new DataEmbededAsyncCallback<Label, ArtistCompact>(data) {

                        public void onSuccess(ArtistCompact aC) {
                            if (aC != null) {
                                tagLand.addArtist(aC, 0);
                            } else {
                                Window.alert("Error fetching artist information.");
                            }
                            data.setText("Add artist");
                        }

                        public void onFailure(Throwable caught) {
                            Window.alert(caught.getMessage());
                        }
                    });
                }
            });
            smallMenuPanel.add(addArtistButton);
            
            hP.add(smallMenuPanel);

            //
            // Add title
            Label title = new Label("Tags for "+iI.getItemName());
            hP.add(title);
            mainGrid.setWidget(0, 0, hP);
        }

        public void displayMainItems() {
            UniqueStore uS = cdm.getArtistOracle();
            subItems = null;
            VerticalPanel vP = new VerticalPanel();

            Label explanation = new Label("Click on an artist's name to display its most distinctive tags");
            explanation.setStyleName("smallItalicExplanation");
            explanation.getElement().setAttribute("style", "margin-bottom: 5px");
            vP.add(explanation);
            
            for (ItemInfo item : mainItems) {
                uS.add(item.getItemName());
                HorizontalPanel hP = new HorizontalPanel();
                
                Label addButton = new Label("Add");
                SwapableWidget sW = new SwapableWidget(addButton, new Image("ajax-loader-small.gif"));
                addButton.setStyleName("recoTags");
                addButton.addStyleName("pointer");
                addButton.getElement().setAttribute("style", "margin-right: 5px");
                addButton.addClickListener(new DualDataEmbededClickListener<String, SwapableWidget>(item.getId(), sW) {
                  
                    public void onClick(Widget sender) {
                        
                        sndData.showWidget(SwapableWidget.LoadableWidget.W2);
                        
                        invokeGetArtistCompactService(data, new DataEmbededAsyncCallback<SwapableWidget, ArtistCompact>(sndData) {

                            public void onSuccess(ArtistCompact aC) {
                                tagLand.addArtist(aC, 0);
                                data.showWidget(SwapableWidget.LoadableWidget.W1);
                            }
                            
                            public void onFailure(Throwable caught) {
                                Window.alert(caught.getMessage());
                            }
                        });
                    }
                });
                hP.add(sW);
                
                Label itemName = new Label(item.getItemName());
                itemName.setStyleName("pointer");
                itemName.addClickListener(new DataEmbededClickListener<ItemInfo>(item) {

                    public void onClick(Widget arg0) {
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
                Window.alert(ex.getMessage());
            }
        }
        
        private void invokeGetDistincitveTagsService(String artistID) {

            AsyncCallback<ItemInfo[]> callback = new AsyncCallback<ItemInfo[]>() {
                public void onSuccess(ItemInfo[] results) {
                    doRemoveListeners();
                    if (results != null) {
                        if (results.length == 0) {
                            mainGrid.setWidget(1, 0, new Label("No tags found"));
                        } else {

                            // Add tags to oracle
                            UniqueStore uS = cdm.getTagOracle();
                            for (ItemInfo iI : results) {
                                uS.add(iI.getItemName());
                            }

                            subItems = results;
                            SortableItemInfoList sIIL = new SortableItemInfoList(results) {
                                protected void onItemClick(ItemInfo i) {
                                    tagLand.addTag(i, 0, true);
                                }
                            };
                            listenerContainer = sIIL;
                            mainGrid.setWidget(1, 0, sIIL);
                        }
                    } else {
                        mainGrid.setWidget(1, 0, new Label("An unknown error occured."));
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            try {
                musicServer.getDistinctiveTags(artistID, 30, callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());

            }
        }

        public void doRemoveListeners() {
            if (listenerContainer != null) {
                listenerContainer.doRemoveListeners();
            }
            listenerContainer = null;
        }
    }

    private abstract class SortableItemInfoList extends Composite implements HasListeners {

        private Grid mainPanel;
        private List<ItemInfo> iI;
        private List<WebListener> webListeners;

        private double maxValue = 0;

        public SortableItemInfoList(ItemInfo[] iI) {
            
            webListeners = new LinkedList<WebListener>();
            this.iI = ItemInfo.arrayToList(iI);

            mainPanel = new Grid(iI.length + 1, 2);

            //
            // Add the title line
            Label nameLbl = new Label("Name");
            nameLbl.addStyleName("pointer");
            nameLbl.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    ((Label)mainPanel.getWidget(0, 0)).setText("Name");
                    ((Label)mainPanel.getWidget(0, 1)).setText("Popularity*");
                    populateMainPanel(ItemInfo.getNameSorter());
                }
            });
            mainPanel.setWidget(0, 0, nameLbl);

            Label popLbl = new Label("Popularity*");
            popLbl.addStyleName("pointer");
            popLbl.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    ((Label)mainPanel.getWidget(0, 0)).setText("Name");
                    ((Label)mainPanel.getWidget(0, 1)).setText("Popularity*");
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
                tagLbl.addClickListener(new DataEmbededClickListener<ItemInfo>(i) {

                    public void onClick(Widget arg0) {
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
                mainPanel.setWidget(lineIndex, 1, WebLib.getSmallPopularityWidget(i.getPopularity()/maxValue, 75, true, false));
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

        public SearchWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, Panel searchBoxContainerPanel, Grid mainTagPanel,
            TagWidget tagLand) {

            super(musicServer, cdm, searchBoxContainerPanel);

            searchBoxStyleName="";

            this.mainTagPanel = mainTagPanel;
            this.tagLand = tagLand;

            searchBoxContainerPanel.add(WebLib.getLoadingBarWidget());

            HorizontalPanel searchType = new HorizontalPanel();
            searchType.setSpacing(5);
            searchButtons = new SearchTypeRadioButton[2];
            searchButtons[0] = new SearchTypeRadioButton("searchType", "For Tag", searchTypes.SEARCH_FOR_TAG_BY_TAG);
            searchButtons[1] = new SearchTypeRadioButton("searchType", "By Artist", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);
            searchButtons[0].setChecked(true);

            searchButtons[1].addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    updateSuggestBox(Oracles.ARTIST);
                }
            });
            searchButtons[0].addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    updateSuggestBox(Oracles.TAG);
                }
            });

            updateSuggestBox(Oracles.TAG);
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

        public void search() {
            mainTagPanel.setWidget(1, 0, WebLib.getLoadingBarWidget());
            if (getCurrLoadedOracle() == Oracles.TAG) {
                invokeTagSearchService(textBox.getText().toLowerCase());
            } else {
                invokeArtistSearchService(textBox.getText().toLowerCase());
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

            AsyncCallback callback = new AsyncCallback()  {

                public void onSuccess(Object result) {
                    SearchResults sr = (SearchResults) result;
                    if (sr != null && sr.isOK()) {
                        ItemInfo[] results = sr.getItemResults();
                        doRemoveListeners();
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No Match for " + sr.getQuery()));
                        } else {
                            ItemInfoHierarchyWidget iihw = new ItemInfoHierarchyWidget(results, tagLand);
                            listenersContainer = iihw;
                            mainTagPanel.setWidget(1, 0, iihw);
                        }
                    } else {
                        if (sr == null) {
                            Window.alert("Error. Resultset is null. There were probably no tags found.");
                        } else {
                            Window.alert("Whoops " + sr.getStatus());
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            try {
                musicServer.artistSearch(searchText, 10, callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }

        private void invokeTagSearchService(String searchText) {

            AsyncCallback callback = new AsyncCallback()  {

                public void onSuccess(Object result) {
                    SearchResults sr = (SearchResults) result;
                    if (sr != null && sr.isOK()) {
                        ItemInfo[] results = sr.getItemResults();
                        doRemoveListeners();
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No Match for " + sr.getQuery()));
                        } else {
                            SortableItemInfoList siil = new SortableItemInfoList(results) {

                                protected void onItemClick(ItemInfo i) {
                                    tagLand.addTag(i, 0, true);
                                }
                            };
                            listenersContainer = siil;
                            mainTagPanel.setWidget(1, 0, siil);
                        }
                    } else {
                        if (sr == null) {
                            Window.alert("Error. Resultset is null. There were probably no tags found.");
                        } else {
                            Window.alert("Whoops " + sr.getStatus());
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            mainTagPanel.setWidget(1, 0, WebLib.getLoadingBarWidget());

            try {
                musicServer.tagSearch(searchText, 100, callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }

        public void doRemoveListeners() {
            if (listenersContainer != null) {
                listenersContainer.doRemoveListeners();
                listenersContainer = null;
            }
        }
    }

    private class ArtistCloudArtistListWidget extends ArtistListWidget {

        private TagWidget tagLand;

        public ArtistCloudArtistListWidget(MusicSearchInterfaceAsync musicServer,
                ClientDataManager cdm, ArtistCompact[] aDArray, TagWidget tagLand) {

            super(musicServer, cdm, aDArray, cdm.isLoggedIn());
            this.tagLand = tagLand;
        }

        @Override
        public void onTagClick(ItemInfo tag) {
            tagLand.addTag(tag, 0, true);
        }

        public void openWhyPopup(SwapableTxtButton why) {
            why.showLoad();
            TagDisplayLib.invokeGetCommonTags(tagLand.getTagMap(), why.getId(),
                    musicServer, cdm,
                    new CommonTagsAsyncCallback(why, "Common tags between your cloud and "+why.getName(), cdm) {});
        }

        @Override
        public void openDiffPopup(DiffButton diff) {

            ItemInfo[] steerTags = ItemInfo.mapToArray(tagLand.getTagMap());
            TagDisplayLib.showDifferenceCloud("Difference cloud between your tag cloud and "+diff.getName(),
                    steerTags, diff.getDistinctiveTags(), cdm);

        }
    }
}