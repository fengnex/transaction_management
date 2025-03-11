package com.htest.transactionManagement.service;

import com.htest.transactionManagement.exception.TransactionNotFoundException;
import com.htest.transactionManagement.model.*;
import com.htest.transactionManagement.util.SnowflakeIdGenerator;
import com.htest.transactionManagement.util.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceImplTest {

    private TransactionService transactionService;
    private TestClock testClock;

    @BeforeEach
    void setUp() {
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
        testClock = new TestClock(LocalDateTime.now());
        transactionService = new TransactionServiceImpl(idGenerator, testClock);
    }

    @Test
    void createTransaction_ShouldCreateSuccessfully() {
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("1234567890")
                .currency("CNY")
                .category(TransactionCategory.SALARY)
                .description("Test deposit")
                .riskLevel(RiskLevel.LOW)
                .isReconciled(false)
                .isFraudSuspected(false)
                .build();

        Transaction created = transactionService.createTransaction(transaction);

        assertNotNull(created.getId());
        assertNotNull(created.getTimestamp());
        assertEquals(transaction.getAmount(), created.getAmount());
        assertEquals(transaction.getType(), created.getType());
        assertEquals(transaction.getSourceAccountNumber(), created.getSourceAccountNumber());
        assertEquals(transaction.getCurrency(), created.getCurrency());
        assertEquals(transaction.getCategory(), created.getCategory());
    }

    @Test
    void createTransaction_WithDifferentAmounts_ShouldNotDetectAsDuplicate() {
        // First transaction
        Transaction transaction1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .description("First deposit")
                .build();

        transactionService.createTransaction(transaction1);

        // Second transaction with different amount
        Transaction transaction2 = Transaction.builder()
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .description("Second deposit")
                .build();

        Transaction created = transactionService.createTransaction(transaction2);
        assertNotNull(created.getId());
        assertEquals(TransactionStatus.INITIATED, created.getStatus());
    }

    @Test
    void createTransaction_AfterTimeWindow_ShouldNotDetectAsDuplicate() {
        // First transaction
        Transaction transaction1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .description("First deposit")
                .build();

        transactionService.createTransaction(transaction1);

        // 使用 TestClock 推进时间 6 秒，而不是实际等待
        testClock.advanceSeconds(6);

        // Similar transaction after time window
        Transaction transaction2 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .description("Second deposit")
                .build();

        Transaction created = transactionService.createTransaction(transaction2);
        assertNotNull(created.getId());
        assertEquals(TransactionStatus.INITIATED, created.getStatus());
    }

    @Test
    void getTransaction_ShouldThrowException_WhenNotFound() {
        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransaction(-1L));
    }

    @Test
    void getAllTransactions_ShouldReturnPagedResults() {
        // Create test transactions with different amounts to avoid duplicate detection
        for (int i = 0; i < 5; i++) {
            Transaction transaction = Transaction.builder()
                    .amount(new BigDecimal(100 + i + ".00"))
                    .type(TransactionType.DEPOSIT)
                    .description("Test transaction " + i)
                    .build();
            transactionService.createTransaction(transaction);
        }

        Page<Transaction> transactions = transactionService.getAllTransactions(PageRequest.of(0, 3));

        assertEquals(3, transactions.getContent().size());
        assertEquals(5, transactions.getTotalElements());
    }

    @Test
    void createTransaction_WithFailedStatus_ShouldAllowDuplicate() {
        // First transaction (Failed)
        Transaction transaction1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .description("Failed deposit")
                .status(TransactionStatus.FAILED)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .build();

        transactionService.createTransaction(transaction1);

        // Same transaction again
        Transaction transaction2 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .description("Retry deposit")
                .sourceAccountNumber("12345")
                .currency("CNY")
                .build();

        Transaction created = transactionService.createTransaction(transaction2);
        assertNotNull(created.getId());
        assertEquals(TransactionStatus.INITIATED, created.getStatus());
    }

    @Test
    void updateTransaction_ToFailedStatus_ShouldAllowDuplicate() {
        // Create initial transaction
        Transaction transaction1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .description("Initial deposit")
                .build();

        Transaction created = transactionService.createTransaction(transaction1);

        // Update to failed status
        created.setStatus(TransactionStatus.FAILED);
        transactionService.updateTransaction(created.getId(), created);

        // Try same transaction again
        Transaction transaction2 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .description("Retry deposit")
                .build();

        assertDoesNotThrow(() -> transactionService.createTransaction(transaction2));
    }
}
