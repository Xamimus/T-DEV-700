package com.api.bank.model.transaction;

public enum TransactionStatus {
    SUCCESS,
    PAYMENT_ERROR,
    CARD_ERROR,
    INSUFFICIENT_FUNDS_ERROR,
    OPERATION_PENDING_ERROR,
}
