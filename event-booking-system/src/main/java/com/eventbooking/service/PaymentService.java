package com.eventbooking.service;

import com.eventbooking.entity.Booking;
import com.eventbooking.entity.Payment;
import com.eventbooking.entity.PaymentStatus;
import com.eventbooking.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Simulated payment gateway. Production mein yahan Razorpay/Stripe SDK call hoga.
    public Payment processPayment(Booking booking, String method) {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .method(method)
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .paidAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }

    public void refund(Booking booking) {
        paymentRepository.findByBooking_Id(booking.getId())
                .ifPresent(p -> p.setStatus(PaymentStatus.REFUNDED));
    }
}