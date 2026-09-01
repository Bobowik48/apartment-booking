package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPayuOrderId(String payuOrderId);

    Optional<Payment> findByReservation_Id(Long reservationId);
}