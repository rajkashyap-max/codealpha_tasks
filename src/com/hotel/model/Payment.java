package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Encapsulates payment transaction details.
 */
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String transactionId;
    private double amount;
    private PaymentMethod method;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String notes;

    public Payment(String transactionId, double amount, PaymentMethod method, LocalDateTime paymentDate, PaymentStatus status, String notes) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.method = method;
        this.paymentDate = paymentDate;
        this.status = status;
        this.notes = notes;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String toDataString() {
        return String.format("%s~%.2f~%s~%s~%s~%s",
                transactionId,
                amount,
                method.name(),
                paymentDate.format(FORMATTER),
                status.name(),
                notes.replace("~", "-")
        );
    }

    public static Payment fromDataString(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String[] parts = text.split("~");
        if (parts.length < 6) return null;

        String txnId = parts[0].trim();
        double amount = Double.parseDouble(parts[1].trim());
        PaymentMethod method = PaymentMethod.valueOf(parts[2].trim());
        LocalDateTime date = LocalDateTime.parse(parts[3].trim(), FORMATTER);
        PaymentStatus status = PaymentStatus.valueOf(parts[4].trim());
        String notes = parts[5].trim();

        return new Payment(txnId, amount, method, date, status, notes);
    }

    @Override
    public String toString() {
        return String.format("TXN: %s | $%.2f via %s | Status: %s | %s",
                transactionId, amount, method.getDisplayName(), status,
                paymentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
