package com.example.practical_test.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Key object for caching event search results
 * Used as cache key for LRU cache
 */
public class EventSearchKey {
    private Long userId;
    private String eventType;
    private String category;
    private Long productId;
    private LocalDateTime from;
    private LocalDateTime to;
    
    public EventSearchKey(Long userId, String eventType, String category, 
                         Long productId, LocalDateTime from, LocalDateTime to) {
        this.userId = userId;
        this.eventType = eventType;
        this.category = category;
        this.productId = productId;
        this.from = from;
        this.to = to;
    }
    
    // Getters
    public Long getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public String getCategory() { return category; }
    public Long getProductId() { return productId; }
    public LocalDateTime getFrom() { return from; }
    public LocalDateTime getTo() { return to; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSearchKey that = (EventSearchKey) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(category, that.category) &&
               Objects.equals(productId, that.productId) &&
               Objects.equals(from, that.from) &&
               Objects.equals(to, that.to);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, eventType, category, productId, from, to);
    }
}

