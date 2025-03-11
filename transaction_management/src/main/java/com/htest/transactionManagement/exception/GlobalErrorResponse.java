package com.htest.transactionManagement.exception;

import java.time.LocalDateTime;

public record GlobalErrorResponse(int status, String message, LocalDateTime timestamp) {
}
