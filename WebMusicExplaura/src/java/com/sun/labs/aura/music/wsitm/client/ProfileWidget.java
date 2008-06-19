/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mailletf
 */
public class ProfileWidget extends Swidget {

    private ClientDataManager cdm;

    public ProfileWidget(ClientDataManager cdm) {
        super("User preferences");

        this.cdm=cdm;
        initWidget(getWidget());
    }

    public List<String> getTokenHeaders() {

        List<String> l = new ArrayList<String>();
        l.add("userpref:");
        return l;
    }

    public Widget getWidget() {
        return new Label("profileeeeee");
    }

}
