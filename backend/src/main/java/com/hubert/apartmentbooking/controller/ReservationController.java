package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.CreateReservationRequest;
import com.hubert.apartmentbooking.dto.response.ReservationResponse;
import com.hubert.apartmentbooking.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Constants.RESERVATIONS_PATH)
@Tag(name = "Reservations", description = "Creating and looking up reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request, Authentication authentication) {
        return reservationService.createReservation(request, authentication);
    }

    @GetMapping("/{accessToken}")
    public ReservationResponse getReservation(@PathVariable String accessToken) {
        return reservationService.getByAccessToken(accessToken);
    }

    @GetMapping(Constants.MY_RESERVATIONS_PATH)
    public List<ReservationResponse> getMyReservations(Authentication authentication) {
        return reservationService.getMyReservations(authentication.getName());
    }
}