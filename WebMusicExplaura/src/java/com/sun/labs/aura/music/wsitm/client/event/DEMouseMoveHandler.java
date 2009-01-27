/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.event.dom.client.MouseMoveHandler;

/**
 *
 * @author mailletf
 */
public abstract class DEMouseMoveHandler<T> implements MouseMoveHandler {

    protected T data;

    public DEMouseMoveHandler(T data) {
        super();
        this.data = data;
    }

}
