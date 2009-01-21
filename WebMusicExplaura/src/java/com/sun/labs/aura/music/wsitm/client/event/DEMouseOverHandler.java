/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.event.dom.client.MouseOverHandler;

/**
 *
 * @author mailletf
 */
public abstract class DEMouseOverHandler<T> implements MouseOverHandler {

    protected T data;

    public DEMouseOverHandler(T data) {
        super();
        this.data = data;
    }

}
