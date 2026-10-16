package com.eventbooking.controller;

import com.eventbooking.dto.BookingRequest;
import com.eventbooking.dto.BookingResponse;
import com.eventbooking.dto.PaymentRequest;
import com.eventbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal UserDetails principal,
                                                  @Valid @RequestBody BookingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(principal.getUsername(), req));
    }

    @GetMapping("/my")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal UserDetails principal) {
        return bookingService.getMyBookings(principal.getUsername());
    }

    @PostMapping("/{id}/pay")
    public BookingResponse pay(@AuthenticationPrincipal UserDetails principal,
                               @PathVariable Long id,
                               @Valid @RequestBody PaymentRequest req) {
        return bookingService.pay(principal.getUsername(), id, req);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal UserDetails principal,
                                  @PathVariable Long id) {
        return bookingService.cancel(principal.getUsername(), id);
    }
}