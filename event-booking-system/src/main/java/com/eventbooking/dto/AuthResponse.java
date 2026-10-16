package com.eventbooking.dto;

public record AuthResponse(String token, UserResponse user) {}