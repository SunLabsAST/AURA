/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;


import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.CloudItemDependentSharedMenu;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidgetContainer;
import java.util.HashMap;

/**
 *  All cloud element context menus will extend this class. It will provide the
 * default context menu options (ie make sticky, negative)
 * @author mailletf
 */
public class SharedSteeringCIMenu extends ContextMenu implements CloudItemDependentSharedMenu {

    protected CloudItem cI;
    protected ClientDataManager cdm;
    protected TagWidgetContainer twc;

    private Label stickyLbl = new Label();
    private Label negLbl = new Label();

    private final String STICK_STRING="Make sticky";
    private final String UNSTICK_STRING="Unstick";

    private final String NEG_STRING="Make negative";
    private final String POS_STRING="Make positive";

    public SharedSteeringCIMenu(ClientDataManager tCdm, TagWidgetContainer tTwc, CloudItem.CloudItemType cit) {

        super();
        this.cdm = tCdm;
        this.twc = tTwc;

        addElement("Delete", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                twc.removeItem(cI.getId());
            }
        });
        addElement(stickyLbl, new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cI.setSticky(!cI.isSticky());
                twc.redrawTagCloud();
            }
        });
        addElement(negLbl, new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cI.setWeight(-1 * cI.getWeight());
                twc.redrawTagCloud();
            }
        });

        // Add menu items for specific CloudItem
        if (cit == CloudItem.CloudItemType.ARTIST) {

            super.addSeperator();
            addElement("Expand artist", new ClickListener() {

                @Override
                public void onClick(Widget sender) {

                    HashMap<String, CloudItem> itemsMap = new HashMap<String, CloudItem>();

                    double maxVal = 0;
                    for (CloudItem item : cI.getContainedItems()) {
                        if (item.getWeight() > maxVal) {
                            maxVal = item.getWeight();
                        }
                    }

                    for (CloudItem item : cI.getContainedItems()) {
                        item.setWeight(item.getWeight() / maxVal * cI.getWeight());
                        itemsMap.put(item.getId(), item);
                    }

                    twc.addItems(itemsMap, TagWidget.ITEM_WEIGHT_TYPE.ABSOLUTE);
                    twc.removeItem(cI.getId());
                }
            });

            addElement("View tag cloud", new ClickListener() {

                @Override
                public void onClick(Widget sender) {
                    TagDisplayLib.showTagCloud("Tag cloud for " + cI.getDisplayName(),
                            cI.getTagMap(), TagDisplayLib.ORDER.SHUFFLE, cdm);
                }
            });

        }

    }

    @Override
    public void showAt(Event e, CloudItem cI) {
        this.cI = cI;

        // Update menu labels text before showing the menu
        if (cI.isSticky()) {
            stickyLbl.setText(UNSTICK_STRING);
        } else {
            stickyLbl.setText(STICK_STRING);
        }
        if (cI.getWeight()<0) {
            negLbl.setText(POS_STRING);
        } else {
            negLbl.setText(NEG_STRING);
        }

        super.showAt(e);
    }
}
