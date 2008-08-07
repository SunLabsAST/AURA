/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

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
        
        initWidget(fP);
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


}