package com.htest.transactionManagement.controller;

import com.htest.transactionManagement.exception.TransactionNotFoundException;
import com.htest.transactionManagement.model.Transaction;
import com.htest.transactionManagement.service.TransactionService;
import com.htest.transactionManagement.validator.TransactionValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<?>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody Transaction transaction) {
        return getTransactionMono(transaction)
                .flatMap(validTransaction -> {
                    // Proceed with the update if there are no validation errors
                    return Mono.defer(() -> {
                        try {
                            return Mono.just(ResponseEntity.ok(transactionService.updateTransaction(id, validTransaction)));
                        } catch (TransactionNotFoundException ex) {
                            return Mono.just(ResponseEntity.notFound().build());
                        }
                    });
                })
                .onErrorResume(ResponseStatusException.class, ex -> Mono.just(ResponseEntity.badRequest().body(ex.getReason())))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build())); // Handle empty case
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
        return Mono.just(transaction);
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
