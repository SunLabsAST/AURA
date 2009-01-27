/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
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

        addElement("Start new steerable with artist's top tags", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.setSteerableReset(true);
                History.newItem("steering:" + currArtist.getId());
            }
        });
        addElement("Start new steerable with artist", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.setSteerableReset(true);
                History.newItem("steering:art:" + currArtist.getId());
                }
            });
        addElement("Add artist's top tags", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.getSteerableTagCloudExternalController().addTags(currArtist.getDistinctiveTags());
            }
        });
        addElement("Add artist", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cdm.getSteerableTagCloudExternalController().addArtist(currArtist);
            }
        });       
    }
    
    @Override
    public void showAt(Event e, ArtistCompact currArtist) {
        this.currArtist = currArtist;
        super.showAt(e);
    }

}
