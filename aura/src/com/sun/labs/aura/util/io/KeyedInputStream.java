package com.sun.labs.aura.util.io;

import java.io.IOException;

/**
 * An input stream of keyed records.
 * @param K the type of the key for the records.  Must extend Serializable and Comparable
 * @param V the type of the value for the records.  Must extend Serializable.
 */
public interface KeyedInputStream<K, V> {
    public Record<K, V> read() throws IOException;
    public boolean isSorted();
    public void reset() throws IOException;
}