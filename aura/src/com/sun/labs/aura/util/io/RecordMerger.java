package com.sun.labs.aura.util.io;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Merges a number of keyed input files into a single keyed output file.
 * @param K the type of the key for the files
 * @param V the type of the value for the files
 */
public interface RecordMerger<K, V> {
    public void merge(Collection<RecordSet<K, V>> inputs,
                      KeyedOutputStream output) throws IOException;
}
