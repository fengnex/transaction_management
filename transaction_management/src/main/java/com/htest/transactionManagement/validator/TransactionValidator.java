package com.htest.transactionManagement.validator;

import com.htest.transactionManagement.model.RiskLevel;
import com.htest.transactionManagement.model.Transaction;
import com.htest.transactionManagement.model.TransactionType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TransactionValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Transaction.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Transaction transaction = (Transaction) target;

        if (transaction.getSourceAccountNumber() == null) {
            errors.rejectValue("sourceAccountNumber", "source.account.required",
                    "Source account number is required");
            return;
        }

        if (transaction.getCurrency() == null) {
            errors.rejectValue("currency", "currency.required",
                    "Currency is required");
            return;
        }

        if (transaction.getStatus() == null) {
            errors.rejectValue("status", "status.required",
                    "Status is required");
            return;
        }

        if (transaction.getRiskLevel() == null) {
            errors.rejectValue("riskLevel", "risk.level.required",
                    "Risk level is required");
            return;
        }

        if (TransactionType.TRANSFER.equals(transaction.getType())
                && transaction.getDestinationAccountNumber() == null) {
            errors.rejectValue("destinationAccountNumber", "transfer.destination.required",
                    "Destination account is required for transfer transactions");
        }

        if (!transaction.getCurrency().equals("CNY") &&
                transaction.getExchangeRate() == null) {
            errors.rejectValue("exchangeRate", "exchange.rate.required",
                    "Exchange rate is required for foreign currency transactions");
        }

        if (RiskLevel.HIGH.equals(transaction.getRiskLevel()) &&
                (transaction.getRemarks() == null || transaction.getRemarks().trim().isEmpty())) {
            errors.rejectValue("remarks", "high.risk.remarks.required",
                    "Remarks are required for high-risk transactions");
        }
    }
}