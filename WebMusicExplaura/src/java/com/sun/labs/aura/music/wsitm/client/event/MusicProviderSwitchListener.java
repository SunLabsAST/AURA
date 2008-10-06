/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.sun.labs.aura.music.wsitm.client.ui.widget.PlayButton.MusicProviders;

/**
 *
 * @author mailletf
 */
public interface MusicProviderSwitchListener extends WebListener  {

    public abstract void onSwitch(MusicProviders newMp);

}

