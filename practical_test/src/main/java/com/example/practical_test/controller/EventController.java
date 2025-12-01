package com.example.practical_test.controller;

import com.example.practical_test.dto.EventRequest;
import com.example.practical_test.dto.EventResponse;
import com.example.practical_test.dto.EventSummaryResponse;
import com.example.practical_test.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Event management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {
    @Autowired
    private EventService eventService;
    
    @Operation(summary = "Create a single event", description = "Create a new user event (VIEW, ADD_TO_CART, or PURCHASE)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Event created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid event data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.practical_test.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.practical_test.dto.ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequest request) {
        try {
            EventResponse response = eventService.createEvent(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "BAD_REQUEST"));
        }
    }
    
    @Operation(summary = "Create multiple events", description = "Create multiple events in a single batch request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Events created successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid event data",
            content = @Content(schema = @Schema(implementation = com.example.practical_test.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @PostMapping("/batch")
    public ResponseEntity<?> createEventsBatch(@Valid @RequestBody List<EventRequest> requests) {
        try {
            List<EventResponse> responses = eventService.createEventsBatch(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "BAD_REQUEST"));
        }
    }
    
    @Operation(summary = "Search and filter events", 
        description = "Search events with optional filters: userId, eventType, category, productId, date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = EventResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.practical_test.dto.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getEvents(
            @Parameter(description = "Filter by user ID") 
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by event type (VIEW, ADD_TO_CART, PURCHASE)") 
            @RequestParam(required = false) String eventType,
            @Parameter(description = "Filter by category") 
            @RequestParam(required = false) String category,
            @Parameter(description = "Filter by product ID") 
            @RequestParam(required = false) Long productId,
            @Parameter(description = "Filter events from this date (ISO format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Filter events until this date (ISO format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            List<EventResponse> events = eventService.getEvents(userId, eventType, category, productId, from, to);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "BAD_REQUEST"));
        }
    }
    
    @Operation(summary = "Get recent events", 
        description = "Get the latest 20 events from the in-memory queue (fast access, no database query)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent events retrieved successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentEvents() {
        try {
            List<EventResponse> events = eventService.getRecentEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }
    
    @Operation(summary = "Get event summary", 
        description = "Get count of events grouped by event type from the in-memory queue (last 500 events). Optionally filter by category.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event summary retrieved successfully",
            content = @Content(schema = @Schema(implementation = EventSummaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @GetMapping("/summary")
    public ResponseEntity<?> getEventSummary(
            @Parameter(description = "Optional category filter") 
            @RequestParam(required = false) String category) {
        try {
            Map<String, Long> summary = eventService.getEventSummary(category);
            return ResponseEntity.ok(new EventSummaryResponse(summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }
    
    @Operation(summary = "Get cache statistics", 
        description = "Get LRU cache statistics (cache size, max size) for testing purposes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.practical_test.dto.CacheStatsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @GetMapping("/cache/stats")
    public ResponseEntity<?> getCacheStats() {
        try {
            int cacheSize = eventService.getCacheSize();
            int maxCacheSize = eventService.getMaxCacheSize();
            String message = String.format("Cache contains %d/%d entries", cacheSize, maxCacheSize);
            return ResponseEntity.ok(new com.example.practical_test.dto.CacheStatsResponse(cacheSize, maxCacheSize, message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }
    
    @Operation(summary = "Clear cache", 
        description = "Manually clear the LRU cache (for testing purposes)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache cleared successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @DeleteMapping("/cache")
    public ResponseEntity<?> clearCache() {
        try {
            eventService.clearCache();
            return ResponseEntity.ok(new com.example.practical_test.dto.CacheStatsResponse(0, 100, "Cache cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.practical_test.dto.ErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR"));
        }
    }
}

