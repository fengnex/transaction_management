package com.htest.transactionManagement.config;

import com.htest.transactionManagement.service.TransactionService;
import com.htest.transactionManagement.util.Clock;
import com.htest.transactionManagement.util.TestClock;
//import com.htest.transactionManagement.validator.TransactionValidator;
import com.htest.transactionManagement.validator.TransactionValidator;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.time.LocalDateTime;

@TestConfiguration
@EnableWebFlux
public class TestConfig {
    @Bean
    public TransactionService transactionService() {
        return Mockito.mock(TransactionService.class);
    }

    @Bean
    public TransactionValidator transactionValidator() {
        return new TransactionValidator();
    }

    @Bean
    public Clock clock() {
        return new TestClock(LocalDateTime.now());
    }
}
