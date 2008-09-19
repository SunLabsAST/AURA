/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.user.client.ui.Label;

/**
 * A GWT Label that also can set its own CSS style
 */
public class StyleLabel extends Label {
    public StyleLabel(String text, String style) {
        super(text);
        setStylePrimaryName(style);
    }
}
