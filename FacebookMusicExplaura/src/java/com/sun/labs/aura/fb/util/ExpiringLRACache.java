
package com.sun.labs.aura.fb.util;

import com.sun.labs.minion.util.LRACache;
import java.util.Collections;
import java.util.Map;

/**
 * An LRA cache that offers an expiration time for each cached value.  The
 * LinkedHashMap stored internally is wrapped by Collections.synchronizedMap.
 * 
 * @author ja151348
 */
public class ExpiringLRACache<K,V> {

    private Map<K,ValueContainer<V>> cache;
    private long time2live; // time objects can live in cache in millisec

    /**
     * Constructs an LRA cache with a max size and a max time to live for each
     * value.  When the cache is full, the value accessed (get or put) least
     * recently will be evicted.  Follows the semantics of LinkedHashMap.
     * 
     * @param maxSize maximum number of elements to hold in the cache
     * @param time2live maximum time (in milliseconds) an item may be
     *                  held in the cache
     */
    public ExpiringLRACache(int maxSize, long time2live) {
        cache = Collections.synchronizedMap(new LRACache<K,ValueContainer<V>>(maxSize));
        this.time2live = time2live;
    }

    public int getSize() {
        return cache.size();
    }

    public V get(K key) {
        ValueContainer<V> val = cache.get(key);
        if (val == null) {
            return null;
        } else if (val.getExpiration() < System.currentTimeMillis()) {
            cache.remove(key);
            return null;
        } else {
            return val.getValue();
        }
    }

    public V put(K key, V value) {
        ValueContainer<V> prev =
                cache.put(key,
                          new ValueContainer<V>(System.currentTimeMillis()
                                                + time2live, value));
        if (prev != null) {
            return prev.getValue();
        }
        return null;
    }


    private class ValueContainer<V> {

        private V value;
        private long expiration;

        public ValueContainer(long creationTime, V value) {
            this.expiration = creationTime;
            this.value = value;
        }

        public V getValue() {
            return value;
        }

        public long getExpiration() {
            return expiration;
        }
    }
}