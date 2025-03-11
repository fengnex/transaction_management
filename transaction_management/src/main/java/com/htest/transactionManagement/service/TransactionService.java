package com.htest.transactionManagement.service;

import com.htest.transactionManagement.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    Transaction createTransaction(Transaction transaction);

    Transaction updateTransaction(Long id, Transaction transaction);

    boolean deleteTransaction(Long id);

    Transaction getTransaction(Long id);

    Page<Transaction> getAllTransactions(Pageable pageable);
}
