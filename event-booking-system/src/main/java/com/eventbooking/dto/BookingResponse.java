package com.eventbooking.dto;

import com.eventbooking.entity.Booking;
import com.eventbooking.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String bookingRef,
        Long eventId,
        String eventTitle,
        String venue,
        LocalDateTime eventDate,
        Integer seats,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        String userName,
        String userEmail,
        String paymentStatus,
        String transactionId
) {
    public static BookingResponse from(Booking b, Payment p) {
        return new BookingResponse(
                b.getId(), b.getBookingRef(),
                b.getEvent().getId(), b.getEvent().getTitle(), b.getEvent().getVenue(), b.getEvent().getEventDate(),
                b.getSeats(), b.getTotalAmount(), b.getStatus().name(), b.getCreatedAt(),
                b.getUser().getName(), b.getUser().getEmail(),
                  p != null ? p.getStatus().name() : null,
                p != null ? p.getTransactionId() : null
        );
    }
}