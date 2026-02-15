package com.Transpo.transpo.dto;

public class PaymentValidationResponseDTO {
    private boolean valid;
    private String message;

    public PaymentValidationResponseDTO() {}
    public PaymentValidationResponseDTO(boolean valid, String message) {
        this.valid = valid; this.message = message;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
