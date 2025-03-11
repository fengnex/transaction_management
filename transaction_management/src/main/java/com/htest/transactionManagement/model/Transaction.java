package com.htest.transactionManagement.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Long id;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;

    @NotNull(message = "Source account cannot be null")
    private String sourceAccountNumber;

    private String destinationAccountNumber;

    @NotNull(message = "Currency cannot be null")
    private String currency;

    @Positive(message = "Exchange rate must be positive")
    private BigDecimal exchangeRate;

    @NotNull(message = "Category cannot be null")
    private TransactionCategory category;

    private String description;
    private LocalDateTime timestamp;

    @NotNull(message = "Status cannot be null")
    private TransactionStatus status;

    @NotNull(message = "riskLevel cannot be null")
    private RiskLevel riskLevel;

    private String referenceNumber;

    private LocalDateTime processedTime;
    private String processedBy;
    private String remarks;
    private Boolean isReconciled;

    private Boolean isFraudSuspected;
    private String ipAddress;
    private String deviceInfo;
}
