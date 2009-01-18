/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.event.dom.client.MouseOutHandler;

/**
 *
 * @author mailletf
 */
public abstract class DEMouseOutHandler<T> implements MouseOutHandler {

    protected T data;

    public DEMouseOutHandler(T data) {
        super();
        this.data = data;
    }

}
