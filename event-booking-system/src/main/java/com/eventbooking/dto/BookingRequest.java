package com.eventbooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull(message = "Event id is required") Long eventId,
        @NotNull @Min(value = 1, message = "Book at least 1 seat")
        @Max(value = 10, message = "Max 10 seats per booking") Integer seats
) {}