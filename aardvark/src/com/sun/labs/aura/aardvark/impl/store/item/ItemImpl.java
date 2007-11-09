/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.store.item;

import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation of a generic Item.
 * 
 * @author ja151348
 */
public abstract class ItemImpl implements Item {

    protected long itemID;
    
    protected String key;
    
    protected Map<String,String> fields = new HashMap();
    
    protected List<Attention> attn = new ArrayList();
    
    public ItemImpl(long itemID, String key) {
        this.itemID = itemID;
        this.key = key;
    }
    

    public long getID() {
        return itemID;
    }

    public String getKey() {
        return key;
    }
    
    public static String getType() {
        return Item.ITEM_TYPE;
    }
        

    public String getField(String name) {
        return fields.get(name);
    }

    public void setField(String name, String value) {
        fields.put(name, value);
    }

    public List<Attention> getAttentionData() {
        List ret = new ArrayList();
        ret.addAll(attn);
        return ret;
    }
    
    public void attend(Attention a) {
        if (!attn.contains(a)) {
            attn.add(a);
        }
    }
    
    public boolean equals(Object o) {
        if (o instanceof ItemImpl) {
            ItemImpl other = (ItemImpl)o;
            if (other.getKey().equals(getKey())) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return getClass().getSimpleName() + ": key[" + key + "]";
    }

}
