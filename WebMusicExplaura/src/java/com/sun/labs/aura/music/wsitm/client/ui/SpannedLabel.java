/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

/**
 *
 * @author mailletf
 */
public class SpannedLabel extends Label {

    public SpannedLabel(String txt) {
        setElement(DOM.createSpan());
        setStyleName("gwt-Label");
        setText(txt);
    }
}
