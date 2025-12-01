package com.example.practical_test.dto;

public class CacheStatsResponse {
    private int cacheSize;
    private int maxCacheSize;
    private String message;
    
    public CacheStatsResponse() {
    }
    
    public CacheStatsResponse(int cacheSize, int maxCacheSize, String message) {
        this.cacheSize = cacheSize;
        this.maxCacheSize = maxCacheSize;
        this.message = message;
    }
    
    public int getCacheSize() {
        return cacheSize;
    }
    
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
    
    public int getMaxCacheSize() {
        return maxCacheSize;
    }
    
    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

