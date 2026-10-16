package com.eventbooking.service;

import com.eventbooking.dto.EventRequest;
import com.eventbooking.dto.EventResponse;
import com.eventbooking.entity.Event;
import com.eventbooking.exception.BadRequestException;
import com.eventbooking.exception.ResourceNotFoundException;
import com.eventbooking.repository.BookingRepository;
import com.eventbooking.repository.EventRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventService(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents(String q, String category) {
        String query = q == null ? "" : q.trim();
        String cat = category == null ? "" : category.trim();
        return eventRepository.searchUpcoming(LocalDateTime.now(), query, cat)
                .stream().map(EventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll(Sort.by("eventDate"))
                .stream().map(EventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long id) {
        return EventResponse.from(findEvent(id));
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return eventRepository.findAllCategories();
    }

    @Transactional
    public EventResponse createEvent(EventRequest req) {
        Event event = Event.builder()
                .title(req.title().trim())
                .description(req.description())
                .venue(req.venue().trim())
                .category(req.category().trim())
                .eventDate(req.eventDate())
                .price(req.price())
                .totalSeats(req.totalSeats())
                .availableSeats(req.totalSeats())
                .imageUrl(req.imageUrl())
                .build();
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest req) {
        // Lock taaki update ke time koi booking seats na badle
        Event event = eventRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));

        int bookedSeats = event.getTotalSeats() - event.getAvailableSeats();
        if (req.totalSeats() < bookedSeats) {
            throw new BadRequestException("Total seats cannot be less than already booked seats (" + bookedSeats + ")");
        }

        event.setTitle(req.title().trim());
        event.setDescription(req.description());
        event.setVenue(req.venue().trim());
        event.setCategory(req.category().trim());
        event.setEventDate(req.eventDate());
        event.setPrice(req.price());
        event.setTotalSeats(req.totalSeats());
        event.setAvailableSeats(req.totalSeats() - bookedSeats);
        event.setImageUrl(req.imageUrl());

        return EventResponse.from(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEvent(id);
        if (bookingRepository.existsByEvent_Id(id)) {
            throw new BadRequestException("Event has bookings and cannot be deleted");
        }
        eventRepository.delete(event);
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));
    }
}