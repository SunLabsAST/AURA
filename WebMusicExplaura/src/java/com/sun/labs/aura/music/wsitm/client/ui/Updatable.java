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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import java.util.ArrayList;

/**
 *
 * @author mailletf
 */
public abstract class Updatable<T> extends Composite {

    private Grid main;
    protected T data;

    public Updatable(Widget title, Widget widget, ClientDataManager cdm, T data) {
        this(title, widget, cdm);
        this.data = data;
    }
        
    public Updatable(Widget title, Widget widget, ClientDataManager cdm) {
        main = new Grid(2, 1);
        main.setWidget(0, 0, title);
        main.setWidget(1, 0, widget);
        cdm.addUpdatableWidget(this);
        this.initWidget(main);
    }
    
    public Updatable(Widget title, ArrayList<ScoredC<ArtistCompact>> aCList, ClientDataManager cdm, T data) {
        
        this.data = data;
        
        main = new Grid(2, 1);
        main.setWidget(0, 0, title);
        update(aCList);
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

    /**
     * Put the given widget as the bottom content
     * @param bottom
     */
    protected void setNewContent(Widget bottom) {
        main.setWidget(1, 0, bottom);
    }
    
    public abstract void update(ArrayList<ScoredC<ArtistCompact>> aCList);

    private class AnimatedUpdatableSection extends AnimatedComposite {

        public AnimatedUpdatableSection(Widget w) {
            initWidget(w);
        }
    }
}
