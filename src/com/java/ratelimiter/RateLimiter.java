package com.java.ratelimiter;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class RateLimiter {

    private final int rate;
    private final int timeWindow; // in seconds
    private final LoadingCache<String, Queue<Long>> customerCaches;

    public RateLimiter(int rate, int timeWindow) {
        if (rate <= 0 || timeWindow <= 0) {
            throw new IllegalArgumentException("Rate and time window must be positive.");
        }
        this.rate = rate;
        this.timeWindow = timeWindow;
        this.customerCaches = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES) // Evict inactive customers
                .build(k -> new ConcurrentLinkedQueue<>());
    }

    /**
     * Determines if a request should be allowed or rejected for a given customer.
     * This method is thread-safe and uses fine-grained locking for high performance.
     *
     * @param customerId  The unique identifier for the customer. Must not be null.
     * @param currentTime The current time of the request in seconds since the
     *                    epoch. Must be non-negative.
     * @return True if the request is allowed, false otherwise.
     * @throws IllegalArgumentException if customerId is null/empty or currentTime
     *                                  is negative.
     */
    public boolean isAllowed(String customerId, long currentTime) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty.");
        }
        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }

        // Get the thread-safe queue from the cache.
        // The cache automatically creates a new queue for a new customer.
        Queue<Long> timestamps = customerCaches.get(customerId);

        // Synchronize on the specific customer's queue for fine-grained locking.
        synchronized (timestamps) {
            long windowStart = currentTime - timeWindow;

            // Remove timestamps older than the current window.
            while (!timestamps.isEmpty() && timestamps.peek() <= windowStart) {
                timestamps.poll();
            }

            // Check if the request is within the rate limit.
            if (timestamps.size() < rate) {
                timestamps.offer(currentTime);
                return true;
            }
        }

        // If the limit is reached, reject the request.
        return false;
    }
}