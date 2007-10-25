/*
 * RecommendationManager.java
 * 
 * Created on Oct 24, 2007, 3:33:58 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

import java.util.List;

/**
 *
 * @author plamere
 */
public interface RecommendationManager {
    List<Entry> getRecommendations(User user);
}
