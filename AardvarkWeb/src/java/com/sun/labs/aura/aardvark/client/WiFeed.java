/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class WiFeed implements IsSerializable {
    private String key;
    private String name;
    private long id;
    private String type;

    public WiFeed(String key, String name, long id, String type) {
        this.key = key;
        this.id = id;
        this.type = type;
        if (name == null) {
            name = key;
        }
        this.name = name;
    }

    public WiFeed() {
    }
    
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }



}
