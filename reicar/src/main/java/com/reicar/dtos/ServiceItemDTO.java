package com.reicar.dtos;

import java.math.BigDecimal;

public record ServiceItemDTO(Integer quantity,
                             String description,
                             BigDecimal unitPrice) {}
