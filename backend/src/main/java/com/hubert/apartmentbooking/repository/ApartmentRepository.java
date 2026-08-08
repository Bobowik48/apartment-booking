package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
}