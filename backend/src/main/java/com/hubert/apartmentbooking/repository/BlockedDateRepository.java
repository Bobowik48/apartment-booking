package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {

    @Query("SELECT b FROM BlockedDate b WHERE b.apartment.id = :apartmentId " +
            "AND b.startDate < :to AND b.endDate > :from")
    List<BlockedDate> findOverlapping(
            @Param("apartmentId") Long apartmentId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}