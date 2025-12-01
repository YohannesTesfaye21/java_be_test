package com.example.practical_test.service;

import com.example.practical_test.cache.LRUCache;
import com.example.practical_test.dto.EventRequest;
import com.example.practical_test.dto.EventResponse;
import com.example.practical_test.dto.EventSearchKey;
import com.example.practical_test.model.Event;
import com.example.practical_test.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    
    @Autowired
    private EventRepository eventRepository;
    
    // Thread-safe in-memory queue for latest 500 events
    private final ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private static final int MAX_QUEUE_SIZE = 500;
    
    // LRU Cache for event search results (max 100 cached queries)
    private final LRUCache<EventSearchKey, List<EventResponse>> eventCache = new LRUCache<>(100);
    
    public EventResponse createEvent(EventRequest request) {
        Event event = new Event();
        event.setUserId(request.getUserId());
        event.setEventType(request.getEventType());
        event.setProductId(request.getProductId());
        event.setCategory(request.getCategory());
        event.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
        
        // Validate event type (case-insensitive)
        if (!isValidEventType(event.getEventType())) {
            throw new IllegalArgumentException("Invalid event type. Must be VIEW, ADD_TO_CART, or PURCHASE (case-insensitive)");
        }
        
        // Normalize event type to uppercase for consistency
        event.setEventType(event.getEventType().toUpperCase());
        
        // Save to database
        Event savedEvent = eventRepository.save(event);
        
        // Add to in-memory queue
        addToQueue(savedEvent);
        
        // Invalidate cache when new event is created
        invalidateCache();
        
        return toEventResponse(savedEvent);
    }
    
    public List<EventResponse> createEventsBatch(List<EventRequest> requests) {
        List<EventResponse> responses = new ArrayList<>();
        for (EventRequest request : requests) {
            responses.add(createEvent(request));
        }
        return responses;
    }
    
    public List<EventResponse> getEvents(Long userId, String eventType, String category, 
                                        Long productId, LocalDateTime from, LocalDateTime to) {
        // Create cache key from search parameters
        EventSearchKey cacheKey = new EventSearchKey(userId, eventType, category, productId, from, to);
        
        // Check LRU cache first
        List<EventResponse> cachedResult = eventCache.get(cacheKey);
        if (cachedResult != null) {
            logger.info("LRU Cache HIT - Returning cached results for query: userId={}, eventType={}, category={}", 
                       userId, eventType, category);
            logger.info("Cache size: {}", eventCache.size());
            return new ArrayList<>(cachedResult); // Return a copy to avoid external modification
        }
        
        // Cache miss - query database
        logger.info("LRU Cache MISS - Querying database for: userId={}, eventType={}, category={}", 
                   userId, eventType, category);
        List<Event> events = eventRepository.findEventsWithFilters(
            userId, eventType, category, productId, from, to
        );
        
        List<EventResponse> responses = events.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
        
        // Store in cache
        eventCache.put(cacheKey, new ArrayList<>(responses)); // Store a copy
        logger.info("Cached query result. Cache size: {}", eventCache.size());
        
        return responses;
    }
    
    public List<EventResponse> getRecentEvents() {
        // Get latest 20 events from queue
        // Convert queue to list and take the last 20 elements
        List<Event> allEvents = new ArrayList<>(eventQueue);
        int startIndex = Math.max(0, allEvents.size() - 20);
        List<Event> recentEvents = allEvents.subList(startIndex, allEvents.size());
        
        return recentEvents.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }
    
    public Map<String, Long> getEventSummary(String category) {
        // Get events from queue
        List<Event> events = new ArrayList<>(eventQueue);
        
        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            events = events.stream()
                    .filter(e -> category.equals(e.getCategory()))
                    .collect(Collectors.toList());
        }
        
        // Group by event type and count
        return events.stream()
                .collect(Collectors.groupingBy(
                    Event::getEventType,
                    Collectors.counting()
                ));
    }
    
    private void addToQueue(Event event) {
        eventQueue.offer(event);
        
        // Maintain max size of 500
        while (eventQueue.size() > MAX_QUEUE_SIZE) {
            eventQueue.poll(); // Remove oldest event
        }
    }
    
    private boolean isValidEventType(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            return false;
        }
        // Case-insensitive validation
        String normalized = eventType.trim().toUpperCase();
        return "VIEW".equals(normalized) || 
               "ADD_TO_CART".equals(normalized) || 
               "PURCHASE".equals(normalized);
    }
    
    private EventResponse toEventResponse(Event event) {
        return new EventResponse(
            event.getId(),
            event.getUserId(),
            event.getEventType(),
            event.getProductId(),
            event.getCategory(),
            event.getTimestamp()
        );
    }
    
    /**
     * Get cache statistics for testing
     */
    public int getCacheSize() {
        return eventCache.size();
    }
    
    public int getMaxCacheSize() {
        return 100; // Max cache size
    }
    
    /**
     * Clear cache manually (for testing)
     */
    public void clearCache() {
        logger.info("Cache manually cleared. Cache size was: {}", eventCache.size());
        eventCache.clear();
    }
    
    /**
     * Invalidate the cache when new events are created
     * This ensures cache consistency
     */
    private void invalidateCache() {
        logger.info("Cache invalidated due to new event creation. Cache cleared.");
        eventCache.clear();
    }
}

