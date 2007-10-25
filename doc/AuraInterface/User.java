/*
 * User.java
 * 
 * Created on Oct 24, 2007, 3:15:08 PM
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
public interface User extends Item {
    String getRecommenderFeedKey();
    URL getStarredItemFeedURL();
}
