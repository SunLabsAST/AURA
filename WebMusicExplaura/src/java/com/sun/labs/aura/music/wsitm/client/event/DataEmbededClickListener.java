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
 * @deprecated Use DEClickHandler instead
 */
public abstract class DataEmbededClickListener<T> implements ClickListener {

    protected T data;

    public DataEmbededClickListener(T data) {
        super();
        this.data = data;
    }

    /**
     * @deprecated
     */
    public abstract void onClick(Widget sender);
}
