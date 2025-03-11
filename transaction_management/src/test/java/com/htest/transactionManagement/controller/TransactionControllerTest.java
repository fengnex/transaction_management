package com.htest.transactionManagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htest.transactionManagement.TransactionManagementApplication;
import com.htest.transactionManagement.config.TestConfig;
import com.htest.transactionManagement.exception.DuplicateTransactionException;
import com.htest.transactionManagement.exception.TransactionNotFoundException;
import com.htest.transactionManagement.model.*;
import com.htest.transactionManagement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = TransactionController.class)
@Import({TestConfig.class, TransactionManagementApplication.class})
@AutoConfigureWebTestClient
class TransactionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .category(TransactionCategory.INSURANCE)
                .riskLevel(RiskLevel.LOW)
                .description("Test transaction")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    void createTransaction_ShouldReturnCreatedTransaction() throws Exception {
        when(transactionService.createTransaction(any(Transaction.class)))
                .thenReturn(testTransaction);

        testTransaction.setSourceAccountNumber("1234567890");
        testTransaction.setCurrency("CNY");
        testTransaction.setRiskLevel(RiskLevel.LOW);
        testTransaction.setRemarks("Test remarks");

        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testTransaction)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testTransaction.getId())
                .jsonPath("$.amount").isEqualTo(testTransaction.getAmount().doubleValue())
                .jsonPath("$.type").isEqualTo(testTransaction.getType().toString());
    }

    @Test
    void createTransaction_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Transaction invalidTransaction = Transaction.builder()
                .amount(new BigDecimal("-100.00")) // Invalid negative amount
                .build();

        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTransaction)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateTransaction_ShouldReturnUpdatedTransaction() throws Exception {
        when(transactionService.updateTransaction(eq(1L), any(Transaction.class)))
                .thenReturn(testTransaction);

        testTransaction.setSourceAccountNumber("1234567890");
        testTransaction.setCurrency("CNY");
        testTransaction.setCategory(TransactionCategory.ENTERTAINMENT);
        testTransaction.setRiskLevel(RiskLevel.LOW);
        testTransaction.setRemarks("Test remarks");

        webTestClient.put().uri("/api/v1/transactions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testTransaction)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testTransaction.getId())
                .jsonPath("$.amount").isEqualTo(testTransaction.getAmount().doubleValue());
    }

    @Test
    void updateTransaction_WhenNotFound_ShouldReturnNotFound() throws Exception {
        Transaction updateTransaction = Transaction.builder()
                .id(999L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .description("Test transaction")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .sourceAccountNumber("1234567890")
                .currency("CNY")
                .riskLevel(RiskLevel.LOW)
                .category(TransactionCategory.INSURANCE)
                .remarks("Test remarks")
                .build();

        when(transactionService.updateTransaction(eq(999L), any(Transaction.class)))
                .thenThrow(new TransactionNotFoundException("Transaction not found with ID: 999"));

        webTestClient.put().uri("/api/v1/transactions/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateTransaction)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteTransaction_ShouldReturnNoContent() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1L);

        webTestClient.delete().uri("/api/v1/transactions/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteTransaction_WhenNotFound_ShouldReturnNotFound() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1L);
        doThrow(new TransactionNotFoundException("Transaction not found with ID: 999"))
                .when(transactionService).deleteTransaction(999L);

        webTestClient.delete().uri("/api/v1/transactions/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTransaction_ShouldReturnTransaction() throws Exception {
        when(transactionService.getTransaction(1L)).thenReturn(testTransaction);

        webTestClient.get().uri("/api/v1/transactions/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(testTransaction.getId())
                .jsonPath("$.amount").isEqualTo(testTransaction.getAmount().doubleValue())
                .jsonPath("$.type").isEqualTo(testTransaction.getType().toString());
    }

    @Test
    void getTransaction_WhenNotFound_ShouldReturnNotFound() throws Exception {
        when(transactionService.getTransaction(999L))
                .thenThrow(new TransactionNotFoundException("Transaction not found with ID: 999"));

        webTestClient.get().uri("/api/v1/transactions/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllTransactions_ShouldReturnPageOfTransactions() throws Exception {
        Page<Transaction> page = new PageImpl<>(
                List.of(testTransaction),
                PageRequest.of(0, 10),
                1
        );

        when(transactionService.getAllTransactions(any(PageRequest.class))).thenReturn(page);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/transactions")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].id").isEqualTo(testTransaction.getId())
                .jsonPath("$.content[0].amount").isEqualTo(testTransaction.getAmount().doubleValue())
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void createTransaction_WhenDuplicate_ShouldReturnConflict() throws Exception {
        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(new DuplicateTransactionException("Possible duplicate transaction detected"));

        testTransaction.setSourceAccountNumber("1234567890");
        testTransaction.setCurrency("CNY");
        testTransaction.setCategory(TransactionCategory.TRANSFER);
        testTransaction.setRiskLevel(RiskLevel.LOW);
        testTransaction.setRemarks("Test remarks");

        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testTransaction)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Possible duplicate transaction detected");
    }

    @Test
    void createTransaction_WithTransferTypeButNoDestination_ShouldReturnBadRequest() {
        Transaction invalidTransaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.TRANSFER)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .status(TransactionStatus.INITIATED)
                .category(TransactionCategory.TRANSFER)
                .riskLevel(RiskLevel.LOW)
                .remarks("Test remarks")
                .description("Invalid transfer")
                .build();

        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTransaction)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertTrue(message.toString().contains("Destination account is required")));
    }

    @Test
    void createTransaction_WithForeignCurrencyButNoExchangeRate_ShouldReturnBadRequest() {
        Transaction invalidTransaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("12345")
                .currency("USD")
                .status(TransactionStatus.INITIATED)
                .category(TransactionCategory.SALARY)
                .riskLevel(RiskLevel.LOW)
                .remarks("Test remarks")
                .description("Invalid foreign currency transaction")
                .build();

        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTransaction)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertTrue(message.toString().contains("Exchange rate is required")));
    }

    @Test
    void createTransaction_WithHighRiskButNoRemarks_ShouldReturnBadRequest() {
        Transaction invalidTransaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .sourceAccountNumber("12345")
                .currency("CNY")
                .status(TransactionStatus.INITIATED)
                .category(TransactionCategory.INSURANCE)
                .riskLevel(RiskLevel.HIGH)
                .description("High risk transaction")
                .build();

        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTransaction)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertTrue(message.toString().contains("Remarks are required for high-risk transactions")));
    }
}
