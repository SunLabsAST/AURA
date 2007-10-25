/*
 * Aardvark.java
 * 
 * Created on Oct 24, 2007, 3:46:12 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

import java.net.URL;

/**
 *
 * @author plamere
 */
public interface Aardvark {
    User getUser(String openID);
    User enrollUser(String openID, URL feed);
    URL getRecommmenderFeed(String userID);
}
