package com.eventbooking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryScheduler.class);

    private final BookingService bookingService;

    public BookingExpiryScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Har 1 minute mein expired pending bookings release karta hai
    @Scheduled(fixedDelay = 60000)
    public void releaseExpiredBookings() {
        int count = bookingService.expirePendingBookings();
        if (count > 0) {
            log.info("Released {} expired pending booking(s)", count);
        }
    }
}