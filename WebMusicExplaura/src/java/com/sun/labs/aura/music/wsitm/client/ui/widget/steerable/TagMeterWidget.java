/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.widget.steerable;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.ui.widget.DeletableWidget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededMouseListener;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.items.steerable.WrapsCloudItem;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.SteeringSwidget.MainPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Meter cloud manipulation UI
 * @author mailletf
 */
public class TagMeterWidget extends TagWidget {

    private final static int MAX_TAG_VALUE = CloudItemMeter.TOTAL_WIDTH;
    private final static int DEFAULT_TAG_VALUE = MAX_TAG_VALUE / 2;
    private VerticalPanel mainTagPanel;
    private HashMap<String, CloudItemMeter> tagCloud;
    private ClientDataManager cdm;

    public TagMeterWidget(MainPanel mainPanel, ClientDataManager cdm) {
        super(mainPanel);

        this.cdm = cdm;
        int panelWidth = 480;
        if (Window.getClientWidth() > 1024) {
            panelWidth = (int) (Window.getClientWidth() * 480.0 / 1024.0);
        }

        mainTagPanel = new VerticalPanel();
        mainTagPanel.setWidth(panelWidth + "px");
        tagCloud = new HashMap<String, CloudItemMeter>();
        initWidget(mainTagPanel);
    }

    public HashMap<String, CloudItem> getItemsMap() {
        HashMap<String, CloudItem> itemsMap = new HashMap<String, CloudItem>();
        for (CloudItemMeter cim : tagCloud.values()) {
            if (cim.getRating() <= CloudItemMeter.RED_WIDTH) {
                cim.getCloudItem().setWeight(-1);
            }
            itemsMap.put(cim.getCloudItem().getId(), cim.getCloudItem());
        }
        return itemsMap;
    }

    @Override
    public double getMaxWeight() {
        double maxVal = 0;
        double tempVal = 0;
        for (CloudItemMeter i : tagCloud.values()) {
            tempVal = i.getCloudItem().getWeight();
            if (tempVal > maxVal) {
                maxVal = tempVal;
            }
        }
        return maxVal;
    }

    public void removeItem(String itemId) {
        if (tagCloud.containsKey(itemId)) {
            mainTagPanel.remove(tagCloud.get(itemId));
            tagCloud.remove(itemId);
            updateRecommendations();
            cdm.getTagCloudListenerManager().triggerOnTagDelete(itemId);
        } else {
            Window.alert(itemId + " is not in tagcloud");
        }
    }

    public void removeAllItems(boolean updateRecommendations) {
        mainTagPanel.clear();
        tagCloud.clear();
        cdm.getTagCloudListenerManager().triggerOnTagDeleteAll();
        updateRecommendations();
    }

    @Override
    public void addItems(HashMap<String, CloudItem> items, ITEM_WEIGHT_TYPE weightType, int limit) {
        if (items != null && items.size() > 0) {
            if (limit == 0) {
                limit = items.size();
            }

            ArrayList<CloudItem> itemsList = new ArrayList<CloudItem>(items.values());
            Collections.sort(itemsList, new CloudItemWeightSorter());
            double maxValue = itemsList.get(0).getWeight();

            int cnt = 0;
            for (CloudItem cI : itemsList) {
                if (weightType == ITEM_WEIGHT_TYPE.RELATIVE) {
                    cI.setWeight( cI.getWeight() / maxValue * MAX_TAG_VALUE );
                }
                addItem(cI, false);
                if (cnt++ > limit) {
                    break;
                }
            }
            updateRecommendations();
        }
    }

    @Override
    public void addItem(CloudItem item, boolean updateRecommendations) {
        if (!tagCloud.containsKey(item.getId())) {
            if (item.getWeight() == 0) {
                item.setWeight( DEFAULT_TAG_VALUE );
            }
            CloudItemMeter newCI = new CloudItemMeter(item);
            tagCloud.put(item.getId(), newCI);
            mainTagPanel.add(newCI);
            cdm.getTagCloudListenerManager().triggerOnTagAdd(item.getId());

            if (updateRecommendations) {
                updateRecommendations();
            }
        }
    }

    @Override
    public boolean containsItem(String itemId) {
        return tagCloud.containsKey(itemId);
    }

    /**
     * Item wrapping a CloudItem for display in the meter UI
     */
    private class CloudItemMeter extends Composite implements WrapsCloudItem {

        private static final int METER_HEIGHT = 16;
        public static final int RED_WIDTH = 23;
        public static final int TOTAL_WIDTH = 280;
        private static final String METER_OFF = "meter-off.jpg";
        private static final String METER_ON = "meter-on.jpg";
        private static final String METER_HOVER = "meter-hover.jpg";
        private CloudItem item;
        private Grid mainPanel;
        private FocusPanel fP;
        private Image redMeter;
        private Image greenMeterHover;
        private Image greenMeterLeft;
        private Image greenMeterRight;

