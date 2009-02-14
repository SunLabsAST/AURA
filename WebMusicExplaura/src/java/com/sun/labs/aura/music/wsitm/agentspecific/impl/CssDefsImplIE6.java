/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.agentspecific.impl;

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

}
