/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

/**
 *
 * @author mailletf
 */
public class DualDataEmbededBaseItemListener<T, V> extends DataEmbededBaseItemListener<T> {

    protected V sndData;
    
    public DualDataEmbededBaseItemListener(T data, V sndData) {
        super(data);
        this.sndData = sndData;
    }
    
}
