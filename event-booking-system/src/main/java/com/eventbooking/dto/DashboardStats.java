package com.eventbooking.dto;

import java.math.BigDecimal;

public record DashboardStats(
        long totalEvents,
        long totalUsers,
        long totalBookings,
        long confirmedBookings,
        BigDecimal totalRevenue
) {}