package com.Transpo.transpo.dto;

public class PaymentRequestDTO {
    private Long reservationId;
    private String method; // e.g., CARD, CASH, WALLET
    private String reference; // optional txn reference

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}