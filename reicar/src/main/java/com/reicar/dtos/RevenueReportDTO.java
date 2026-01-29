package com.reicar.dtos;

import com.reicar.entities.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record RevenueReportDTO(
    LocalDate startDate,
    LocalDate endDate,
    LocalDate generatedAt,
    BigDecimal totalInvoiced,
    BigDecimal totalReceived,
    BigDecimal outstandingBalance,
    int invoiceCount,
    int paidInvoiceCount,
    int unpaidInvoiceCount,
    int partialInvoiceCount,
    Map<PaymentMethod, BigDecimal> revenueByMethod,
    List<InvoiceDTO> invoices
) {}
