package com.reicar.entities.enums;

public enum PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    PIX,
    BANK_TRANSFER;

    public String getDisplayName() {
        return switch (this) {
            case CASH -> "Dinheiro";
            case CREDIT_CARD -> "Cartão de Crédito";
            case DEBIT_CARD -> "Cartão de Débito";
            case PIX -> "PIX";
            case BANK_TRANSFER -> "Transferência Bancária";
        };
    }
}
