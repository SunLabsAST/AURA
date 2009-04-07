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

package com.sun.labs.aura.music.wsitm.client.ui;

import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public class UpdatablePanel extends Composite {

    private Grid main;
    
    private FlowPanel refreshingPanel;

    public UpdatablePanel(Widget title, Widget widget, ClientDataManager cdm) {

        Grid g = new Grid (1,2);
        g.setStyleName("h2");
        g.setWidth("300px");
        g.setWidget(0, 0, title);

        refreshingPanel = new FlowPanel();
        refreshingPanel.add(new Image("ajax-loader-small.gif"));
        refreshingPanel.setVisible(false);

        g.getColumnFormatter().setWidth(1, "17px");
        g.setWidget(0, 1, refreshingPanel);

        main = new Grid(2, 1);
        main.setWidget(0, 0, g);
        main.setWidget(1, 0, new AnimatedUpdatableSection(widget));
        this.initWidget(main);
    }

    public void setWaitIconVisible(boolean visible) {
        refreshingPanel.setVisible(visible);
    }

    /**
     * Put the given widget as the new content.
     * @param w
     */
    public void setNewContent(Widget bottom) {
        /*((AnimatedUpdatableSection)main.getWidget(1, 0)).slideOut(Direction.UP, new DualDataEmbededCommand<Grid, Widget>(main, bottom) {
            public void execute() {
                data.setWidget(1, 0, sndData);
            }
        });*/
        main.setWidget(1, 0, bottom);
        
    }
    
    private class AnimatedUpdatableSection extends AnimatedComposite {

        public AnimatedUpdatableSection(Widget w) {
            initWidget(w);
            /*
             this.slideIn(Direction.DOWN, new Command() {
                public void execute() {}
            });
             * */
        }
    }
}
