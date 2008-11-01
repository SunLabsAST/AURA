/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.event.DualDataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedSteeringMenu extends ContextMenu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedSteeringMenu(ClientDataManager tCdm) {

        super();

        this.cdm = tCdm;

        addElement("Start new steerable with artist's top tags", new DataEmbededClickListener<SharedSteeringMenu>(this) {

            @Override
            public void onClick(Widget sender) {
                cdm.setSteerableReset(true);
                History.newItem("steering:" + data.currArtist.getId());
            }
        });
        addElement("Start new steerable with artist",
                new DualDataEmbededClickListener<ClientDataManager, SharedSteeringMenu>(cdm, this) {

                    @Override
                    public void onClick(Widget sender) {
                        cdm.setSteerableReset(true);
                        History.newItem("steering:art:" + sndData.currArtist.getId());
                    }
                });
        addElement("Add artist's top tags", new DualDataEmbededClickListener<ClientDataManager, SharedSteeringMenu>(cdm, this) {

            @Override
            public void onClick(Widget sender) {
                data.getSteerableTagCloudExternalController().addTags(sndData.currArtist.getDistinctiveTags());
            }
        });
        addElement("Add artist", new DualDataEmbededClickListener<ClientDataManager, SharedSteeringMenu>(cdm, this) {

            @Override
            public void onClick(Widget sender) {
                data.getSteerableTagCloudExternalController().addArtist(sndData.currArtist);
            }
        });       
    }
    
    public void showAt(Event e, ArtistCompact currArtist) {
        this.currArtist = currArtist;
        super.showAt(e);
    }

}
