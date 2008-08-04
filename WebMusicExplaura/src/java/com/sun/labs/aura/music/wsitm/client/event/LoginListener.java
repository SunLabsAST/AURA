/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;

/**
 *
 * @author mailletf
 */
public interface LoginListener extends WebListener  {

    public abstract void onLogin(ListenerDetails lD);
    public abstract void onLogout ();

}
