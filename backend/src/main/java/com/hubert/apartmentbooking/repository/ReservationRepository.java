package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.Reservation;
import com.hubert.apartmentbooking.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.apartment.id = :apartmentId " +
            "AND r.status <> :excludedStatus " +
            "AND r.checkInDate < :to AND r.checkOutDate > :from")
    List<Reservation> findOverlapping(
            @Param("apartmentId") Long apartmentId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("excludedStatus") ReservationStatus excludedStatus);

    Optional<Reservation> findByAccessToken(String accessToken);

    List<Reservation> findByUser_EmailOrderByCheckInDateDesc(String email);
}