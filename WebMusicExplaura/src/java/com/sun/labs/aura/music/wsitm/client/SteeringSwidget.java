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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.AbstractSearchWidget.Oracles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class SteeringSwidget extends Swidget {

    private MainPanel mP;

    public SteeringSwidget(ClientDataManager cdm) {
        super("Steering", cdm);
        mP = new MainPanel();
        registerLoginListener(mP);
        initWidget(mP);
    }

    public List<String> getTokenHeaders() {

        List<String> l = new ArrayList<String>();
        l.add("steering:");
        return l;
    }

    private class MainPanel extends LoginListener {

        private DockPanel dP;
        private Grid mainTagPanel;
        private Grid mainArtistListPanel;
        private ResizableTagWidget tagLand;
        private VerticalPanel savePanel;
        private SearchWidget search;
        private FlowPanel searchBoxContainerPanel;

        public MainPanel() {
            dP = new DockPanel();

            // Left
            mainArtistListPanel = new Grid(1, 1);
            mainArtistListPanel.setWidth("300px");
            mainArtistListPanel.setWidget(0, 0, new Label("Add tags to your tag cloud to get recommendations"));
            dP.add(WebLib.createSection("Recommendations", mainArtistListPanel), DockPanel.WEST);

            // Right (continued lower)
            mainTagPanel = new Grid(2, 1);
            dP.add(WebLib.createSection("Add tag", mainTagPanel), DockPanel.EAST);

            // Save panel
            savePanel = new VerticalPanel();
            HorizontalPanel saveNamePanel = new HorizontalPanel();
            saveNamePanel.add(new Label("Name:"));
            saveNamePanel.add(new TextBox());
            savePanel.add(saveNamePanel);
            savePanel.add(new TagInputWidget("tag cloud"));
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

            Button updateButton = new Button("Update recommendations");
            updateButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    invokeFetchNewRecommendations();
                }
            });
            mainNorthMenuPanel.add(updateButton);


            Button resetButton = new Button("Erase all tags");
            resetButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    tagLand.removeAllTags();
                }
            });
            mainNorthMenuPanel.add(resetButton);

            dP.add(mainNorthMenuPanel, DockPanel.NORTH);
            dP.add(savePanel, DockPanel.NORTH);

            //
            // North 2
            tagLand = new ResizableTagWidget();
            dP.add(tagLand, DockPanel.NORTH);

            // Right again
            searchBoxContainerPanel = new FlowPanel();
            mainTagPanel.setWidth("200px");
            mainTagPanel.setWidget(1, 0, new Label("Search for tags to add using the above search box"));

            search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel, mainTagPanel, tagLand);
            search.updateSuggestBox(Oracles.TAG);
            mainTagPanel.setWidget(0, 0, search);

            initWidget(dP);
        }

        private void invokeFetchNewRecommendations() {
            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {

                    ArtistCompact[] aCArray = (ArtistCompact[]) result;
                    mainArtistListPanel.setWidget(0, 0,
                            new ArtistListWidget(musicServer, cdm.getListenerDetails(), aCArray));

                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            mainArtistListPanel.setWidget(0, 0, WebLib.getLoadingBarWidget());

            try {
                musicServer.getSteerableRecommendations(tagLand.getTapMap(), callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }

        public void onLogin(ListenerDetails lD) {
        }

        public void onLogout() {
        }

    }

    public class ResizableTagWidget extends Composite {

        private Map<String, DeletableResizableTag> tagCloud;

        private double maxSize = 0.1;

        private Grid g;
        private FocusPanel fP;
        private FlowPanel flowP;

        private int lastX;
        private int lastY;
        
        int colorIndex = 1;
        
        private ColorConfig[] color;

        public ResizableTagWidget() {

            fP = new FocusPanel();
            fP.setWidth("500px");
            fP.setHeight("450px");
            flowP = new FlowPanel();
            flowP.setWidth("500px");
            flowP.getElement().setAttribute("style", "margin-top: 15px");
            fP.add(flowP);
            initWidget(fP);

            color = new ColorConfig[2];
            color[0] = new ColorConfig("#D4C790", "#D49090");
            color[1] = new ColorConfig("#ADA376", "#AD7676");

            tagCloud = new HashMap<String, DeletableResizableTag>();

            fP.addMouseListener(new MouseListener() {

                public void onMouseDown(Widget arg0, int arg1, int arg2) {
                    lastX = arg1;
                    lastY = arg2;
                }

                public void onMouseEnter(Widget arg0) {
                }

                public void onMouseLeave(Widget arg0) {
                    for (DeletableWidget<ResizableTag> dW : tagCloud.values()) {
                        dW.getWidget().setClickFalse();
                    }
                }

                public void onMouseMove(Widget arg0, int arg1, int arg2) {

                    //DOM.eventPreventDefault(DOM.eventGetCurrentEvent());

                    int increment = lastY - arg2;

                    double diff = 0;
                    maxSize = 0; // reset maxsize to deal with when the top tag is scaled down
                    for (DeletableResizableTag dW : tagCloud.values()) {
                        double tempDiff = dW.getWidget().updateSize(increment, true);
                        dW.setXButtonPosition();
                        if (tempDiff != 0) {
                            diff = tempDiff;
                        }
                        if (Math.abs(dW.getWidget().getCurrentSize())>maxSize) {
                            maxSize = Math.abs(dW.getWidget().getCurrentSize());
                        }
                    }
                    
                    //
                    // Do a second pass to modify the tags that aren't being resized
                    // if the one that is resized has reached its maximum of minimum
                    // size
                    if (diff != 0) {
                        diff = diff / (tagCloud.size()-1);
                        for (DeletableResizableTag dW : tagCloud.values()) {
                            dW.getWidget().updateSize(diff, false);
                            dW.setXButtonPosition();
                            if (Math.abs(dW.getWidget().getCurrentSize())>maxSize) {
                                maxSize = Math.abs(dW.getWidget().getCurrentSize());
                            }
                        }
                    }

                    lastX = arg1;
                    lastY = arg2;
                }

                public void onMouseUp(Widget arg0, int arg1, int arg2) {
                    for (DeletableWidget<ResizableTag> dW : tagCloud.values()) {
                        dW.getWidget().setClickFalse();
                    }
                }
            });

        }

        public Map<String, Double> getTapMap() {
            Map<String, Double> tagMap = new HashMap<String, Double>();
            // Add the tags normalised by the size of the biggest one
            for (String tag : tagCloud.keySet()) {
                tagMap.put(tagCloud.get(tag).getWidget().getText(), tagCloud.get(tag).getWidget().getCurrentSize()/maxSize);
            }
            return tagMap;
        }

        public void addTag(ItemInfo tag) {
            if (!tagCloud.containsKey(tag.getId())) {
                ResizableTag rT = new ResizableTag(tag.getItemName(), color[(colorIndex++)%2]);
                DeletableResizableTag dW = new DeletableResizableTag(rT);

                tagCloud.put(tag.getId(), dW);
                flowP.add(dW);
                flowP.add(new SpannedLabel(" "));
            }
        }

        public void removeTag(String tagId) {
            if (tagCloud.containsKey(tagId)) {
                flowP.remove(tagCloud.get(tagId));
                tagCloud.remove(tagId);
                redrawTagCloud();
            }
        }

        public void removeAllTags() {
            tagCloud.clear();
            flowP.clear();
            colorIndex=1;
        }

        public void redrawTagCloud() {
            colorIndex = 1;
            for (DeletableWidget<ResizableTag> dW : tagCloud.values()) {
                dW.getWidget().updateColor(color[(colorIndex++)%2]);
            }
        }

        public class DeletableResizableTag extends DeletableWidget<ResizableTag> {

            public DeletableResizableTag(ResizableTag t) {
                super(t);

                xB.getElement().setAttribute("style",
                        "display:none; margin-bottom: "+getXButtonMargin()+"px;");
            }

            private final double getXButtonMargin() {
                return Math.abs(getWidget().getCurrentSize())*0.6;
            }

            public void setXButtonPosition() {
                String displayAttrib = xB.getElement().getAttribute("style");
                String newStyle = "";
                for (String s : displayAttrib.split(";")) {
                    String[] sSplit = s.split(":");
                    if (sSplit[0].trim().equals("margin-bottom")) {
                        newStyle += "margin-bottom:"+getXButtonMargin()+"px;";
                    } else {
                        newStyle += s+";";
                    }
                }
                xB.getElement().setAttribute("style", newStyle);
            }

            public void onDelete() {
                removeTag(ClientDataManager.nameToKey(getWidget().getText()));
            }
        }

        public class ResizableTag extends SpannedLabel {

            private boolean hasClicked = false;
            private FocusPanel fP;
            private double currentSize = 40;
            private ColorConfig color;

            private static final int MIN_SIZE = 4;
            private static final int MAX_SIZE = 175;

            public ResizableTag(String txt, ColorConfig color) {
                super(txt);
                addStyleName("marginRight");
                this.color = color;
                resetAttributes();

                addMouseListener(new MouseListener() {

                    public void onMouseDown(Widget arg0, int arg1, int arg2) {
                        hasClicked = true;
                    }

                    public void onMouseEnter(Widget arg0) {
                    }

                    public void onMouseLeave(Widget arg0) {
                    }

                    public void onMouseMove(Widget arg0, int arg1, int arg2) {
                    }

                    public void onMouseUp(Widget arg0, int arg1, int arg2) {
                        hasClicked = false;
                    }
                });

            }

            private final void resetAttributes() {
                getElement().setAttribute("style", "font-size:"+Math.abs(currentSize)+"px; color:"+color.getColor(currentSize)+";");
            }

            public void updateColor(ColorConfig color) {
                this.color = color;
                updateSize(0, true);
            }

            public void updateSize(int increment, ColorConfig color) {
                this.color = color;
                updateSize(increment, true);
            }

            /**
             * Update size of tag
             * @param increment increment by which to increase or decrease the tag's size
             * @param modifyHasClicked modify the tag that is being dragged or all the others
             * @return the increment by which the other tags need to be resized if this tag has reached it's maximum or minimum size
             */
            public double updateSize(double increment, boolean modifyHasClicked) {
                if (hasClicked == modifyHasClicked) {
                    currentSize += increment;
                    // If we're crossing from positive to negative
                    if (-MIN_SIZE<currentSize && currentSize<MIN_SIZE) {
                        currentSize = MIN_SIZE * increment/Math.abs(increment);
                        return 0;
                    } else if (Math.abs(currentSize)>MAX_SIZE) {
                        double absCurrSize = Math.abs(currentSize);
                        double diff = currentSize/absCurrSize * (MAX_SIZE - absCurrSize);
                        currentSize = currentSize/absCurrSize * MAX_SIZE;
                        return diff;
                    }
                    resetAttributes();
                }
                return 0;
            }

            public double getCurrentSize() {
                return currentSize;
            }

            public void setClickFalse() {
                hasClicked = false;
            }

            public boolean equals(ResizableTag rT) {
                return this.getText().equals(rT.getText());
            }
        }

        public class ColorConfig {

            private String positive;
            private String negative;

            public ColorConfig(String positive, String negative) {
                this.positive = positive;
                this.negative = negative;
            }

            /**
             * Return the right color based on the current size of the item.
             * @param size
             * @return
             */
            public final String getColor(double size) {
                if (size<0) {
                    return negative;
                } else {
                    return positive;
                }
            }

        }
    }

    public class ItemInfoHierarchyWidget extends Composite {

        private Grid mainGrid;
        private ItemInfo[] mainItems;

        private ResizableTagWidget tagLand;

        public ItemInfoHierarchyWidget(ItemInfo[] iI, ResizableTagWidget tagLand) {
            mainGrid = new Grid(3,1);
            mainItems = iI;
            this.tagLand = tagLand;

        }

        /**
         * Called when user clicks on one of the main items. Will display the
         * details for this item along with a header allowing the user to go back
         * to the main item list
         * @param iI
         */
        public void displayDetails(ItemInfo iI) {
            mainGrid.getCellFormatter().getElement(0, 0).setAttribute("style", "display:none;");
            mainGrid.getCellFormatter().getElement(1, 0).setAttribute("style", "display:;");
            mainGrid.getCellFormatter().getElement(2, 0).setAttribute("style", "display:;");
            mainGrid.setWidget(2, 0, WebLib.getLoadingBarWidget());
            invokeGetDistincitveTagsService(iI.getId());

            HorizontalPanel hP = new HorizontalPanel();
            SpannedLabel backButton = new SpannedLabel("Back");
            backButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    displayMainItems();
                }
            });

            SpannedLabel title = new SpannedLabel("Tags for "+iI.getItemName());
            hP.add(backButton);
            hP.add(title);
            mainGrid.setWidget(1, 0, hP);

        }

        public void displayMainItems() {
            mainGrid.getCellFormatter().getElement(0, 0).setAttribute("style", "display:;");
            mainGrid.getCellFormatter().getElement(1, 0).setAttribute("style", "display:none;");
            mainGrid.getCellFormatter().getElement(2, 0).setAttribute("style", "display:none;");

            VerticalPanel vP = new VerticalPanel();
            for (ItemInfo item : mainItems) {
                Label itemName = new Label(item.getItemName());
                itemName.addClickListener(new DataEmbededClickListener<ItemInfo>(item) {

                    public void onClick(Widget arg0) {
                        displayDetails(data);
                    }
                });
                vP.add(itemName);
            }
            mainGrid.setWidget(0, 0, vP);
        }

        private void invokeGetDistincitveTagsService(String artistID) {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    ItemInfo[] results = (ItemInfo[]) result;
                    if (results != null) {
                        if (results.length == 0) {
                            mainGrid.setWidget(2, 0, new Label("No tags found"));
                        } else {
                            mainGrid.setWidget(2, 0, new SortableItemInfoList(results) {

                                protected void onItemClick(ItemInfo i) {
                                    tagLand.addTag(i);
                                }
                            });
                        }
                    } else {
                        mainGrid.setWidget(2, 0, new Label("An unknown error occured."));
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
    }

    public abstract class SortableItemInfoList extends Composite {

        private Grid mainPanel;
        private List<ItemInfo> iI;

        public SortableItemInfoList(ItemInfo[] iI) {
            this.iI = new ArrayList<ItemInfo>();
            for (ItemInfo i : iI) {
                this.iI.add(i);
            }

            mainPanel = new Grid(iI.length + 1, 2);

            //
            // Add the title line
            Label nameLbl = new Label("Name *");
            nameLbl.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    ((Label)mainPanel.getWidget(0, 0)).setText("Name");
                    ((Label)mainPanel.getWidget(0, 1)).setText("Popularity *");
                    populateMainPanel(new NameSorter());
                }
            });
            mainPanel.setWidget(0, 0, nameLbl);

            Label popLbl = new Label("Popularity");
            popLbl.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    ((Label)mainPanel.getWidget(0, 0)).setText("Name");
                    ((Label)mainPanel.getWidget(0, 1)).setText("Popularity *");
                    populateMainPanel(new PopularitySorter());
                }
            });
            mainPanel.setWidget(0, 1, popLbl);

            populateMainPanel(new PopularitySorter());
            initWidget(mainPanel);
        }

        private void populateMainPanel(Comparator<ItemInfo> c) {

            //
            // Add all the items
            Collections.sort(iI, c);
            int lineIndex = 1;
            for (ItemInfo i : iI) {
                Label tagLbl = new Label(i.getItemName());
                tagLbl.addClickListener(new TagClickListener(i));
                tagLbl.setStyleName("smallTagClick");
                mainPanel.setWidget(lineIndex, 0, tagLbl);
                mainPanel.setWidget(lineIndex, 1, WebLib.getSmallPopularityWidget(i.getPopularity(), 75, true, false));
                lineIndex++;
            }
        }

        protected abstract void onItemClick(ItemInfo i);

        public class PopularitySorter implements Comparator<ItemInfo> {

            public int compare(ItemInfo o1, ItemInfo o2) {
                return new Double(o1.getPopularity()).compareTo(new Double(o2.getPopularity()));
            }

        }

        public class NameSorter implements Comparator<ItemInfo> {

            public int compare(ItemInfo o1, ItemInfo o2) {
                return o1.getItemName().compareTo(o2.getItemName());
            }
        }

        public class TagClickListener implements ClickListener {

            ItemInfo iI;

            public TagClickListener(ItemInfo iI) {
                this.iI = iI;
            }

            public void onClick(Widget arg0) {
                onItemClick(iI);
            }
        }
    }

    public class SearchWidget extends AbstractSearchWidget {

        private Grid mainTagPanel;
        private ResizableTagWidget tagLand;

        public SearchWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, Panel searchBoxContainerPanel, Grid mainTagPanel,
            ResizableTagWidget tagLand) {

            super(musicServer, cdm, searchBoxContainerPanel);

            searchBoxStyleName="";

            this.mainTagPanel = mainTagPanel;
            this.tagLand = tagLand;

            textBox = new SuggestBox();
            textBox.setTabIndex(1);
            setSuggestBoxWidth(50);

            searchBoxContainerPanel.add(WebLib.getLoadingBarWidget());

            Panel searchType = new VerticalPanel();
            searchButtons = new RadioButton[2];
            searchButtons[0] = new RadioButton("searchType", "By Artist");
            searchButtons[1] = new RadioButton("searchType", "For Tag");

            searchButtons[0].addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    updateSuggestBox(Oracles.ARTIST);
                }
            });
            searchButtons[1].addClickListener(new ClickListener() {
                public void onClick(Widget arg0) {
                    updateSuggestBox(Oracles.TAG);
                }
            });

            setText("", SearchResults.SEARCH_FOR_ARTIST_BY_ARTIST);
            updateSuggestBox(Oracles.ARTIST);

            for (int i = 0; i < searchButtons.length; i++) {
                searchType.add(searchButtons[i]);
                searchButtons[i].getElement().setAttribute("style", "font-size: 12px");
            }
            searchType.setWidth("100%");
            searchType.setStyleName("searchPanel");

            HorizontalPanel searchPanel = new HorizontalPanel();
            searchPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
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
            this.setWidth("200px");
        }

        public void search() {
            if (getCurrLoadedOracle() == Oracles.TAG) {
                invokeTagSearchService(textBox.getText().toLowerCase());
            } else {
                invokeArtistSearchService(textBox.getText().toLowerCase());
            }
        }

        private void invokeArtistSearchService(String searchText) {

            AsyncCallback callback = new AsyncCallback()  {

                public void onSuccess(Object result) {
                    SearchResults sr = (SearchResults) result;
                    if (sr != null && sr.isOK()) {
                        ItemInfo[] results = sr.getItemResults();
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No Match for " + sr.getQuery()));
                        } else {
                            mainTagPanel.setWidget(1, 0, new ItemInfoHierarchyWidget(results, tagLand));
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
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No Match for " + sr.getQuery()));
                        } else {
                            mainTagPanel.setWidget(1, 0, new SortableItemInfoList(results) {

                                protected void onItemClick(ItemInfo i) {
                                    tagLand.addTag(i);
                                }
                            });
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
    }
}