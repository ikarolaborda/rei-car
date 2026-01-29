package com.reicar.dtos;

import java.math.BigDecimal;

public record DashboardMetricsDTO(
    BigDecimal dailyRevenue,
    BigDecimal weeklyRevenue,
    BigDecimal monthlyRevenue,
    int unpaidInvoiceCount,
    BigDecimal unpaidInvoiceTotal,
    int partialInvoiceCount,
    BigDecimal partialOutstandingTotal,
    BigDecimal totalOutstanding
) {}
