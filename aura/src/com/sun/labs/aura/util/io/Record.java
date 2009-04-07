/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

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
