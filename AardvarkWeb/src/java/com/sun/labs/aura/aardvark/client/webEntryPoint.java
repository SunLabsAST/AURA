/*
 * webEntryPoint.java
 *
 * Created on November 4, 2007, 6:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 *
 * @author plamere
 */
public class webEntryPoint implements EntryPoint {
    
    /** Creates a new instance of webEntryPoint */
    public webEntryPoint() {
    }
    
    /**
     * The entry point method, called automatically by loading a module
     * that declares an implementing class as an entry-point
     */
    public void onModuleLoad() {
        RootPanel.get().add(new MainPanel());
    }
}
