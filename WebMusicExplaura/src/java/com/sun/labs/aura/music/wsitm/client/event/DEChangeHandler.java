/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.event.dom.client.ChangeHandler;

/**
 *
 * @author mailletf
 */
public abstract class DEChangeHandler<T> implements ChangeHandler {

    protected T data;

    public DEChangeHandler(T data) {
        super();
        this.data = data;
    }

}
