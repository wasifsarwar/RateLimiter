public class Main {

    public static void main(String[] args) {
        System.out.println("Rate Limiter Testing");
        ratelimiter.RateLimiter rateLimiter = new ratelimiter.RateLimiter(5, 60);

        // The First 5 requests should be allowed
        System.out.println("Request at 100s: " + rateLimiter.isAllowed("user123", 100)); // 1st
        System.out.println("Request at 110s: " + rateLimiter.isAllowed("user123", 110)); // 2nd
        System.out.println("Request at 115s: " + rateLimiter.isAllowed("user123", 115)); // 3rd
        System.out.println("Request at 120s: " + rateLimiter.isAllowed("user123", 120)); // 4th
        System.out.println("Request at 125s: " + rateLimiter.isAllowed("user123", 125)); // 5th

        System.out.println("\n--- Limit should be reached ---\n");

        // 6th request should be rejected as it's within 60s of the first request
        System.out.println("Request at 130s: " + rateLimiter.isAllowed("user123", 130)); // 6th, should be false

        System.out.println("\n--- Window slides forward ---\n");

        // Request at 170s should be allowed as the window has slid forward.
        // The timestamp at 100s has expired (since 170 - 60 = 110).
        System.out.println("Request at 170s: " + rateLimiter.isAllowed("user123", 170)); // Should be true
    }
}
