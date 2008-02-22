package com.sun.labs.aura.datastore;

/**
 * A tagging interface that can be implemented by objects that are the values
 * in a <code>SimpleItem</code>'s data map.  If a value implements this index,
 * then the value will have its <code>toString</code> method called and the 
 * resulting data will be treated like a string by the indexer.
 */
public interface Indexable {

}
