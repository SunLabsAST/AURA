/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 *
 * @author plamere
 */
class StatusWidget extends Composite {

    Label label = new Label();

    StatusWidget() {
        label.setStyleName("status");
        initWidget(label);
    }

    void info(String msg) {
        label.setStyleName("status");
        label.setText(msg);
    }

    void processing() {
        info("Processing ....");
    }

    void clear() {
        info("");
    }

    void error(String msg) {
        label.setStyleName("statusError");
        label.setText(msg);
    }
}
