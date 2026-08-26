package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.ApartmentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentPhotoRepository extends JpaRepository<ApartmentPhoto, Long> {
    List<ApartmentPhoto> findByApartment_IdOrderByDisplayOrderAsc(Long apartmentId);
}