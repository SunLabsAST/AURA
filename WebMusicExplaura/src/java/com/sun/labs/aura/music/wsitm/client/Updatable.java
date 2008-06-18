/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;

/**
 *
 * @author mailletf
 */
public abstract class Updatable extends Composite {

    private Grid main;
    protected String extraParam;

    public Updatable(Widget title, Widget widget, ClientDataManager cdm, String extraParam) {
        this(title, widget, cdm);
        this.extraParam = extraParam;
    }
        
    public Updatable(Widget title, Widget widget, ClientDataManager cdm) {
        main = new Grid(2, 1);
        main.setWidget(0, 0, title);
        main.setWidget(1, 0, widget);
        cdm.addUpdatableWidget(this);
        this.initWidget(main);
    }
    
    public Updatable(Widget title, ArtistDetails aD, ClientDataManager cdm, String extraParam) {
        
        this.extraParam=extraParam;
        
        main = new Grid(2, 1);
        main.setWidget(0, 0, title);
        update(aD);
        cdm.addUpdatableWidget(this);
        this.initWidget(main);
    }

    public void displayWaitIcon() {
        main.setWidget(1, 0, new Image("ajax-loader.gif"));
        main.getCellFormatter().getElement(1, 0).setAttribute("align", "center");
    }

    /**
     * Put the given widget as the new content. Should be called by overloaded
     * method update
     * @param w
     */
    protected void setNewContent(Widget top, Widget bottom) {
        main.setWidget(0, 0, top);
        main.setWidget(1, 0, bottom);
    }
    
    public abstract void update(ArtistDetails aD);
    
}
