package com.eventbooking.config;

import com.eventbooking.entity.Event;
import com.eventbooking.entity.Role;
import com.eventbooking.entity.User;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataSeeder(UserRepository userRepository, EventRepository eventRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) return;

        createUser("Admin", "admin@eventbooking.com", "admin123", Role.ADMIN);
        createUser("Demo User", "demo@eventbooking.com", "demo123", Role.USER);

        if (eventRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).withNano(0);
            eventRepository.saveAll(List.of(
                event("Sunburn Arena ft. DJ Snake", "Music", "Mahalaxmi Lawns, Pune", now.plusDays(10), "1499", 200,
                        "India's biggest EDM night with live performances and stunning visuals.",
                        "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800"),
                event("Stand-Up Comedy Night", "Comedy", "Phoenix Marketcity, Pune", now.plusDays(5), "499", 120,
                        "An evening of laughter with top stand-up comedians.",
                        "https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=800"),
                event("Tech Conference 2026", "Conference", "JW Marriott, Bangalore", now.plusDays(21), "2999", 300,
                        "Talks on AI, cloud and full-stack engineering from industry leaders.",
                        "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800"),
                event("IPL Screening: MI vs CSK", "Sports", "PVR Icon, Mumbai", now.plusDays(3), "299", 80,
                        "Watch the big match live on the big screen with fellow fans.",
                        "https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=800"),
                event("Marathi Natak: Natsamrat", "Theatre", "Balgandharva Rangmandir, Pune", now.plusDays(14), "399", 150,
                        "A classic Marathi play performed by a renowned theatre group.",
                        "https://images.unsplash.com/photo-1503095396549-807759245b35?w=800"),
                event("Full-Stack Bootcamp Workshop", "Workshop", "Online / Pune", now.plusDays(7), "0", 50,
                        "Hands-on workshop: build and deploy a Spring Boot + React app.",
                        "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800")
            ));
            log.info("Seeded sample events");
        }
    }

    private void createUser(String name, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) return;
        userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build());
        log.info("Created {} user: {}", role, email);
    }

    private Event event(String title, String category, String venue, LocalDateTime date,
                        String price, int seats, String description, String imageUrl) {
        return Event.builder()
                .title(title)
                .category(category)
                .venue(venue)
                .eventDate(date)
                .price(new BigDecimal(price))
                .totalSeats(seats)
                .availableSeats(seats)
                .description(description)
                .imageUrl(imageUrl)
                .build();
    }
}