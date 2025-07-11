package com.ratelimiter;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class RateLimiter {

    private final int rate;
    private final int timeWindow; // in seconds
    private final LoadingCache<String, Queue<Long>> userCaches;

    public RateLimiter(int rate, int timeWindow) {
        if (rate <= 0 || timeWindow <= 0) {
            throw new IllegalArgumentException("Rate and time window must be positive.");
        }
        this.rate = rate;
        this.timeWindow = timeWindow;

        /*
         * Special Map that holds smart cache for each userId.
         * Evicts entries automatically that aren't accessed for 10 minutes
         *
         * ConcurrentLinkedQueue allows atomic operations, hence it is thread
         * interruption safe
         */
        this.userCaches = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(k -> new ConcurrentLinkedQueue<>());
    }

    /**
     * Determines if a request should be allowed or rejected for a given user.
     * This method is thread-safe and uses synchronized thread locking for high
     * performance.
     *
     * @param userId  The unique identifier for the user. Must not be null.
     * @param currentTime The current time of the request in seconds since the
     *                    epoch. Must be non-negative.
     * @return True if the request is allowed, false if it should be rejected.
     * @throws IllegalArgumentException if userId is null/empty or currentTime
     *                                  is negative.
     */
    public boolean isAllowed(String userId, long currentTime) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }

        // Get the thread-safe queue from the cache.
        // The cache automatically creates a new queue for a new user.
        Queue<Long> timestamps = userCaches.get(userId);

        // Synchronize on the specific user's queue for fine-grained locking.
        synchronized (Objects.requireNonNull(timestamps)) {
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