package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.UpdateApartmentRequest;
import com.hubert.apartmentbooking.dto.request.UpdatePhotoRequest;
import com.hubert.apartmentbooking.dto.response.ApartmentResponse;
import com.hubert.apartmentbooking.exception.ApartmentNotFoundException;
import com.hubert.apartmentbooking.model.Apartment;
import com.hubert.apartmentbooking.model.ApartmentPhoto;
import com.hubert.apartmentbooking.repository.ApartmentPhotoRepository;
import com.hubert.apartmentbooking.repository.ApartmentRepository;
import com.hubert.apartmentbooking.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(Constants.APARTMENTS_PATH)
@Tag(name = "Apartments", description = "Apartment details, photo gallery and admin edits")
public class ApartmentController {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentPhotoRepository apartmentPhotoRepository;
    private final FileStorageService fileStorageService;

    public ApartmentController(ApartmentRepository apartmentRepository,
                               ApartmentPhotoRepository apartmentPhotoRepository,
                               FileStorageService fileStorageService) {
        this.apartmentRepository = apartmentRepository;
        this.apartmentPhotoRepository = apartmentPhotoRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{id}")
    public ApartmentResponse getApartment(@PathVariable Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ApartmentNotFoundException(Constants.APARTMENT_NOT_FOUND));
        return ApartmentResponse.from(apartment);
    }

    @PutMapping("/{id}")
    public Apartment updateApartment(@PathVariable Long id, @Valid @RequestBody UpdateApartmentRequest request) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ApartmentNotFoundException(Constants.APARTMENT_NOT_FOUND));

        apartment.setName(request.name());
        apartment.setDescription(request.description());
        apartment.setStreet(request.street());
        apartment.setApartmentNumber(request.apartmentNumber());
        apartment.setDistrict(request.district());
        apartment.setCity(request.city());
        apartment.setPricePerNight(request.pricePerNight());
        apartment.setMaxGuests(request.maxGuests());
        apartment.setArea(request.area());
        apartment.setFloor(request.floor());
        apartment.setBuildingEntranceCode(request.buildingEntranceCode());
        apartment.setKeyBoxCode(request.keyBoxCode());

        return apartmentRepository.save(apartment);
    }

    @GetMapping("/{id}/photos")
    public List<ApartmentPhoto> getPhotos(@PathVariable Long id) {
        return apartmentPhotoRepository.findByApartment_IdOrderByDisplayOrderAsc(id);
    }

    @PostMapping("/{id}/photos")
    public ApartmentPhoto uploadPhoto(@PathVariable Long id,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "altText", required = false) String altText) throws IOException {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ApartmentNotFoundException(Constants.APARTMENT_NOT_FOUND));

        String fileName = fileStorageService.storeApartmentPhoto(id, file);
        int nextOrder = apartmentPhotoRepository.findByApartment_IdOrderByDisplayOrderAsc(id).size();

        ApartmentPhoto photo = new ApartmentPhoto();
        photo.setApartment(apartment);
        photo.setFileName(fileName);
        photo.setDisplayOrder(nextOrder);
        photo.setAltText(altText);

        return apartmentPhotoRepository.save(photo);
    }

    @PutMapping("/{id}/photos/{photoId}")
    public ApartmentPhoto updatePhoto(@PathVariable Long id, @PathVariable Long photoId,
                                      @RequestBody UpdatePhotoRequest request) {
        ApartmentPhoto photo = apartmentPhotoRepository.findById(photoId)
                .orElseThrow(() -> new NoSuchElementException(Constants.APARTMENT_NOT_FOUND));

        photo.setAltText(request.altText());
        return apartmentPhotoRepository.save(photo);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public void deletePhoto(@PathVariable Long id, @PathVariable Long photoId) throws IOException {
        ApartmentPhoto photo = apartmentPhotoRepository.findById(photoId)
                .orElseThrow(() -> new NoSuchElementException(Constants.APARTMENT_NOT_FOUND));

        fileStorageService.deleteApartmentPhoto(id, photo.getFileName());
        apartmentPhotoRepository.delete(photo);
    }
}