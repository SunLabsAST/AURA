/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.agentspecific.impl;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.cobogw.gwt.user.client.ui.RoundedLinePanel;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 * Browser specific functions for Internet Explorer
 * @author mailletf
 */
public class CssDefsImplIE6 extends CssDefsImpl {

    @Override
    public void setRoundedPopupWidth(PopupPanel rP, int width) {
        rP.getElement().getStyle().setPropertyPx("width", width+7);
    }

    @Override
    public String getLastFmRadioPrefix() {

        final String prefix = "WebMusicExplaura";

        String currUrl = Window.Location.getHref();
        return currUrl.substring(0, currUrl.indexOf(prefix) + prefix.length()) + "/";
    }
    
    @Override
    /**
     * An unknown bug causes some white spaces to be added between the divs created
     * by the RoundedPanel class. IE will not display white spaces before the third
     * div so limit the corner height to 2.
     */
    public RoundedPanel createRoundedPanel(Widget w, int corners, int cornerHeight) {
        return new RoundedPanel(w, corners, 2);
    }
    @Override
    public RoundedLinePanel createRoundedLinePanel(Widget w, int corners, int cornerHeight) {
        return new RoundedLinePanel(w, corners, 2);
    }

}
