package com.reicar.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CustomerStatementDTO(
    Long customerId,
    String customerName,
    String customerPhone,
    String customerCity,
    String customerState,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate generatedAt,
    List<InvoiceDTO> invoices,
    List<PaymentDTO> payments,
    BigDecimal totalInvoiced,
    BigDecimal totalPaid,
    BigDecimal balance
) {}
