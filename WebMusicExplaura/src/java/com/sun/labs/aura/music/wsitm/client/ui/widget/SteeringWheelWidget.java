/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.event.DEMouseOutHandler;
import com.sun.labs.aura.music.wsitm.client.event.DEMouseOverHandler;

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
            steerImage = "steering-20.png";
            steerImageHover = "steering-hover-20.gif";
        } else {
            steerImage = "steering-30.png";
            steerImageHover = "steering-hover-30.gif";
        }
        Image.prefetch(steerImageHover);
        Image.prefetch(steerImage);

        this.setUrl(steerImage);
        addStyleName("pointer");

        this.cH = cH;
        this.addClickHandler(this.cH);
        /*
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
         * */
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
