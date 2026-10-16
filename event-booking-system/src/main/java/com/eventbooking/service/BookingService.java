package com.eventbooking.service;

import com.eventbooking.dto.BookingRequest;
import com.eventbooking.dto.BookingResponse;
import com.eventbooking.dto.DashboardStats;
import com.eventbooking.dto.PaymentRequest;
import com.eventbooking.entity.*;
import com.eventbooking.exception.BadRequestException;
import com.eventbooking.exception.ResourceNotFoundException;
import com.eventbooking.exception.SeatsUnavailableException;
import com.eventbooking.repository.BookingRepository;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.PaymentRepository;
import com.eventbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;

    @Value("${app.booking.pending-expiry-minutes}")
    private long pendingExpiryMinutes;

    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository,
                          UserRepository userRepository, PaymentRepository paymentRepository,
                          PaymentService paymentService, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }

    /**
     * Seats book karta hai. Event row pe PESSIMISTIC_WRITE lock lagta hai,
     * isliye ek saath aayi requests ek-ek karke chalti hain aur overbooking nahi hoti.
     */
    @Transactional
    public BookingResponse createBooking(String email, BookingRequest req) {
        User user = findUser(email);
        Event event = eventRepository.findByIdForUpdate(req.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + req.eventId()));

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This event has already started");
        }
        if (event.getAvailableSeats() < req.seats()) {
            throw new SeatsUnavailableException("Only " + event.getAvailableSeats() + " seat(s) left for this event");
        }

        event.setAvailableSeats(event.getAvailableSeats() - req.seats());

        Booking booking = Booking.builder()
                .bookingRef(generateRef())
                .user(user)
                .event(event)
                .seats(req.seats())
                .totalAmount(event.getPrice().multiply(BigDecimal.valueOf(req.seats())))
                .status(BookingStatus.PENDING_PAYMENT)
                .build();
        bookingRepository.save(booking);

        return BookingResponse.from(booking, null);
    }

    @Transactional
    public BookingResponse pay(String email, Long bookingId, PaymentRequest req) {
        Booking booking = findOwnedBooking(email, bookingId);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not awaiting payment");
        }
        if (booking.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(pendingExpiryMinutes))) {
            throw new BadRequestException("Booking has expired. Please book again.");
        }

        Payment payment = paymentService.processPayment(booking, req.method());
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }

        BookingResponse response = BookingResponse.from(booking, payment);
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            emailService.sendBookingConfirmation(response);
        }
        return response;
    }

    @Transactional
    public BookingResponse cancel(String email, Long bookingId) {
        User currentUser = findUser(email);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        if (!isOwner && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You cannot cancel this booking");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        releaseSeats(booking);
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            paymentService.refund(booking);
        }
        booking.setStatus(BookingStatus.CANCELLED);

        BookingResponse response = toResponse(booking);
        emailService.sendCancellation(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String email) {
        User user = findUser(email);
        return bookingRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        return new DashboardStats(
                eventRepository.count(),
                userRepository.count(),
                bookingRepository.count(),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED),
                bookingRepository.sumAmountByStatus(BookingStatus.CONFIRMED)
        );
    }

    /** Payment na hone par 15 minute baad pending bookings cancel karke seats wapas karta hai. */
    @Transactional
    public int expirePendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(pendingExpiryMinutes);
        List<Booking> expired = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, cutoff);

        for (Booking booking : expired) {
            releaseSeats(booking);
            booking.setStatus(BookingStatus.CANCELLED);
        }
        return expired.size();
    }

    private void releaseSeats(Booking booking) {
        Event event = eventRepository.findByIdForUpdate(booking.getEvent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setAvailableSeats(event.getAvailableSeats() + booking.getSeats());
    }

    private BookingResponse toResponse(Booking booking) {
        Payment payment = paymentRepository.findByBooking_Id(booking.getId()).orElse(null);
        return BookingResponse.from(booking, payment);
    }

    private Booking findOwnedBooking(String email, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        return booking;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String generateRef() {
        return "EVB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}