package com.hotel.service;

import com.hotel.model.Payment;
import com.hotel.model.PaymentMethod;
import com.hotel.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service simulating payment gateway interactions, authorization, and refunds.
 */
public class PaymentService {
    private final Random random = new Random();

    /**
     * Simulates processing a customer payment through various gateways.
     */
    public Payment processPayment(double amount, PaymentMethod method, String paymentDetail) {
        String txnId = generateTransactionId("TXN");
        String note;

        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                String masked = maskCardNumber(paymentDetail);
                note = "Card ending in " + masked + " approved via SecurePay Gateway";
                break;
            case UPI:
                note = "Instant UPI settlement verified for VPA: " + paymentDetail;
                break;
            case CASH:
            default:
                note = "Cash collected and receipt issued at Front Desk counter";
                break;
        }

        return new Payment(txnId, amount, method, LocalDateTime.now(), PaymentStatus.COMPLETED, note);
    }

    /**
     * Simulates issuing a refund when a reservation is cancelled.
     */
    public Payment processRefund(Payment originalPayment, double refundAmount, String reason) {
        String refId = generateTransactionId("REF");
        String note = "Refund of $" + String.format("%.2f", refundAmount) + " processed for " +
                originalPayment.getTransactionId() + " (" + reason + ")";

        return new Payment(refId, refundAmount, originalPayment.getMethod(),
                LocalDateTime.now(), PaymentStatus.REFUNDED, note);
    }

    private String generateTransactionId(String prefix) {
        int number = 100000 + random.nextInt(900000);
        return prefix + "-" + number;
    }

    private String maskCardNumber(String card) {
        if (card == null || card.length() < 4) return "****";
        String clean = card.replaceAll("\\s+", "").replaceAll("-", "");
        if (clean.length() <= 4) return clean;
        return clean.substring(clean.length() - 4);
    }
}
