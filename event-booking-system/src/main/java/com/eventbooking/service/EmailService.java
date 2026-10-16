package com.eventbooking.service;

import com.eventbooking.dto.BookingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled}")
    private boolean mailEnabled;

    @Value("${app.mail.from}")
    private String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Async
    public void sendBookingConfirmation(BookingResponse b) {
        String body = """
                Hi %s,

                Your booking is confirmed!

                Booking Ref : %s
                Event       : %s
                Venue       : %s
                Date        : %s
                Seats       : %d
                Amount Paid : Rs. %s
                Transaction : %s

                Thank you for booking with us.
                """.formatted(b.userName(), b.bookingRef(), b.eventTitle(), b.venue(),
                b.eventDate(), b.seats(), b.totalAmount(), b.transactionId());

        send(b.userEmail(), "Booking Confirmed - " + b.bookingRef(), body);
    }

    @Async
    public void sendCancellation(BookingResponse b) {
        String body = """
                Hi %s,

                Your booking %s for "%s" has been cancelled.
                If you had paid, the refund will be processed to your original payment method.
                """.formatted(b.userName(), b.bookingRef(), b.eventTitle());

        send(b.userEmail(), "Booking Cancelled - " + b.bookingRef(), body);
    }

    private void send(String to, String subject, String body) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (!mailEnabled || sender == null) {
            log.info("[EMAIL] To: {} | Subject: {}\n{}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
        } catch (Exception ex) {
            log.error("Failed to send email to {}", to, ex);
        }
    }
}