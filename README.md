# RateLimiter

This project contains two Java implementations of a rate limiter based on the sliding window log algorithm.

## Implementations

There's two approaches to this solution in this project. 
- `RateLimiterSingleInstance` : this approach is designed to address requests coming to a scalable, concurrent environment
- `RateLimiter` : this approach is designed for a single instance for non-scaled low-traffic environment


### 1. `RateLimiterSingleInstance`

*   **Single-Instance Logic:** Uses a simple `HashMap`.
*   **Not Thread-Safe:** Best used in a single-threaded context or for educational purposes.
*   **No Eviction:** The user map will grow indefinitely, so it's not ideal for long-running services with many unique users.

### 2. `RateLimiter`

*   **Thread-Safe:** Uses fine-grained locking to handle many requests at once without bottlenecks between different users.
*   **Memory-Efficient:** Built with a `Caffeine` cache to automatically remove data for inactive users, preventing memory leaks over time.


## Core Algorithm: Sliding Window Log

The main idea here is to keep an exact timestamp for every single request a user makes, and shift the window as requests keep coming in later times

Logic breakdown:

1.  **Find the User's History:** We look up the user and grab their queue of past request timestamps.
2.  **Clean Up Old Timestamps:** We calculate the start of our time window (for example, `currentTime - 60 seconds`). Then, we go through the user's queue from oldest to newest and remove any timestamps that are now outside that window. This is the "sliding" part.
3.  **Make the Decision:** After cleaning up the old ones, we just count how many timestamps are left. If that count is less than the rate limit, the request is good to go. We add the new request's timestamp to the queue.
4.  **Reject:** If the count is already at the limit, we reject the request and don't add the new timestamp.