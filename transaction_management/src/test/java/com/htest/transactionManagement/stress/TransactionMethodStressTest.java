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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class TransactionMethodStressTest {

    @Autowired
    private RestTemplate restTemplate;

    private final String baseUrl = "http://localhost:8080";
    private final Random random = new Random();

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

    public void createStressTest(int numberOfRequests, List<Long> transactionIds, AtomicInteger errorCount, AtomicInteger okCount) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis();
                try {
                    Transaction transaction = generateTransaction();
                    ResponseEntity<Transaction> createResponse = restTemplate.postForEntity(baseUrl + "/api/v1/transactions", transaction, Transaction.class);
                    if (createResponse.getStatusCode().is2xxSuccessful()) {
                        okCount.incrementAndGet();
                        Long transactionId = createResponse.getBody().getId();
                        transactionIds.add(transactionId);
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
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        printMetrics(new ArrayList<>(latencies), errorCount.get(), okCount.get(), startTime, endTime,"Create Transaction:");
    }

    public void updateStressTest(int numberOfRequests, List<Long> transactionIds, AtomicInteger errorCount, AtomicInteger okCount) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis();
                try {
                    if (!transactionIds.isEmpty()) {
                        Long transactionId = transactionIds.get(random.nextInt(transactionIds.size()));
                        Transaction transaction = generateTransaction();
                        transaction.setId(transactionId);
                        ResponseEntity<Transaction> updateResponse = restTemplate.exchange(baseUrl + "/api/v1/transactions/" + transactionId, HttpMethod.PUT, new HttpEntity<>(transaction), Transaction.class);
                        if (updateResponse.getStatusCode().is2xxSuccessful()) {
                            okCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
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
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        printMetrics(new ArrayList<>(latencies), errorCount.get(), okCount.get(), startTime, endTime, "Update Transaction:");
    }

    public void deleteStressTest(int numberOfRequests, List<Long> transactionIds, AtomicInteger errorCount, AtomicInteger okCount) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis();
                try {
                    if (!transactionIds.isEmpty()) {
                        Long transactionId = transactionIds.get(random.nextInt(transactionIds.size()));
                        restTemplate.delete(baseUrl + "/api/v1/transactions/" + transactionId);
                        okCount.incrementAndGet();
                        transactionIds.remove(transactionId);
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
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        printMetrics(new ArrayList<>(latencies), errorCount.get(), okCount.get(), startTime, endTime,"Delete Transaction:");
    }

    public void readStressTest(int numberOfRequests, AtomicInteger errorCount, AtomicInteger okCount) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Long> latencies = new CopyOnWriteArrayList<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis();
                try {
                    int page = random.nextInt(10);
                    int size = random.nextInt(10) + 1;
                    ResponseEntity<List<Transaction>> getResponse = restTemplate.getForEntity(baseUrl + "/api/v1/transactions?page=" + page + "&size=" + size, (Class<List<Transaction>>) (Class<?>) List.class);
                    if (getResponse.getStatusCode().is2xxSuccessful()) {
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
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        printMetrics(latencies, errorCount.get(), okCount.get(), startTime, endTime, "Query Transaction:");
    }

    private void printMetrics(List<Long> latencies, int errorCount, int totalCount, long startTime, long endTime, String category) {
        long totalRequests = totalCount + errorCount;
        double throughput = totalRequests / ((endTime - startTime) / 1000.0);
        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxLatency = latencies.stream().mapToLong(Long::longValue).max().orElse(0);
        Collections.sort(latencies);
        long tp90Latency = latencies.size() > 0 ? latencies.get((int) (latencies.size() * 0.9)) : 0;
        double errorRate = (double) errorCount / totalRequests * 100;

        System.out.println(category);
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Throughput: " + (int) throughput + " requests/sec");
        System.out.println("Average Latency: " + Math.round(avgLatency * 100) / 100.0 + " ms");
        System.out.println("Max Latency: " + maxLatency + " ms");
        System.out.println("Error Count: " + errorCount);
        System.out.println("Error Rate: " + Math.round(errorRate * 100) / 100.0 + "%");
        System.out.println("90th Percentile Latency: " + tp90Latency / 1000f + " ms\n");
    }

    @Test
    public void testStressTest() throws InterruptedException {
        List<Long> transactionIds = new ArrayList<>();
        AtomicInteger createErrorCount = new AtomicInteger(0);
        AtomicInteger createOkCount = new AtomicInteger(0);
        AtomicInteger updateErrorCount = new AtomicInteger(0);
        AtomicInteger updateOkCount = new AtomicInteger(0);
        AtomicInteger deleteErrorCount = new AtomicInteger(0);
        AtomicInteger deleteOkCount = new AtomicInteger(0);
        AtomicInteger readErrorCount = new AtomicInteger(0);
        AtomicInteger readOkCount = new AtomicInteger(0);

        createStressTest(100, transactionIds, createErrorCount, createOkCount);

        updateStressTest(100, transactionIds, updateErrorCount, updateOkCount);

        readStressTest(100, readErrorCount, readOkCount);

        // 执行删除压力测试
        deleteStressTest(100, transactionIds, deleteErrorCount, deleteOkCount);
    }
}