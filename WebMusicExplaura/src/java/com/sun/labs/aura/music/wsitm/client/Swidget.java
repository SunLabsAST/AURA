/*
 * Swidget.java
 *
 * Created on March 7, 2007, 5:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;
import com.google.gwt.user.client.ui.Composite;

/**
 *
 * @author plamere
 */
public abstract class Swidget extends Composite {
    private String name;
  
    
    public Swidget(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
