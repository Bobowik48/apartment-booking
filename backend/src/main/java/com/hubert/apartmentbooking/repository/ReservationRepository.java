package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.apartment.id = :apartmentId " +
            "AND r.status <> com.hubert.apartmentbooking.model.ReservationStatus.CANCELLED " +
            "AND r.checkInDate < :to AND r.checkOutDate > :from")
    List<Reservation> findOverlapping(
            @Param("apartmentId") Long apartmentId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}