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
