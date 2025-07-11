package ratelimiter;

public class RateLimiterTest {
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // Create a new RateLimiter before each test
        // 3 requests per 10 seconds
        rateLimiter = new RateLimiter(3, 10);
    }

    @Test
    void testSuccessfulRequests() {
        // First 3 requests should be allowed
        assertTrue(rateLimiter.isAllowed("user1", 1), "1st request should be allowed");
        assertTrue(rateLimiter.isAllowed("user1", 2), "2nd request should be allowed");
        assertTrue(rateLimiter.isAllowed("user1", 3), "3rd request should be allowed");
    }

    @Test
    void testRejectedRequestWhenLimitExceeded() {
        rateLimiter.isAllowed("user1", 1);
        rateLimiter.isAllowed("user1", 2);
        rateLimiter.isAllowed("user1", 3);

        // Fourth request should be rejected
        assertFalse(rateLimiter.isAllowed("user1", 4), "4th request should be rejected");
    }

    @Test
    void testWindowSliding() {
        rateLimiter.isAllowed("user1", 1); // This one will expire
        rateLimiter.isAllowed("user1", 5);
        rateLimiter.isAllowed("user1", 8);

        // This request is at time 12. The window is (12 - 10) = 2.
        // The request at time 1 is now outside the window.
        assertTrue(rateLimiter.isAllowed("user1", 12), "Request after window slides should be allowed");
    }

    @Test
    void testDifferentCustomersAreIndependent() {
        assertTrue(rateLimiter.isAllowed("user1", 1));
        assertTrue(rateLimiter.isAllowed("user1", 2));
        assertTrue(rateLimiter.isAllowed("user1", 3));
        assertFalse(rateLimiter.isAllowed("user1", 4), "User1 should be rate limited");

        // User2 should be independent and have their own rate limit
        assertTrue(rateLimiter.isAllowed("user2", 5), "User2's first request should be allowed");
    }

    @Test
    void testConstructorWithInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RateLimiter(0, 10);
        }, "Rate of 0 should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            new RateLimiter(5, -1);
        }, "Negative time window should throw exception");
    }
}
