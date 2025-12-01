package com.example.practical_test.repository;

import com.example.practical_test.model.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EventRepositoryImpl implements EventRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<Event> findEventsWithFilters(Long userId, String eventType, String category, 
                                             Long productId, LocalDateTime from, LocalDateTime to) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> event = query.from(Event.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (userId != null) {
            predicates.add(cb.equal(event.get("userId"), userId));
        }
        if (eventType != null && !eventType.isEmpty()) {
            predicates.add(cb.equal(event.get("eventType"), eventType));
        }
        if (category != null && !category.isEmpty()) {
            predicates.add(cb.equal(event.get("category"), category));
        }
        if (productId != null) {
            predicates.add(cb.equal(event.get("productId"), productId));
        }
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("timestamp"), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("timestamp"), to));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(event.get("timestamp")));
        
        TypedQuery<Event> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }
}

