/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;

/**
 *
 * @author mailletf
 */
public class SteeringWheelWidget extends Image {

    private static final String steerImageBig = "steering-30.gif";
    private static final String steerImageSmall = "steering-20.gif";

    private static final String steerImageHoverBig = "steering-hover-30.gif";
    private static final String steerImageHoverSmall = "steering-hover-20.gif";

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
            steerImage = steerImageSmall;
            steerImageHover = steerImageHoverSmall;
        } else {
            steerImage = steerImageBig;
            steerImageHover = steerImageHoverBig;
        }
        
        setUrl(steerImage);
        addStyleName("pointer");

        this.cH = cH;
        this.addClickHandler(this.cH);

        this.mOverH = new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                setUrl(steerImageHover);
            }
        };

        this.mOutH = new MouseOutHandler () {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                setUrl(steerImage);
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