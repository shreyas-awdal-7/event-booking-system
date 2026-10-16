package com.eventbooking.service;

import com.eventbooking.dto.EventRequest;
import com.eventbooking.dto.EventResponse;
import com.eventbooking.entity.Event;
import com.eventbooking.exception.BadRequestException;
import com.eventbooking.repository.BookingRepository;
import com.eventbooking.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private EventService eventService;

    private Event event;

    @BeforeEach
    void setUp() {
        // 100 seats mein se 60 booked hain
        event = Event.builder()
                .id(1L).title("Music Fest").venue("Pune").category("Music")
                .eventDate(LocalDateTime.now().plusDays(10))
                .price(new BigDecimal("999"))
                .totalSeats(100).availableSeats(40)
                .build();
    }

    @Test
    void createEvent_setsAvailableSeatsEqualToTotal() {
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventResponse response = eventService.createEvent(request(150));

        assertThat(response.totalSeats()).isEqualTo(150);
        assertThat(response.availableSeats()).isEqualTo(150);
    }

    @Test
    void updateEvent_recalculatesAvailableSeats() {
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));

        EventResponse response = eventService.updateEvent(1L, request(120));

        assertThat(response.totalSeats()).isEqualTo(120);
        assertThat(response.availableSeats()).isEqualTo(60);
    }

    @Test
    void updateEvent_belowBookedSeats_throws() {
        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateEvent(1L, request(50)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("60");
    }

    @Test
    void deleteEvent_withBookings_throws() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByEvent_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> eventService.deleteEvent(1L))
                .isInstanceOf(BadRequestException.class);
        verify(eventRepository, never()).delete(any());
    }

    private EventRequest request(int totalSeats) {
        return new EventRequest("Music Fest", "Live music", "Pune", "Music",
                LocalDateTime.now().plusDays(10), new BigDecimal("999"), totalSeats, null);
    }
}