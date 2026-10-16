package com.eventbooking.repository;

import com.eventbooking.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Row lock: booking ke time ek hi transaction seats update kar sakta hai
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT e FROM Event e
            WHERE e.eventDate > :now
              AND LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%'))
              AND (:category = '' OR e.category = :category)
            ORDER BY e.eventDate ASC
            """)
    List<Event> searchUpcoming(@Param("now") LocalDateTime now,
                               @Param("q") String q,
                               @Param("category") String category);

    @Query("SELECT DISTINCT e.category FROM Event e ORDER BY e.category")
    List<String> findAllCategories();
}