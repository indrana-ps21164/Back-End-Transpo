package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.PaymentRequestDTO;
import com.Transpo.transpo.dto.PaymentResponseDTO;
import com.Transpo.transpo.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> pay(@RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO result = paymentService.pay(request);
        return ResponseEntity.ok(result);
    }
}