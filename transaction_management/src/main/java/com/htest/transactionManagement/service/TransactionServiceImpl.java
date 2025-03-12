package com.htest.transactionManagement.service;

import com.htest.transactionManagement.exception.DuplicateTransactionException;
import com.htest.transactionManagement.exception.TransactionNotFoundException;
import com.htest.transactionManagement.model.Transaction;
import com.htest.transactionManagement.model.TransactionStatus;
import com.htest.transactionManagement.util.Clock;
import com.htest.transactionManagement.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final Map<Long, Transaction> transactionStore = new ConcurrentHashMap<>();
    private final SnowflakeIdGenerator idGenerator;
    private final Map<String, Long> transactionHashIndex = new ConcurrentHashMap<>();
    private final Clock clock;

    public TransactionServiceImpl(SnowflakeIdGenerator idGenerator, Clock clock) {
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    private String calculateTransactionHash(Transaction transaction) {
        if (transaction.getStatus() != null &&
                (transaction.getStatus() == TransactionStatus.FAILED ||
                        transaction.getStatus() == TransactionStatus.CANCELLED ||
                        transaction.getStatus() == TransactionStatus.REVERSED ||
                        transaction.getStatus() == TransactionStatus.REJECTED)) {
            return null;
        }

        return String.format("%s_%s_%s_%s_%s",
                transaction.getAmount(),
                transaction.getType(),
                transaction.getSourceAccountNumber(),
                transaction.getCurrency(),
                transaction.getTimestamp().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    @Override
    @CachePut(value = "transactions", key = "#result.id")
    public Transaction createTransaction(Transaction transaction) {
        transaction.setId(idGenerator.nextId());
        transaction.setTimestamp(clock.now());
        if (transaction.getStatus() == null) {
            transaction.setStatus(TransactionStatus.INITIATED);
        }

        String transactionHash = calculateTransactionHash(transaction);

        if (transactionHash != null) {
            if (transactionHashIndex.containsKey(transactionHash)) {
                Long existingId = transactionHashIndex.get(transactionHash);
                Transaction existing = transactionStore.get(existingId);
                if (existing != null &&
                        ChronoUnit.SECONDS.between(existing.getTimestamp(), clock.now()) <= 5 &&
                        !isTerminalStatus(existing.getStatus())) {
                    throw new DuplicateTransactionException(
                            "Possible duplicate transaction detected within 5-second window");
                }
            }
            transactionHashIndex.put(transactionHash, transaction.getId());
        }

        transactionStore.put(transaction.getId(), transaction);

//        log.info("Created transaction with ID: {}", transaction.getId());
        return transaction;
    }

    @Override
    @CachePut(value = "transactions", key = "#id")
    public Transaction updateTransaction(Long id, Transaction transaction) {
        if (!transactionStore.containsKey(id)) {
            throw new TransactionNotFoundException("Transaction not found with ID: " + id);
        }

        Transaction oldTransaction = transactionStore.get(id);
        String oldHash = calculateTransactionHash(oldTransaction);

        if (oldHash != null) {
            transactionHashIndex.remove(oldHash);
        }

        transaction.setId(id);
        String newHash = calculateTransactionHash(transaction);
        if (newHash != null && !isTerminalStatus(transaction.getStatus())) {
            transactionHashIndex.put(newHash, id);
        }

        transactionStore.put(id, transaction);
//        log.info("Updated transaction with ID: {}", id);
        return transaction;
    }

    @Override
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransaction(Long id) {
        if (!transactionStore.containsKey(id)) {
            throw new TransactionNotFoundException("Transaction not found with ID: " + id);
        }

        Transaction transaction = transactionStore.get(id);
        String hash = calculateTransactionHash(transaction);
        if (hash != null) {
            transactionHashIndex.remove(hash);
        }

        transactionStore.remove(id);
        log.info("Deleted transaction with ID: {}", id);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id")
    public Transaction getTransaction(Long id) {
        Transaction transaction = transactionStore.get(id);
        if (transaction == null) {
            throw new TransactionNotFoundException("Transaction not found with ID: " + id);
        }
        return transaction;
    }

    @Override
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        List<Transaction> transactions = new ArrayList<>(transactionStore.values());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());

        return new PageImpl<>(
                transactions.subList(start, end),
                pageable,
                transactions.size()
        );
    }

    private boolean isTerminalStatus(TransactionStatus status) {
        return status == TransactionStatus.FAILED ||
                status == TransactionStatus.CANCELLED ||
                status == TransactionStatus.REVERSED ||
                status == TransactionStatus.REJECTED ||
                status == TransactionStatus.COMPLETED;
    }
}
