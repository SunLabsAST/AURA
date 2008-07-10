/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

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

    private Grid mainImage;

    public enum wheelSize {
        SMALL,
        BIG
    }

    public SteeringWheelWidget(wheelSize size, ClickListener cL) {
        if (size == wheelSize.SMALL) {
            steerImage = steerImageSmall;
            steerImageHover = steerImageHoverSmall;
        } else {
            steerImage = steerImageBig;
            steerImageHover = steerImageHoverBig;
        }
        addClickListener(cL);
        setUrl(steerImage);
        setTitle("Get steerable recommendations starting with this artist");
        addStyleName("pointer");

        this.addMouseListener(new MouseListener() {

            public void onMouseEnter(Widget arg0) {
                setUrl(steerImageHover);
            }

            public void onMouseLeave(Widget arg0) {
                setUrl(steerImage);
            }

            public void onMouseDown(Widget arg0, int arg1, int arg2) {}
            public void onMouseMove(Widget arg0, int arg1, int arg2) {}
            public void onMouseUp(Widget arg0, int arg1, int arg2) {}
        });
    }

}
