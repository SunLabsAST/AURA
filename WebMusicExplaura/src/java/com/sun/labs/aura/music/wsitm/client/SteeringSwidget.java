/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.ArrayList;
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
        private ArtistListWidget artistList;

        private VerticalPanel savePanel;

        private SuggestBox sbox;

        public MainPanel() {
            dP = new DockPanel();

            // Right
            mainTagPanel = new Grid(2,1);
            mainTagPanel.setWidth("200px");
            updateTagSuggestBox();
            mainTagPanel.setWidget(1, 0, new Label("Search for tags to add using the above search box"));
            dP.add(WebLib.createSection("Add tag", mainTagPanel), DockPanel.EAST);

            // Left
            mainArtistListPanel = new Grid(1,1);
            mainArtistListPanel.setWidth("300px");
            mainArtistListPanel.setWidget(0, 0, new Label("Add tags to your tag cloud to get recommendations"));
            dP.add(WebLib.createSection("Recommendations", mainArtistListPanel), DockPanel.WEST);

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

            tagLand = new ResizableTagWidget();
            dP.add(tagLand, DockPanel.NORTH);

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

        private void updateTagSuggestBox() {
            if (cdm.getTagOracle() == null) {
                invokeOracleFetchService();
                return;
            }

            sbox = new SuggestBox(cdm.getTagOracle());
            //sbox.setStyleName("searchText");
            sbox.ensureDebugId("cwSuggestBox");
            sbox.setLimit(20);

            sbox.addKeyboardListener(new KeyboardListenerAdapter() {

                public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                    if (keyCode == KEY_ENTER) {

                        /* Hack to go around the bug of the suggestbox which wasn't
                         * using the highlighted element of the suggetions popup
                         * when submitting the form
                         * */
                        DeferredCommand.addCommand(new Command() {

                            public void execute() {
                                invokeTagSearchService(sbox.getText().toLowerCase(), 0);
                            }
                        });
                    }
                }
            });

            mainTagPanel.setWidget(0, 0, sbox);
        }

        public void onLogin(ListenerDetails lD) {
        }

        public void onLogout() {
        }

        private void invokeTagSearchService(String searchText, int page) {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    // do some UI stuff to show success
                    SearchResults sr = (SearchResults) result;
                    if (sr != null && sr.isOK()) {
                        ItemInfo[] results = sr.getItemResults();
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No Match for " + sr.getQuery()));
                        } else {
                            VerticalPanel hP = new VerticalPanel();
                            for (ItemInfo iI : results) {
                                cdm.getTagOracle().add(iI.getItemName());
                                Label lbl = new Label(iI.getItemName());
                                lbl.addClickListener(new TagClickListener(iI));
                                hP.add(lbl);
                            }
                            mainTagPanel.setWidget(1, 0, hP);
                        }
                    } else {
                        if (sr == null) {
                            Window.alert("Error. Resultset is null. There were probably no tags foud.s");
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

        private void invokeOracleFetchService() {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    // do some UI stuff to show success
                    List<String> callBackList = (List<String>) result;
                    MultiWordSuggestOracle newOracle = new MultiWordSuggestOracle();
                    newOracle.addAll(callBackList);
                    cdm.setTagOracle(newOracle);
                    updateTagSuggestBox();
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            mainTagPanel.setWidget(0, 0, WebLib.getLoadingBarWidget());

            try {
                musicServer.getTagOracle(callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }

        public class TagClickListener implements ClickListener {

            ItemInfo iI;

            public TagClickListener(ItemInfo iI) {
                this.iI = iI;
            }

            public void onClick(Widget arg0) {
                tagLand.addTag(iI);
            }
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
            fP.setWidth("600px");
            fP.setHeight("450px");
            flowP = new FlowPanel();
            flowP.setWidth("600px");
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
}
