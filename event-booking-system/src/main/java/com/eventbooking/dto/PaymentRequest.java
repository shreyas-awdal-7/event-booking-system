package com.eventbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PaymentRequest(
        @NotBlank
        @Pattern(regexp = "UPI|CARD|NETBANKING", message = "Method must be UPI, CARD or NETBANKING")
        String method
) {}