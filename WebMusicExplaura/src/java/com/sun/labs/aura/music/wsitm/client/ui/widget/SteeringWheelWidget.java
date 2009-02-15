/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.event.DEMouseOutHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEMouseOverHandler;
import com.sun.labs.aura.music.wsitm.client.ui.bundles.ArtistRelatedBundle;

/**
 *
 * @author mailletf
 */
public class SteeringWheelWidget extends Image {

    private static ArtistRelatedBundle steerImgBundle = (ArtistRelatedBundle) GWT.create(ArtistRelatedBundle.class);
    private AbstractImagePrototype steerImage;
    private AbstractImagePrototype steerImageHover;

    private MouseOverHandler mOverH;
    private MouseOutHandler mOutH;
    private ClickHandler cH;

    public enum wheelSize {
        SMALL,
        BIG
    }

    public SteeringWheelWidget(wheelSize size, ClickHandler cH) {

        if (size == wheelSize.SMALL) {
            steerImage = steerImgBundle.steering20();
            steerImageHover = steerImgBundle.steeringHover20();
        } else {
            steerImage = steerImgBundle.steering30();
            steerImageHover = steerImgBundle.steeringHover30();
        }
        
        steerImage.applyTo(this);
        addStyleName("pointer");

        this.cH = cH;
        this.addClickHandler(this.cH);

        this.mOverH = new DEMouseOverHandler<SteeringWheelWidget>(this) {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                steerImageHover.applyTo(data);
            }
        };

        this.mOutH = new DEMouseOutHandler<SteeringWheelWidget>(this) {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                steerImage.applyTo(data);
            }
        };
    }

    public ClickHandler getClickHandler() {
        return this.cH;
    }

    public MouseOutHandler getMouseOutHandler() {
        return mOutH;
    }

    public MouseOverHandler getMouseOverHandler() {
        return mOverH;
    }
}