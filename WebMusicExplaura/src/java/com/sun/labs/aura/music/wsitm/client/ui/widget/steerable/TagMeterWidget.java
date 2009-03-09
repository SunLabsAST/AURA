/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client.ui.widget.steerable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.*;
import com.sun.labs.aura.music.wsitm.client.ui.widget.DeletableWidget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sun.labs.aura.music.wsitm.client.event.DEMouseMoveHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEMouseUpHandler;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.items.steerable.WrapsCloudItem;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.SteeringSwidget.MainPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Meter cloud manipulation UI
 * @author mailletf
 */
public class TagMeterWidget extends TagWidget {

    protected static TagMeterBundle meterImgBundle =
            (TagMeterBundle) GWT.create(TagMeterBundle.class);

    private final static int MAX_TAG_VALUE = CloudItemMeter.GREEN_WIDTH;
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

    @Override
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
    public void redrawTagCloud() {
        // do something!!
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

    @Override
    public void removeItem(String itemId) {
        if (tagCloud.containsKey(itemId)) {
            mainTagPanel.remove(tagCloud.get(itemId));
            tagCloud.remove(itemId);
            updateRecommendations();
            cdm.getTagCloudListenerManager().triggerOnTagDelete(itemId);
        } else {
            Popup.showErrorPopup("", Popup.ERROR_MSG_PREFIX.NONE,
                    itemId + " is not in tagcloud", Popup.ERROR_LVL.NORMAL, null);
        }
    }

    @Override
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
        public static final int GREEN_WIDTH = 280;
        public static final int TOTAL_WIDTH = 300;

        private static final String METER_OFF = "meter-off.jpg";
        private static final String METER_ON = "meter-on.jpg";
        private static final String METER_HOVER = "meter-hover.jpg";

        private final Image STICK_METER_HOVER = meterImgBundle.meterStickHover().createImage();
        private final Image STICK_METER_OFF = meterImgBundle.meterStickOff().createImage();
        private final Image STICK_METER_ON = meterImgBundle.meterStickOn().createImage();

        private CloudItem item;
        private Grid mainPanel;
        private FocusPanel fP;
        private DeletableTag dTag;

        private Image redMeter;
        private Image greenMeterHover;
        private Image greenMeterLeft;
        private Image greenMeterRight;

        private HorizontalPanel meterHp;

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
            //stickMeter = meterImgBundle.meterStickOff().createImage();
            
            meterHp = new HorizontalPanel();
            meterHp.add(redMeter);
            meterHp.add(greenMeterHover);
            meterHp.add(greenMeterLeft);
            meterHp.add(greenMeterRight);
            meterHp.add(STICK_METER_OFF);
            fP.add(meterHp);

            fP.getElement().setAttribute("style", "margin-right: 40px");
            fP.addMouseOutHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    redrawMeter();
                }
            });

            // Handle hover events
            fP.addMouseMoveHandler(new DEMouseMoveHandler<CloudItem>(item) {
                @Override
                public void onMouseMove(MouseMoveEvent event) {
                    int y = event.getRelativeX(fP.getElement());
                    // We are hovering over the red section
                    if (y <= RED_WIDTH) {
                        swapStickButton(STICK_METER_OFF);
                        redMeter.setUrlAndVisibleRect(METER_HOVER, 0, 0, RED_WIDTH, METER_HEIGHT);
                        if (data.getWeight() > RED_WIDTH) {
                            greenMeterLeft.setVisibleRect(RED_WIDTH, 0, (int)data.getWeight() - RED_WIDTH, METER_HEIGHT);
                            greenMeterRight.setVisibleRect((int)data.getWeight(), 0, GREEN_WIDTH - (int)data.getWeight(), METER_HEIGHT);
                        } else {
                            greenMeterLeft.setVisibleRect(0, 0, 0, METER_HEIGHT);
                            greenMeterRight.setVisibleRect(RED_WIDTH, 0, GREEN_WIDTH - RED_WIDTH, METER_HEIGHT);
                        }
                    // We are hovering over the green section
                    } else if (y <= GREEN_WIDTH) {
                        if (data.getWeight() <= RED_WIDTH) {
                            redMeter.setUrlAndVisibleRect(METER_ON, 0, 0, RED_WIDTH, METER_HEIGHT);
                        } else {
                            redMeter.setUrlAndVisibleRect(METER_OFF, 0, 0, RED_WIDTH, METER_HEIGHT);
                        }

                        greenMeterHover.setVisibleRect(RED_WIDTH, 0, y - RED_WIDTH, METER_HEIGHT);
                        // If we are higher than the previous rating
                        if (y > data.getWeight()) {
                            greenMeterLeft.setVisibleRect(0, 0, 0, METER_HEIGHT);
                            greenMeterRight.setVisibleRect(y, 0, GREEN_WIDTH - y, METER_HEIGHT);
                        } else {
                            greenMeterLeft.setVisibleRect(y, 0, (int)data.getWeight() - y, METER_HEIGHT);
                            greenMeterRight.setVisibleRect((int)data.getWeight(), 0, GREEN_WIDTH - (int)data.getWeight(), METER_HEIGHT);
                        }
                    // We are hovering over the sticky section
                    } else {
                        // Take positive weight
                        int w = ((int)data.getWeight());
                        // If tag is negative and is made sticky, the weight will be
                        // switched to the default value; this is what we want to show
                        // when hovering the sticky tag
                        if (w<RED_WIDTH) {
                            w=DEFAULT_TAG_VALUE;
                        }
                        redMeter.setUrlAndVisibleRect(METER_OFF, 0, 0, RED_WIDTH, METER_HEIGHT);
                        greenMeterHover.setVisibleRect(0, 0, 0, METER_HEIGHT);
                        greenMeterLeft.setUrlAndVisibleRect(METER_ON, RED_WIDTH, 0, w - RED_WIDTH, METER_HEIGHT);
                        greenMeterRight.setUrlAndVisibleRect(METER_OFF, w, 0, GREEN_WIDTH - w, METER_HEIGHT);
                        swapStickButton(STICK_METER_HOVER);
                    }
                }
            });

            // Handle click events
            fP.addMouseUpHandler(new DEMouseUpHandler<CloudItem>(item) {
                @Override
                public void onMouseUp(MouseUpEvent event) {
                    ((FocusPanel) event.getSource()).setFocus(false);
                    double newWeight = event.getRelativeX(fP.getElement());
                    // If we clicked in the sticky zone, don't update weight;
                    // just swap sticky status
                    if (newWeight > GREEN_WIDTH) {
                        data.setSticky(!data.isSticky());
                        if (data.getWeight()<RED_WIDTH) {
                            data.setWeight(DEFAULT_TAG_VALUE);
                        }
                    } else {
                        // Only remove sticky if we click in the red zone
                        if (newWeight <= RED_WIDTH) {
                            data.setSticky(false);
                        }
                        data.setWeight(newWeight);
                    }
                    redrawMeter();
                    updateRecommendations();
                }
            });

            mainPanel.getCellFormatter().setHorizontalAlignment(0, 0, HorizontalPanel.ALIGN_LEFT);
            dTag = new DeletableTag(item, RED_WIDTH);
            mainPanel.setWidget(0, 0, dTag);

            mainPanel.getCellFormatter().setHorizontalAlignment(0, 1, HorizontalPanel.ALIGN_RIGHT);
            mainPanel.getCellFormatter().setWidth(0, 1, "150px");
            mainPanel.setWidget(0, 1, fP);

            initWidget(mainPanel);

            redrawMeter();
        }

        /**
         * Removes the last widget in the meter horizontal pannel and replaces it
         * by the passed widget
         * @param newButton Widget to add to meter
         */
        private void swapStickButton(Image newButton) {
            meterHp.remove(4);
            meterHp.add(newButton);
        }

        private void redrawMeter() {
            // If we have a positive weight
            if (item.getWeight() > RED_WIDTH) {
                redMeter.setUrlAndVisibleRect(METER_OFF, 0, 0, RED_WIDTH, METER_HEIGHT);
                greenMeterHover.setVisibleRect(0, 0, 0, METER_HEIGHT);
                greenMeterLeft.setUrlAndVisibleRect(METER_ON, RED_WIDTH, 0, (int)item.getWeight() - RED_WIDTH, METER_HEIGHT);
                greenMeterRight.setUrlAndVisibleRect(METER_OFF, (int)item.getWeight(), 0, GREEN_WIDTH - (int)item.getWeight(), METER_HEIGHT);
                if (item.isSticky()) {
                    swapStickButton(STICK_METER_ON);
                } else {
                    swapStickButton(STICK_METER_OFF);
                }
            } else {
                redMeter.setUrlAndVisibleRect(METER_ON, 0, 0, RED_WIDTH, METER_HEIGHT);
                greenMeterHover.setVisibleRect(0, 0, 0, METER_HEIGHT);
                greenMeterLeft.setUrlAndVisibleRect(METER_ON, 0, 0, 0, METER_HEIGHT);
                greenMeterRight.setUrlAndVisibleRect(METER_OFF, RED_WIDTH, 0, GREEN_WIDTH - RED_WIDTH, METER_HEIGHT);
                swapStickButton(STICK_METER_OFF);
            }
            dTag.updateColor();
        }

        public int getRating() {
            return (int)item.getWeight();
        }

        public String getName() {
            return item.getDisplayName();
        }

        @Override
        public CloudItem getCloudItem() {
            return item;
        }
    }

    private class DeletableTag extends DeletableWidget<Label> {

        private final int negativeWidth;
        private String prevColor= null;
        private CloudItem i;

        public DeletableTag(CloudItem i, int negativeWidth) {
            super(new SpannedLabel(i.getDisplayName()));
            this.i = i;
            this.negativeWidth=negativeWidth;
            updateColor();
            addRemoveButton();
        }

        public void updateColor() {
            int tw = (int)i.getWeight();
            if (tw<negativeWidth) {
                tw = -1;
            }
            prevColor = TagDisplayLib.setColorToElem(this.getWidget(), 1, tw, prevColor, i.getTagColorType());
        }

        @Override
        public void onDelete() {
            /*
             this.fadeOut(new DataEmbededCommand<String>(i.getId()) {

                public void execute() {
                    removeItem(data);
                }
            });
            */
            removeItem(i.getId());
        }
    }

    public interface TagMeterBundle extends ImageBundle {

        @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-stick-hover.jpg")
        public AbstractImagePrototype meterStickHover();

        @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-stick-off.jpg")
        public AbstractImagePrototype meterStickOff();

        @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-stick-on.jpg")
        public AbstractImagePrototype meterStickOn();
    }
}