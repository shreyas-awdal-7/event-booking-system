package com.eventbooking.service;

import com.eventbooking.dto.BookingRequest;
import com.eventbooking.dto.BookingResponse;
import com.eventbooking.dto.PaymentRequest;
import com.eventbooking.entity.*;
import com.eventbooking.exception.BadRequestException;
import com.eventbooking.exception.SeatsUnavailableException;
import com.eventbooking.repository.BookingRepository;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.PaymentRepository;
import com.eventbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentService paymentService;
    @Mock private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingService, "pendingExpiryMinutes", 15L);

        user = User.builder().id(1L).name("Aditya").email("aditya@test.com").role(Role.USER).build();

        event = Event.builder()
                .id(1L).title("Tech Conference").venue("Pune").category("Conference")
                .eventDate(LocalDateTime.now().plusDays(5))
                .price(new BigDecimal("500"))
                .totalSeats(10).availableSeats(5)
                .build();
    }

    @Test
    void createBooking_success_reducesAvailableSeats() {
        when(userRepository.findByEmail("aditya@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.createBooking("aditya@test.com", new BookingRequest(1L, 2));

        assertThat(response.seats()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("1000");
        assertThat(response.status()).isEqualTo("PENDING_PAYMENT");
        assertThat(event.getAvailableSeats()).isEqualTo(3);
    }

    @Test
    void createBooking_notEnoughSeats_throwsAndDoesNotSave() {
        when(userRepository.findByEmail("aditya@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> bookingService.createBooking("aditya@test.com", new BookingRequest(1L, 6)))
                .isInstanceOf(SeatsUnavailableException.class)
                .hasMessageContaining("Only 5 seat(s) left");

        assertThat(event.getAvailableSeats()).isEqualTo(5);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_pastEvent_throws() {
        event.setEventDate(LocalDateTime.now().minusDays(1));
        when(userRepository.findByEmail("aditya@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> bookingService.createBooking("aditya@test.com", new BookingRequest(1L, 1)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void pay_pendingBooking_confirmsAndSendsEmail() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, user);
        Payment payment = Payment.builder().booking(booking).amount(booking.getTotalAmount())
                .method("UPI").status(PaymentStatus.SUCCESS).transactionId("TXN-123").build();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(paymentService.processPayment(booking, "UPI")).thenReturn(payment);

        BookingResponse response = bookingService.pay("aditya@test.com", 10L, new PaymentRequest("UPI"));

        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(response.transactionId()).isEqualTo("TXN-123");
        verify(emailService).sendBookingConfirmation(any(BookingResponse.class));
    }

    @Test
    void pay_alreadyConfirmed_throws() {
        Booking booking = booking(BookingStatus.CONFIRMED, user);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.pay("aditya@test.com", 10L, new PaymentRequest("UPI")))
                .isInstanceOf(BadRequestException.class);
        verify(paymentService, never()).processPayment(any(), any());
    }

    @Test
    void cancel_confirmedBooking_releasesSeatsAndRefunds() {
        Booking booking = booking(BookingStatus.CONFIRMED, user);
        event.setAvailableSeats(3);

        when(userRepository.findByEmail("aditya@test.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(paymentRepository.findByBooking_Id(10L)).thenReturn(Optional.empty());

        BookingResponse response = bookingService.cancel("aditya@test.com", 10L);

        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(event.getAvailableSeats()).isEqualTo(5);
        verify(paymentService).refund(booking);
        verify(emailService).sendCancellation(any(BookingResponse.class));
    }

    @Test
    void cancel_otherUsersBooking_throwsAccessDenied() {
        User otherUser = User.builder().id(2L).name("Other").email("other@test.com").role(Role.USER).build();
        Booking booking = booking(BookingStatus.CONFIRMED, user);

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel("other@test.com", 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Booking booking(BookingStatus status, User owner) {
        return Booking.builder()
                .id(10L).bookingRef("EVB-TEST1234")
                .user(owner).event(event)
                .seats(2).totalAmount(new BigDecimal("1000"))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}