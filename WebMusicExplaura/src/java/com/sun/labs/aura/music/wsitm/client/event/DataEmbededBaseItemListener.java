/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;

/**
 *
 * @author mailletf
 */
public class DataEmbededBaseItemListener<T> extends BaseItemListenerAdapter {

    protected T data;
    
    public DataEmbededBaseItemListener(T data) {
        this.data = data;
    }    
}
