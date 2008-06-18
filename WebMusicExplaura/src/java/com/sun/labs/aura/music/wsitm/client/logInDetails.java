/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;


/**
 *
 * @author mailletf
 */
public class logInDetails implements IsSerializable {

    public ItemInfo[] userTags;
    public ArtistDetails[] favArtistDetails;
    
}
