/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.Map;


/**
 *
 * @author mailletf
 */
public class ListenerDetails implements IsSerializable {

    public ArtistDetails[] favArtistDetails;

    public String openID;
    public boolean loggedIn=false;

    public String realName;
    public String nickName;
    public String birthDate;
    public String email;
    public String state;
    public String country;
    public String gender;
    public String language;

    public String lastfmUser;
    public String pandoraUser;

    public ArtistCompact[] recommendations;
    public ItemInfo[] userTagCloud;

}
