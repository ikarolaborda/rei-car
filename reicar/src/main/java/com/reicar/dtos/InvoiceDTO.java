package com.reicar.dtos;

import com.reicar.entities.Invoice;
import com.reicar.entities.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDTO(
    Long id,
    String invoiceNumber,
    LocalDate issueDate,
    InvoiceStatus status,
    Long serviceOrderId,
    String serviceOrderNumber,
    Long customerId,
    String customerName,
    String customerPhone,
    BigDecimal totalValue,
    BigDecimal paidAmount,
    BigDecimal remainingBalance,
    List<PaymentDTO> payments
) {
    public static InvoiceDTO from(Invoice invoice) {
        List<PaymentDTO> paymentDTOs = invoice.getPayments() != null
            ? invoice.getPayments().stream().map(PaymentDTO::from).toList()
            : List.of();

        return new InvoiceDTO(
            invoice.getId(),
            invoice.getInvoiceNumber(),
            invoice.getIssueDate(),
            invoice.getStatus(),
            invoice.getServiceOrder() != null ? invoice.getServiceOrder().getId() : null,
            invoice.getServiceOrder() != null ? invoice.getServiceOrder().getOrderNumber() : null,
            invoice.getCustomer() != null ? invoice.getCustomer().getId() : null,
            invoice.getCustomer() != null ? invoice.getCustomer().getName() : null,
            invoice.getCustomer() != null ? invoice.getCustomer().getPhone() : null,
            invoice.getTotalValue(),
            invoice.getPaidAmount(),
            invoice.getRemainingBalance(),
            paymentDTOs
        );
    }
}
