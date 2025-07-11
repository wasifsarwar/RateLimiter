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
