package com.Transpo.transpo.controller;

import com.Transpo.transpo.dto.PaymentValidationRequestDTO;
import com.Transpo.transpo.dto.PaymentValidationResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/validate")
    public ResponseEntity<PaymentValidationResponseDTO> validate(@RequestBody PaymentValidationRequestDTO req) {
        if (req == null) return ResponseEntity.badRequest().body(new PaymentValidationResponseDTO(false, "Invalid request"));
        final String num = req.getPaymentNumber() != null ? req.getPaymentNumber().replaceAll("\\s+", "") : "";
        final String cvv = req.getCvv() != null ? req.getCvv().trim() : "";
        final String expiry = req.getExpiry() != null ? req.getExpiry().trim() : "";

        if (!num.matches("^\\d{16}$")) {
            return ResponseEntity.ok(new PaymentValidationResponseDTO(false, "Payment number must be 16 digits"));
        }
        if (!cvv.matches("^\\d{3}$")) {
            return ResponseEntity.ok(new PaymentValidationResponseDTO(false, "Security key (CVV) must be 3 digits"));
        }
        // Accept MM/YY or MM/YYYY and ensure not in the past
        boolean expiryOk = false;
        YearMonth now = YearMonth.now();
        for (String pattern : new String[]{"MM/yy", "MM/yyyy"}) {
            try {
                YearMonth ym = YearMonth.parse(expiry, DateTimeFormatter.ofPattern(pattern));
                if (!ym.isBefore(now)) { expiryOk = true; break; }
            } catch (DateTimeParseException ignored) {}
        }
        if (!expiryOk) {
            return ResponseEntity.ok(new PaymentValidationResponseDTO(false, "Expiry date invalid or in the past"));
        }
        // Mock gateway: simple Luhn check optional; approve all passing validation
        return ResponseEntity.ok(new PaymentValidationResponseDTO(true, "Payment authorized"));
    }
}
