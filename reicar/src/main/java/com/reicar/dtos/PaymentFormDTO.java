package com.reicar.dtos;

import com.reicar.entities.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentFormDTO(
    @NotNull(message = "ID da fatura é obrigatório")
    Long invoiceId,

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    BigDecimal amount,

    @NotNull(message = "Método de pagamento é obrigatório")
    PaymentMethod paymentMethod
) {
    public PaymentFormDTO() {
        this(null, null, null);
    }
}
