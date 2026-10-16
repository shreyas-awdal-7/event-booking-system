package com.eventbooking.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank(message = "Title is required") String title,
        @Size(max = 2000) String description,
        @NotBlank(message = "Venue is required") String venue,
        @NotBlank(message = "Category is required") String category,
        @NotNull @Future(message = "Event date must be in the future") LocalDateTime eventDate,
        @NotNull @DecimalMin(value = "0.0", message = "Price cannot be negative") BigDecimal price,
        @NotNull @Min(value = 1, message = "At least 1 seat required") Integer totalSeats,
        String imageUrl
) {}