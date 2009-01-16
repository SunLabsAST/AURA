/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

/**
 *
 * @author mailletf
 */
public abstract class DDECommand<T, V> extends DECommand<T> {

    protected V sndData;
    
    public DDECommand(T data, V sndData) {
        super(data);
        this.sndData = sndData;
    }
  
}
