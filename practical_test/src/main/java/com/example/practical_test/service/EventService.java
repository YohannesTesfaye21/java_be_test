package com.example.practical_test.service;

import com.example.practical_test.dto.EventRequest;
import com.example.practical_test.dto.EventResponse;
import com.example.practical_test.model.Event;
import com.example.practical_test.repository.EventRepository;
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
    @Autowired
    private EventRepository eventRepository;
    
    // Thread-safe in-memory queue for latest 500 events
    private final ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private static final int MAX_QUEUE_SIZE = 500;
    
    public EventResponse createEvent(EventRequest request) {
        Event event = new Event();
        event.setUserId(request.getUserId());
        event.setEventType(request.getEventType());
        event.setProductId(request.getProductId());
        event.setCategory(request.getCategory());
        event.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
        
        // Validate event type
        if (!isValidEventType(event.getEventType())) {
            throw new IllegalArgumentException("Invalid event type. Must be VIEW, ADD_TO_CART, or PURCHASE");
        }
        
        // Save to database
        Event savedEvent = eventRepository.save(event);
        
        // Add to in-memory queue
        addToQueue(savedEvent);
        
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
        List<Event> events = eventRepository.findEventsWithFilters(
            userId, eventType, category, productId, from, to
        );
        
        return events.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
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
        return "VIEW".equals(eventType) || 
               "ADD_TO_CART".equals(eventType) || 
               "PURCHASE".equals(eventType);
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
}

