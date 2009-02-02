/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.event.dom.client.MouseUpHandler;

/**
 *
 * @author mailletf
 */
public abstract class DEMouseUpHandler<T> implements MouseUpHandler {

    protected T data;

    public DEMouseUpHandler(T data) {
        super();
        this.data = data;
    }

}
