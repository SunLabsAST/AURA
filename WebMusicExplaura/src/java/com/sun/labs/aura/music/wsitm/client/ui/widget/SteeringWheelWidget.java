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

    private String steerImage;
    private String steerImageHover;

    private MouseOverHandler mOverH;
    private MouseOutHandler mOutH;
    private ClickHandler cH;

    public enum wheelSize {
        SMALL,
        BIG
    }

    public SteeringWheelWidget(wheelSize size, ClickHandler cH) {

        if (size == wheelSize.SMALL) {
            steerImage = "steering-20.gif";
            steerImageHover = "steering-hover-20.gif";
        } else {
            steerImage = "steering-30.gif";
            steerImageHover = "steering-hover-30.gif";
        }
        Image.prefetch(steerImageHover);
        Image.prefetch(steerImage);

        this.setUrl(steerImage);
        addStyleName("pointer");

        this.cH = cH;
        this.addClickHandler(this.cH);

        this.mOverH = new DEMouseOverHandler<SteeringWheelWidget>(this) {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                data.setUrl(steerImageHover);
            }
        };

        this.mOutH = new DEMouseOutHandler<SteeringWheelWidget>(this) {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                data.setUrl(steerImage);
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