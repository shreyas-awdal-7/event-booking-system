package com.eventbooking.controller;

import com.eventbooking.dto.BookingResponse;
import com.eventbooking.dto.DashboardStats;
import com.eventbooking.dto.EventResponse;
import com.eventbooking.service.BookingService;
import com.eventbooking.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BookingService bookingService;
    private final EventService eventService;

    public AdminController(BookingService bookingService, EventService eventService) {
        this.bookingService = bookingService;
        this.eventService = eventService;
    }

    @GetMapping("/stats")
    public DashboardStats stats() {
        return bookingService.getStats();
    }

    @GetMapping("/bookings")
    public List<BookingResponse> allBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/events")
    public List<EventResponse> allEvents() {
        return eventService.getAllEvents();
    }
}