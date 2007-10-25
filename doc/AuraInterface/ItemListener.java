/*
 * ItemListener.java
 * 
 * Created on Oct 24, 2007, 3:58:59 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

/**
 *
 * @author plamere
 */
public interface ItemListener {
    void created(Item[] item);
    void changed(Item[] item);
    void deleted(Item[] item);
}
