package com.sun.labs.aura.util.io;

import java.io.IOException;

/**
 */
public interface KeyedOutputStream<K, V> {
    public void write(Record<K, V> record) throws IOException;
    public void write(K key, V value) throws IOException;
    public void close() throws java.io.IOException;
}
