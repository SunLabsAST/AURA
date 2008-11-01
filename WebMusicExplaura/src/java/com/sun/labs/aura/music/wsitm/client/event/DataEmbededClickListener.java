/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public abstract class DataEmbededClickListener<T> implements ClickListener {

    protected T data;

    public DataEmbededClickListener(T data) {
        super();
        this.data = data;
    }

    public abstract void onClick(Widget sender);
}
