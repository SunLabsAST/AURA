/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.Command;

/**
 *
 * @author mailletf
 */
public abstract class DECommand <T> implements Command {

    protected T data;

    public DECommand(T data) {
        this.data = data;
    }
}
