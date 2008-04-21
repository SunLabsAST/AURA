/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

/**
 * A record in a keyed input or output stream.
 * @param K the type of the key in the record.  This must extend Comparable and Serializable
 * @param V the value type of the key in the record.  This must extend Serializable.
 */
public class Record<K,V> implements Comparable<Record<K,V>> {
    
    private K key;
    
    private V value;
    
    public Record(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    public K getKey() {
        return key;
    }
    
    public V getValue() {
        return value;
    }
    
    public int compareTo(Record<K, V> o) {
        return ((Comparable) key).compareTo(o.key);
    }

}
