package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, Long> {
}