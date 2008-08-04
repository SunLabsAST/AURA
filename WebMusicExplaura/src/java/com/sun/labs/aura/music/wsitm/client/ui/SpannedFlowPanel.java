/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 *
 * @author mailletf
 */
public class SpannedFlowPanel extends FlowPanel {

    public SpannedFlowPanel() {
        setElement(DOM.createSpan());
    }
}
