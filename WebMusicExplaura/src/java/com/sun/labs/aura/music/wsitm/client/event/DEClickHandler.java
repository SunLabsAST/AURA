/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 *
 * @author mailletf
 */
public abstract class DEClickHandler<T> implements ClickHandler {

    protected T data;
    
    public DEClickHandler(T data) {
        super();
        this.data = data;
    }

    @Override
    public abstract void onClick(ClickEvent event);

}
