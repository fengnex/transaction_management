package com.htest.transactionManagement.stress;

import com.htest.transactionManagement.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class TransactionStressTest {

    @Autowired
    private RestTemplate restTemplate;

    private final String baseUrl = "http://localhost:8080";

    private static Transaction generateTransaction() {
        Transaction transaction = new Transaction();

        transaction.setAmount(BigDecimal.valueOf(123));
        transaction.setCurrency("CNY");
        transaction.setStatus(TransactionStatus.INITIATED);
        transaction.setSourceAccountNumber("1234");
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setRiskLevel(RiskLevel.LOW);
        transaction.setCategory(TransactionCategory.INSURANCE);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setRemarks("Test transaction remarks");
        return transaction;
    }

    public void stressTest(int numberOfRequests, int durationInSeconds) throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Long> latencies = new CopyOnWriteArrayList<>();
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger okCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfRequests; i++) {
            final int requestIndex = i;
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis();
                try {
                    Transaction transaction = generateTransaction();
                    ResponseEntity<Transaction> createResponse = restTemplate.postForEntity(baseUrl + "/api/v1/transactions", transaction, Transaction.class);
                    if (createResponse.getStatusCode().is2xxSuccessful()) {
                        okCount.incrementAndGet();
                        Long transactionId = createResponse.getBody().getId();
                        transaction.setId(transactionId);
                        ResponseEntity<Transaction> updateResponse = restTemplate.exchange(baseUrl + "/api/v1/transactions/" + transactionId, HttpMethod.PUT, new HttpEntity<>(transaction), Transaction.class);

                        if (!updateResponse.getStatusCode().is2xxSuccessful()) {
                            errorCount.incrementAndGet();
                        } else {
                            okCount.incrementAndGet();
                        }

                        for (int j = 0; j < 100; j++) {
                            ResponseEntity<Transaction> getResponse = restTemplate.getForEntity(baseUrl + "/api/v1/transactions/" + transactionId, Transaction.class);
                            if (!getResponse.getStatusCode().is2xxSuccessful()) {
                                errorCount.incrementAndGet();
                            } else {
                                okCount.incrementAndGet();
                            }
                        }

                        restTemplate.delete(baseUrl + "/api/v1/transactions/" + transactionId);
                        okCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    long requestEndTime = System.currentTimeMillis();
                    latencies.add(requestEndTime - requestStartTime);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(durationInSeconds, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        List<Long> copiedLatencies = new ArrayList<>(latencies);
        printMetrics(copiedLatencies, errorCount.get(), okCount.get(), startTime, endTime);
    }

    private void printMetrics(List<Long> latencies, int errorCount, int totalCount, long startTime, long endTime) {
        long totalRequests = totalCount + errorCount;
        double throughput = totalRequests / ((endTime - startTime) / 1000.0);

        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxLatency = latencies.stream().mapToLong(Long::longValue).max().orElse(0);

        // Calculate 90th percentile latency
        Collections.sort(latencies);
        long tp90Latency = latencies.size() > 0 ? latencies.get((int) (latencies.size() * 0.9)) : 0;
        double errorRate = (double) errorCount / totalRequests * 100;

        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Throughput: " + (int) throughput + " requests/sec");
        System.out.println("Average Latency: " + Math.round(avgLatency * 100) / 100.0 + " ms");
        System.out.println("Max Latency: " + maxLatency + " ms");
        System.out.println("Error Count: " + errorCount);
        System.out.println("Error Rate: " + Math.round(errorRate * 100) / 100.0 + "%");
        System.out.println("90th Percentile Latency: " + tp90Latency / 1000f + " ms");
    }

    @Test
    public void testStressTest() throws InterruptedException {
        stressTest(1000000, 30);
    }
}