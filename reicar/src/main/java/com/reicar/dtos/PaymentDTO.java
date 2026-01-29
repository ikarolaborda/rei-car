package com.reicar.dtos;

import com.reicar.entities.Payment;
import com.reicar.entities.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(
    Long id,
    Long invoiceId,
    String invoiceNumber,
    String customerName,
    BigDecimal amount,
    LocalDateTime paymentDate,
    PaymentMethod paymentMethod,
    String paymentMethodDisplay,
    String recordedBy
) {
    public static PaymentDTO from(Payment payment) {
        return new PaymentDTO(
            payment.getId(),
            payment.getInvoice() != null ? payment.getInvoice().getId() : null,
            payment.getInvoice() != null ? payment.getInvoice().getInvoiceNumber() : null,
            payment.getInvoice() != null && payment.getInvoice().getCustomer() != null
                ? payment.getInvoice().getCustomer().getName() : null,
            payment.getAmount(),
            payment.getPaymentDate(),
            payment.getPaymentMethod(),
            payment.getPaymentMethod() != null ? payment.getPaymentMethod().getDisplayName() : null,
            payment.getRecordedBy()
        );
    }
}
