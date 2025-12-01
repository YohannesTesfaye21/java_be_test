package com.example.practical_test.dto;

import java.util.Map;

public class EventSummaryResponse {
    private Map<String, Long> summary;
    
    public EventSummaryResponse() {
    }
    
    public EventSummaryResponse(Map<String, Long> summary) {
        this.summary = summary;
    }
    
    public Map<String, Long> getSummary() {
        return summary;
    }
    
    public void setSummary(Map<String, Long> summary) {
        this.summary = summary;
    }
}

