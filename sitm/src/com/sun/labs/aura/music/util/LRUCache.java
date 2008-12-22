/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private int maxSize;

    public LRUCache(int maxSize) {
	this.maxSize = maxSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	return size() > maxSize;
    }
}
