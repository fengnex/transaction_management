package com.htest.transactionManagement.stress;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

/**
 * Stress test TransactionController with virtual thread
 */
public class VirtualThreadStressTest {
    private static final String BASE_URL = "http://localhost:8080/api/v1/transactions";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // statistics information
    private static final Map<String, LongAdder> successCounters = new ConcurrentHashMap<>();
    private static final Map<String, LongAdder> failureCounters = new ConcurrentHashMap<>();
    private static final Map<String, List<Long>> responseTimes = new ConcurrentHashMap<>();
    private static final int durationSeconds = 60;  // stress test duration

    public static void main(String[] args) throws Exception {
        int numThreads = 1000;  // number of virtual threads

        System.out.println("Starting stress test with " + numThreads + " virtual threads for " + durationSeconds + " seconds");

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            Instant startTime = Instant.now();

            for (int i = 0; i < numThreads; i++) {
                futures.add(executor.submit(() -> runTestScenario(startTime, durationSeconds)));
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        printResults();
    }

    private static void runTestScenario(Instant startTime, int durationSeconds) {
        Random random = new Random();

        while (Duration.between(startTime, Instant.now()).getSeconds() < durationSeconds) {
            try {
                executeCreate();
                executeGetAll();
                executeUpdate(random.nextInt(10) + 1);
                executeDelete(random.nextInt(10) + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void executeGetAll() {
        executeRequest("GET_ALL", () -> {
            var request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        });
    }

    private static void executeCreate() {
        var transaction = generateTransaction();
        executeRequest("CREATE", () -> {
            var request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(transaction)))
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return send;
        });
    }

    private static void executeUpdate(int id) {
        var transaction = generateTransaction();
        executeRequest("UPDATE", () -> {
            var request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(transaction)))
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        });
    }

    private static void executeDelete(int id) {
        executeRequest("DELETE", () -> {
            var request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        });
    }

    private static void executeRequest(String operation, RequestExecutor executor) {
        long startTime = System.nanoTime();
        try {
            HttpResponse<String> response = executor.execute();
            long duration = (System.nanoTime() - startTime) / 1_000_000;

            synchronized (responseTimes) {
                responseTimes.computeIfAbsent(operation, k -> new ArrayList<>()).add(duration);
            }

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                successCounters.computeIfAbsent(operation, k -> new LongAdder()).increment();
            } else {
                failureCounters.computeIfAbsent(operation, k -> new LongAdder()).increment();
            }
        } catch (Exception e) {
            failureCounters.computeIfAbsent(operation, k -> new LongAdder()).increment();
        }
    }

    private static Map<String, Object> generateTransaction() {
        Random random = new Random();
        String[] types = {"DEPOSIT", "WITHDRAWAL"};
        String[] categories = {"SALARY", "SHOPPING", "INVESTMENT"};
        String[] riskLevels = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", random.nextDouble() * 1000);
        transaction.put("currency", "CNY");
        transaction.put("status", "INITIATED");
        transaction.put("sourceAccountNumber", random.nextInt(1000000, 1000000000));
        transaction.put("type", types[random.nextInt(types.length)]);
        transaction.put("riskLevel", riskLevels[random.nextInt(riskLevels.length)]);
        transaction.put("category", categories[random.nextInt(categories.length)]);
        transaction.put("description", "Test transaction ");
        transaction.put("remarks", "Test remarks ");
        transaction.put("timestamp", LocalDateTime.now().toString());

        return transaction;
    }

    private static void printResults() {
        System.out.println("\nStress Test Results:");
        System.out.println("=================");

        Set<String> operations = new HashSet<>();
        operations.addAll(successCounters.keySet());
        operations.addAll(failureCounters.keySet());

        for (String operation : operations) {
            long successes = successCounters.getOrDefault(operation, new LongAdder()).sum();
            long failures = failureCounters.getOrDefault(operation, new LongAdder()).sum();
            List<Long> times = responseTimes.getOrDefault(operation, List.of());

            double avgResponseTime = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long maxResponseTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
            long minResponseTime = times.stream().mapToLong(Long::longValue).min().orElse(0);

            System.out.println("\nOperation: " + operation);
            System.out.println("Successes: " + successes);
            System.out.println("Failures: " + failures);
            System.out.println("Throughput: " + (successes + failures) / durationSeconds);
            System.out.println("Average Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
            System.out.println("Min Response Time: " + minResponseTime + "ms");
            System.out.println("Max Response Time: " + maxResponseTime + "ms");
        }
    }

    @FunctionalInterface
    interface RequestExecutor {
        HttpResponse<String> execute() throws Exception;
    }
} 