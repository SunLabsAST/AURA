/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.CloudItemDependentSharedMenu;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidgetContainer;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public class SharedSteeringArtistMenu extends ContextMenu implements CloudItemDependentSharedMenu {

    protected CloudItem cI;
    protected ClientDataManager cdm;
    protected TagWidgetContainer twc;

    public SharedSteeringArtistMenu(ClientDataManager tCdm, TagWidgetContainer tTwc) {

        super();
        this.cdm = tCdm;
        this.twc = tTwc;

        addElement("Expand artist", new DataEmbededClickListener<SharedSteeringArtistMenu>(this) {

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
                    item.setWeight( item.getWeight() / maxVal * cI.getWeight() );
                    itemsMap.put(item.getId(), item);
                }

                twc.addItems(itemsMap, TagWidget.ITEM_WEIGHT_TYPE.ABSOLUTE);
                twc.removeItem(cI.getId());
            }
        });
        
        addElement("View tag cloud", new DataEmbededClickListener<SharedSteeringArtistMenu>(this) {

            @Override
            public void onClick(Widget sender) {
                TagDisplayLib.showTagCloud("Tag cloud for "+cI.getDisplayName(), 
                        cI.getTagMap(), TagDisplayLib.ORDER.SHUFFLE, cdm);
            }

        });
    }

    public void showAt(Event e, CloudItem cI) {
        this.cI = cI;
        super.showAt(e);
    }

}
