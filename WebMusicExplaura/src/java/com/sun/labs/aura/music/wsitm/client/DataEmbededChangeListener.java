/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.ui.ChangeListener;

/**
 *
 * @author mailletf
 */
public abstract class DataEmbededChangeListener <T> implements ChangeListener {

    protected T data;

    public DataEmbededChangeListener(T data) {
        super();
        this.data = data;
    }
}
