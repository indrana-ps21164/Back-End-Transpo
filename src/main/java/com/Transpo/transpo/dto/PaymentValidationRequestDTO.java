package com.Transpo.transpo.dto;

public class PaymentValidationRequestDTO {
    private String paymentNumber; // 16-digit card number
    private String expiry;        // MM/YY or MM/YYYY
    private String cvv;           // 3 digits

    public String getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(String paymentNumber) { this.paymentNumber = paymentNumber; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}
