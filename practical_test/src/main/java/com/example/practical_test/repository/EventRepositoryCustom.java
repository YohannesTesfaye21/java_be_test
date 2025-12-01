package com.example.practical_test.repository;

import com.example.practical_test.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> findEventsWithFilters(Long userId, String eventType, String category, 
                                     Long productId, LocalDateTime from, LocalDateTime to);
}

