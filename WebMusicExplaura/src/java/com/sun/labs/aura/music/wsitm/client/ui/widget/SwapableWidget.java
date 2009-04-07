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

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedFlowPanel;

/**
 *
 * @author mailletf
 */
public class SwapableWidget<T extends Widget, V extends Widget> extends Composite {

    private T w1;
    private V w2;
    
    private SpannedFlowPanel fP;

    private LoadableWidget loadedWidget;

    public enum LoadableWidget {
        W1,
        W2
    }

    public SwapableWidget(T w1, V w2) {
        this.w1 = w1;
        this.w2 = w2;
        
        fP = new SpannedFlowPanel();
        fP.add(w1);
        loadedWidget = LoadableWidget.W1;
        
        initWidget(fP);

        // Register for onclick events
        sinkEvents(Event.ONCLICK);
    }

    public void showWidget(LoadableWidget widgetToLoad) {
        if (widgetToLoad != loadedWidget) {
            fP.clear();
            if (widgetToLoad == LoadableWidget.W1) {
                fP.add(w1);
            } else {
                fP.add(w2);
            }
            loadedWidget = widgetToLoad;
        }
    }

    public T getWidget1() {
        return w1;
    }

    public V getWidget2() {
        return w2;
    }

    public LoadableWidget getLoadedWidgetName() {
        return loadedWidget;
    }

    public Widget getLoadedWidget() {
        if (loadedWidget==LoadableWidget.W1) {
            return w1;
        } else {
            return w2;
        }
    }

    public void swapWidget() {
        if (getLoadedWidgetName()==LoadableWidget.W1) {
            showWidget(LoadableWidget.W2);
        } else {
            showWidget(LoadableWidget.W1);
        }
    }
}
