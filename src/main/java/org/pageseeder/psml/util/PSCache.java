package org.pageseeder.psml.util;

import java.util.LinkedHashMap;

/**
 * An LRU Cache implementation
 */
public class PSCache<K, V> extends LinkedHashMap<K, V> {

    private int maxSize;

    public PSCache(int size) {
        super(size, 0.75f, true);
        this.maxSize = size;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry eldest) {

        // remove the oldest element if size limit is reached
        return size() > maxSize;
    }

}
