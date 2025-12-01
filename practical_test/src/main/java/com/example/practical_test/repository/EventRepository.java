package com.example.practical_test.repository;

import com.example.practical_test.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
    List<Event> findByUserId(Long userId);
    
    List<Event> findByEventType(String eventType);
    
    List<Event> findByCategory(String category);
    
    List<Event> findByProductId(Long productId);
}

