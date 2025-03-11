package com.htest.transactionManagement.model;

public enum TransactionStatus {
    INITIATED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REVERSED,
    REJECTED,
    SUSPICIOUS
}
