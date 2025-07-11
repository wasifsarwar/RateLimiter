package com.ratelimitersingleinstance;

import java.util.*;

public class RateLimiterSingleInstance {

    private final int rate;
    private final int timeWindow;

    // Maps user to a queue of request timestamps
    private final Map<String, Queue<Long>> requestMap;

    public RateLimiterSingleInstance(int rate, int timeWindow) {
        if (rate <= 0 || timeWindow <= 0) {
            throw new IllegalArgumentException("Rate and time window must be positive.");
        }

        this.rate = rate;
        this.timeWindow = timeWindow;
        this.requestMap = new HashMap<>();
    }

    /**
     * Determines if a request should be allowed or rejected for a given user.
     *
     * @param userId      The unique identifier for the user. Must not be null.
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

        requestMap.computeIfAbsent(userId, k -> new LinkedList<>()); // add new Queue if it's a new user
        Queue<Long> timestamps = requestMap.get(userId);

        long windowStart = currentTime - timeWindow; // start of a sliding window

        // remove old timestamps that are below new windowStart
        while (!timestamps.isEmpty() && timestamps.peek() <= windowStart) {
            timestamps.poll();
        }

        // if there's fewer requests than the rate limit, incoming request is valid

        if (timestamps.size() < rate) {
            timestamps.offer(currentTime);
            return true;
        }

        return false;
    }

}