        private CloudItemMeter(CloudItem item) {
            this.item = item;

            if (item.getWeight() == 0) {
                item.setWeight(DEFAULT_TAG_VALUE);
            } else if (item.getWeight() > TOTAL_WIDTH) {
                item.setWeight(MAX_TAG_VALUE);
            }

            mainPanel = new Grid(1, 2);
            mainPanel.setWidth("100%");

            fP = new FocusPanel();

            redMeter = new Image(METER_OFF);
            greenMeterHover = new Image(METER_HOVER);
            greenMeterLeft = new Image(METER_ON);
            greenMeterRight = new Image(METER_OFF);
            HorizontalPanel hP = new HorizontalPanel();
            hP.add(redMeter);
            hP.add(greenMeterHover);
            hP.add(greenMeterLeft);
            hP.add(greenMeterRight);
            fP.add(hP);

            fP.getElement().setAttribute("style", "margin-right: 40px");
            fP.addMouseListener(new DataEmbededMouseListener<CloudItem>(item) {

                public void onMouseLeave(Widget arg0) {
                    redrawMeter();
                }

                public void onMouseMove(Widget arg0, int y, int x) {
                    // We are hovering over the red section
                    if (y <= RED_WIDTH) {
                        redMeter.setUrlAndVisibleRect(METER_HOVER, 0, 0, RED_WIDTH, METER_HEIGHT);
                        if (data.getWeight() > RED_WIDTH) {
                            greenMeterLeft.setVisibleRect(RED_WIDTH, 0, (int)data.getWeight() - RED_WIDTH, METER_HEIGHT);
                            greenMeterRight.setVisibleRect((int)data.getWeight(), 0, TOTAL_WIDTH - (int)data.getWeight(), METER_HEIGHT);
                        } else {
                            greenMeterLeft.setVisibleRect(0, 0, 0, METER_HEIGHT);
                            greenMeterRight.setVisibleRect(RED_WIDTH, 0, TOTAL_WIDTH - RED_WIDTH, METER_HEIGHT);
                        }
                    // We are hovering over the green section
                    } else {
                        if (data.getWeight() <= RED_WIDTH) {
                            redMeter.setUrlAndVisibleRect(METER_ON, 0, 0, RED_WIDTH, METER_HEIGHT);
                        } else {
                            redMeter.setUrlAndVisibleRect(METER_OFF, 0, 0, RED_WIDTH, METER_HEIGHT);
                        }

                        greenMeterHover.setVisibleRect(RED_WIDTH, 0, y - RED_WIDTH, METER_HEIGHT);
                        // If we are higher than the previous rating
                        if (y > data.getWeight()) {
                            greenMeterLeft.setVisibleRect(0, 0, 0, METER_HEIGHT);
                            greenMeterRight.setVisibleRect(y, 0, TOTAL_WIDTH - y, METER_HEIGHT);
                        } else {
                            greenMeterLeft.setVisibleRect(y, 0, (int)data.getWeight() - y, METER_HEIGHT);
                            greenMeterRight.setVisibleRect((int)data.getWeight(), 0, TOTAL_WIDTH - (int)data.getWeight(), METER_HEIGHT);
                        }
                    }
                }

                public void onMouseUp(Widget arg0, int y, int x) {
                    ((FocusPanel) arg0).setFocus(false);
                    data.setWeight(y);
                    redrawMeter();
                    updateRecommendations();
                }
                
                public void onMouseDown(Widget arg0, int arg1, int arg2) {}
                public void onMouseEnter(Widget arg0) {}
                
            });

            mainPanel.getCellFormatter().setHorizontalAlignment(0, 0, HorizontalPanel.ALIGN_LEFT);
            mainPanel.setWidget(0, 0, new DeletableTag(item));

            mainPanel.getCellFormatter().setHorizontalAlignment(0, 1, HorizontalPanel.ALIGN_RIGHT);
            mainPanel.getCellFormatter().setWidth(0, 1, "150px");
            mainPanel.setWidget(0, 1, fP);

            initWidget(mainPanel);

            redrawMeter();
        }

        private void redrawMeter() {
            if (item.getWeight() > RED_WIDTH) {
                redMeter.setUrlAndVisibleRect(METER_OFF, 0, 0, RED_WIDTH, METER_HEIGHT);
                greenMeterHover.setVisibleRect(0, 0, 0, METER_HEIGHT);
                greenMeterLeft.setUrlAndVisibleRect(METER_ON, RED_WIDTH, 0, (int)item.getWeight() - RED_WIDTH, METER_HEIGHT);
                greenMeterRight.setUrlAndVisibleRect(METER_OFF, (int)item.getWeight(), 0, TOTAL_WIDTH - (int)item.getWeight(), METER_HEIGHT);
            } else {
                redMeter.setUrlAndVisibleRect(METER_ON, 0, 0, RED_WIDTH, METER_HEIGHT);
                greenMeterHover.setVisibleRect(0, 0, 0, METER_HEIGHT);
                greenMeterLeft.setUrlAndVisibleRect(METER_ON, 0, 0, 0, METER_HEIGHT);
                greenMeterRight.setUrlAndVisibleRect(METER_OFF, RED_WIDTH, 0, TOTAL_WIDTH - RED_WIDTH, METER_HEIGHT);
            }
        }

        public int getRating() {
            return (int)item.getWeight();
        }

        public String getName() {
            return item.getDisplayName();
        }

        public CloudItem getCloudItem() {
            return item;
        }
    }

    private class DeletableTag extends DeletableWidget<Label> {

        private CloudItem i;

        public DeletableTag(CloudItem i) {
            super(new SpannedLabel(i.getDisplayName()));
            this.i = i;
            this.getElement().setAttribute("style", "color:"+i.getColorConfig()[1].getColor(1)+";");
            addRemoveButton();
        }

        public void onDelete() {
            this.fadeOut(new DataEmbededCommand<String>(i.getId()) {

                public void execute() {
                    removeItem(data);
                }
            });
        }
    }
}