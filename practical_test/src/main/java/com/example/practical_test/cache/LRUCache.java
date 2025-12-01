package com.example.practical_test.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-safe LRU (Least Recently Used) Cache implementation
 * Uses LinkedHashMap with access-order to maintain insertion order
 */
public class LRUCache<K, V> {
    private final int maxSize;
    private final Map<K, V> cache;
    
    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
        // LinkedHashMap with accessOrder=true maintains LRU order
        // The third parameter (0.75f) is load factor, true means access-order
        this.cache = new LinkedHashMap<K, V>(maxSize + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                // Remove oldest entry when cache exceeds max size
                return size() > LRUCache.this.maxSize;
            }
        };
    }
    
    public synchronized V get(K key) {
        return cache.get(key);
    }
    
    public synchronized void put(K key, V value) {
        cache.put(key, value);
    }
    
    public synchronized void remove(K key) {
        cache.remove(key);
    }
    
    public synchronized void clear() {
        cache.clear();
    }
    
    public synchronized int size() {
        return cache.size();
    }
    
    public synchronized boolean containsKey(K key) {
        return cache.containsKey(key);
    }
}

