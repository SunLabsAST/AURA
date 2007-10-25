/*
 * Item.java
 * 
 * Created on Oct 24, 2007, 3:04:09 PM
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
public interface Item {
    public long getID();
    public String getKey();
    public String getType();

    public String getField(String name);
    public void setField(String name, String value);
    public List<Attention> getAttentionData();
}
