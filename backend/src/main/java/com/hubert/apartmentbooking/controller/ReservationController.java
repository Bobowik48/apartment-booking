package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.CreateReservationRequest;
import com.hubert.apartmentbooking.dto.response.ReservationResponse;
import com.hubert.apartmentbooking.service.ReservationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.RESERVATIONS_PATH)
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ReservationResponse createReservation(@RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @GetMapping("/{accessToken}")
    public ReservationResponse getReservation(@PathVariable String accessToken) {
        return reservationService.getByAccessToken(accessToken);
    }
}