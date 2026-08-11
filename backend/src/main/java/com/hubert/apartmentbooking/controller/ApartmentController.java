package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.exception.ApartmentNotFoundException;
import com.hubert.apartmentbooking.model.Apartment;
import com.hubert.apartmentbooking.repository.ApartmentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.APARTMENTS_PATH)
public class ApartmentController {

    private final ApartmentRepository apartmentRepository;

    public ApartmentController(ApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }

    @GetMapping("/{id}")
    public Apartment getApartment(@PathVariable Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ApartmentNotFoundException(Constants.APARTMENT_NOT_FOUND));
    }
}
