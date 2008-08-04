/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

/**
 *
 * @author mailletf
 */
public abstract class DualDataEmbededCommand<T, V> extends DataEmbededCommand<T> {

    protected V sndData;
    
    public DualDataEmbededCommand(T data, V sndData) {
        super(data);
        this.sndData = sndData;
    }
  
}
