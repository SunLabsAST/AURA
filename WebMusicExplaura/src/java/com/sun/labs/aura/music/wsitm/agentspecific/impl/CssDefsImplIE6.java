/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.agentspecific.impl;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 *
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

}
