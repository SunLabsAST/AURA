/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author mailletf
 */
public abstract class DataEmbededAsyncCallback<T> implements AsyncCallback<T> {

    protected T data;

    public DataEmbededAsyncCallback(T data) {
        super();
        this.data = data;
    }

}
