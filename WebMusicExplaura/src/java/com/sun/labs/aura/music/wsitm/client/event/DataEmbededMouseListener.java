/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.user.client.ui.MouseListener;

/**
 * @deprecated 
 * @author mailletf
 */
public abstract class DataEmbededMouseListener<T> implements MouseListener {

    protected T data;

    /**
     * @deprecated 
     * @param data
     */
    public DataEmbededMouseListener(T data) {
        super();
        this.data = data;
    }
}