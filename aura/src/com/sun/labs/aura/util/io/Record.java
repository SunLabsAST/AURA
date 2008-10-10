package com.sun.labs.aura.util.io;

/**
 * A record in a keyed input or output stream.
 * 
 * @param K the type of the key in the record.  This must extend Comparable and Serializable
 * @param V the value type of the key in the record.  This must extend Serializable.
 */
public class Record<K,V> implements Comparable<Record<K,V>> {
    
    protected enum Type {
        STRING, INTEGER, LONG, FLOAT, DOUBLE, OBJECT
    };

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
    
    public int compareTo(Record<K, V> other) {
        return ((Comparable)key).compareTo(other.key);
    }
    
    public boolean equals(Object o) {
        if (o instanceof Record) {
            Record or = (Record)o;
            return (key.equals(or.key) &&
                    value.equals(or.value));
        }
        return false;
    }
    
    public int hashCode() {
        return key.hashCode();
    }
    
    public String toString() {
        return String.format("key: '%s' value: '%s'", key.toString(), value.toString()); 
    }

    public Type getKeyType() {
        return getType(key);
    }
    
    public Type getValueType() {
        return getType(value);
    }
    
    public static Type getType(Object o) {
        if(o instanceof String) {
            return Type.STRING;
        }
        
        if(o instanceof Integer) {
            return Type.INTEGER;
        }
        
        if(o instanceof Long) {
            return Type.LONG;
        }
        
        if(o instanceof Float) {
            return Type.FLOAT;
        }
        
        if(o instanceof Double) {
            return Type.DOUBLE;
        }
        
        return Type.OBJECT;
    }

}
