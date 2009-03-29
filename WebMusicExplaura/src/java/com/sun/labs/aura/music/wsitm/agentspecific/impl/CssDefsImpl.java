/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.agentspecific.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.cobogw.gwt.user.client.ui.RoundedLinePanel;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author mailletf
 */
public class CssDefsImpl {

    public static final CssDefsImpl impl = GWT.create(CssDefsImpl.class);

    public void setRoundedPopupWidth(PopupPanel rP, int width) {}
    public String getLastFmRadioPrefix() { return ""; }
    public RoundedPanel createRoundedPanel(Widget w, int corners, int cornerHeight) {
        return new RoundedPanel(w, corners, cornerHeight);
    }
    public RoundedLinePanel createRoundedLinePanel(Widget w, int corners, int cornerHeight) {
        return new RoundedLinePanel(w, corners, cornerHeight);
    }

}
