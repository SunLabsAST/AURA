/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.ui.Composite;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;

/**
 *
 * @author mailletf
 */
public abstract class LoginListener extends Composite {

    public abstract void onLogin(ListenerDetails lD);
    public abstract void onLogout ();

}
