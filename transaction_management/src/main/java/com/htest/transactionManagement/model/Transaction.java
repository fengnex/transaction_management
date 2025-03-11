package com.htest.transactionManagement.model;

import jakarta.validation.constraints.*;
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
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Source account number must contain only alphanumeric characters")
    private String sourceAccountNumber;

    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Destination account number must contain only alphanumeric characters")
    private String destinationAccountNumber;

    @NotNull(message = "Currency cannot be null")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;

    @PositiveOrZero(message = "Exchange rate must be positive or zero")
    private BigDecimal exchangeRate;

    @NotNull(message = "Category cannot be null")
    private TransactionCategory category;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    private LocalDateTime timestamp;

    @NotNull(message = "Status cannot be null")
    private TransactionStatus status;

    @NotNull(message = "riskLevel cannot be null")
    private RiskLevel riskLevel;

    @Size(max = 50, message = "Reference number cannot exceed 50 characters")
    private String referenceNumber;

    private LocalDateTime processedTime;
    @Size(max = 50, message = "Processed by cannot exceed 50 characters")
    private String processedBy;

    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    private String remarks;
    private Boolean isReconciled;

    private Boolean isFraudSuspected;

    @Pattern(regexp = "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$",
            message = "Invalid IP address (IPv4 or IPv6)")
    private String ipAddress;

    @Size(max = 255, message = "Device info cannot exceed 255 characters")
    private String deviceInfo;
}
