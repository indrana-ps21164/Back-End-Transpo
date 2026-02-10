package com.Transpo.transpo.service;

import com.Transpo.transpo.dto.PaymentRequestDTO;
import com.Transpo.transpo.dto.PaymentResponseDTO;
import com.Transpo.transpo.exception.NotFoundException;
import com.Transpo.transpo.model.Reservation;
import com.Transpo.transpo.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;

    public PaymentService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public PaymentResponseDTO pay(PaymentRequestDTO req) {
        if (req == null || req.getReservationId() == null) {
            return new PaymentResponseDTO(null, "FAILED", "Reservation ID required", req != null ? req.getMethod() : null, req != null ? req.getReference() : null);
        }
        Reservation reservation = reservationRepository.findById(req.getReservationId())
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        // Simple dummy logic: mark as paid and persist
        reservation.setPaid(true);
        reservation.setPaymentMethod(req.getMethod());
        reservation.setPaymentReference(req.getReference());
        reservationRepository.save(reservation);

        return new PaymentResponseDTO(reservation.getId(), "SUCCESS", "Payment processed successfully", req.getMethod(), req.getReference());
    }
}