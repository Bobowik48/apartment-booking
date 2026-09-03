package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.exception.ApartmentNotFoundException;
import com.hubert.apartmentbooking.model.Apartment;
import com.hubert.apartmentbooking.repository.ApartmentRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.ADMIN_PATH + "/apartments")
@Tag(name = "Admin - Apartments", description = "Admin-only apartment lookups (requires ROLE_ADMIN)")
public class AdminApartmentController {

    private final ApartmentRepository apartmentRepository;

    public AdminApartmentController(ApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }

    @GetMapping("/{id}")
    public Apartment getApartment(@PathVariable Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ApartmentNotFoundException(Constants.APARTMENT_NOT_FOUND));
    }
}