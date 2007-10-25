/*
 * ItemStore.java
 * 
 * Created on Oct 24, 2007, 2:55:20 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

/**
 *
 * @author plamere
 */
public interface ItemStore {
    Item create(String key);
    long getID(String key);
    Item get(long id);
    Item get(String key);
    void put(Item item);

    void addItemListener(String type, ItemListener l);
    void removeItemListener(String type, ItemListener l);
}