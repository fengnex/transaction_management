package com.htest.transactionManagement.controller;

import com.htest.transactionManagement.model.Transaction;
import com.htest.transactionManagement.service.TransactionService;
import com.htest.transactionManagement.validator.TransactionValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionValidator transactionValidator;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        return Mono.just(transaction)
                .flatMap(this::validateTransaction) // moved validation to a separate method
                .map(transactionService::createTransaction);
    }

    private Mono<Transaction> validateTransaction(Transaction transaction) {
        Errors errors = new BeanPropertyBindingResult(transaction, "transaction");
        transactionValidator.validate(transaction, errors);
        if (errors.hasErrors()) {
            // Collect error messages in a more efficient way
            String errorMessage = errors.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage));
        }
        return Mono.just(transaction);
    }

    @PutMapping("/{id}")
    public Mono<Transaction> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody Transaction transaction) {
        Mono<Transaction> errors1 = getTransactionMono(transaction);
        if (errors1 != null) return errors1;

        return Mono.just(transaction)
                .map(t -> transactionService.updateTransaction(id, t));
    }

    private Mono<Transaction> getTransactionMono(@RequestBody @Valid Transaction transaction) {
        Errors errors = new BeanPropertyBindingResult(transaction, "transaction");
        transactionValidator.validate(transaction, errors);

        if (errors.hasErrors()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    errors.getFieldErrors().stream()
                            .map(error -> error.getField() + ": " + error.getDefaultMessage())
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("Validation failed")
            ));
        }
        return null;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteTransaction(@PathVariable Long id) {
        return Mono.fromRunnable(() -> transactionService.deleteTransaction(id));
    }

    @GetMapping("/{id}")
    public Mono<Transaction> getTransaction(@PathVariable Long id) {
        return Mono.fromCallable(() -> transactionService.getTransaction(id));
    }

    @GetMapping
    public Mono<Page<Transaction>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Mono.fromCallable(() ->
                transactionService.getAllTransactions(PageRequest.of(page, size))
        );
    }
}
