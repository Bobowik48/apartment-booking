package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.response.PaymentInitResponse;
import com.hubert.apartmentbooking.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.PAYMENTS_PATH)
@Tag(name = "Payments", description = "PayU checkout initiation and payment notifications")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{reservationId}")
    public PaymentInitResponse initPayment(@PathVariable Long reservationId, HttpServletRequest request) {
        return paymentService.initPayment(reservationId, request.getRemoteAddr());
    }

    @PostMapping(Constants.NOTIFY_ENDPOINT)
    public ResponseEntity<Void> handleNotification(@RequestBody String rawBody,
                                                   @RequestHeader("OpenPayu-Signature") String signatureHeader) {
        paymentService.handleNotification(rawBody, signatureHeader);
        return ResponseEntity.ok().build();
    }
}