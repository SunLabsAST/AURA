/*
 * TagTree.java
 *
 * Created on April 9, 2007, 1:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class TagTree implements IsSerializable {
   private String id;
   private String name;
   private TagTree[] children;
    /**
     * Creates a new instance of TagTree
     */
    public TagTree(String id, String name, TagTree[] children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }
    
    public TagTree() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TagTree[] getChildren() {
        return children;
    }
}
