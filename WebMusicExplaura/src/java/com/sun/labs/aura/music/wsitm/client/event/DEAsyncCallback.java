/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @param <T> Type of data to store in object
 * @param <R> Callback return type
 * @author mailletf
 */
public abstract class DEAsyncCallback<T, R> implements AsyncCallback<R> {

    protected T data;

    public DEAsyncCallback(T data) {
        super();
        this.data = data;
    }

}
