package com.Transpo.transpo.dto;

public class PaymentResponseDTO {
    private Long reservationId;
    private String status; // SUCCESS or FAILED
    private String message;
    private String method;
    private String reference;

    public PaymentResponseDTO() {}
    public PaymentResponseDTO(Long reservationId, String status, String message, String method, String reference) {
        this.reservationId = reservationId;
        this.status = status;
        this.message = message;
        this.method = method;
        this.reference = reference;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}