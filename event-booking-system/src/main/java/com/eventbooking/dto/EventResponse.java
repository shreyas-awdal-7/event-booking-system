package com.eventbooking.dto;

import com.eventbooking.entity.Event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String venue,
        String category,
        LocalDateTime eventDate,
        BigDecimal price,
        Integer totalSeats,
        Integer availableSeats,
        String imageUrl
) {
    public static EventResponse from(Event e) {
        return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getVenue(),
                e.getCategory(), e.getEventDate(), e.getPrice(), e.getTotalSeats(),
                e.getAvailableSeats(), e.getImageUrl());
    }
}