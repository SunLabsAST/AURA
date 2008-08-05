/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

/**
 *
 * @author mailletf
 */
public abstract class DualDataEmbededClickListener<T, V> extends DataEmbededClickListener<T> {

    protected V sndData;

    public DualDataEmbededClickListener(T data, V sndData) {
        super(data);
        this.sndData = sndData;
    }

}
