import java.util.HashMap;


class User {

    private String name;
    private String ip;


    User(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }


    String getName() {
        return name;
    }


    String getIp() {
        return ip;
    }
}


class Request {

    private User user;
    private long timestamp;


    Request(User user) {
        this.user = user;
        this.timestamp = System.currentTimeMillis();
    }


    User getUser() {
        return user;
    }


    long getTimestamp() {
        return timestamp;
    }
}


class RateLimitData {

    private double availableTokens;

    private long lastRefillTime;


    RateLimitData(
            int bucketCapacity,
            long currentTime) {

        // Initially bucket is full
        this.availableTokens = bucketCapacity;

        this.lastRefillTime = currentTime;
    }


    double getAvailableTokens() {
        return availableTokens;
    }


    void setAvailableTokens(
            double availableTokens) {

        this.availableTokens = availableTokens;
    }


    long getLastRefillTime() {
        return lastRefillTime;
    }


    void setLastRefillTime(
            long lastRefillTime) {

        this.lastRefillTime = lastRefillTime;
    }
}


class RateLimiter {

    private int bucketCapacity;

    // Tokens added per second
    private double refillRate;

    private HashMap<User, RateLimitData> users;


    RateLimiter(
            int bucketCapacity,
            double refillRate) {

        this.bucketCapacity = bucketCapacity;

        this.refillRate = refillRate;

        this.users = new HashMap<>();
    }


    boolean allowRequest(Request request) {

        User user = request.getUser();

        long currentTime = request.getTimestamp();


        /*
         * If this is a new user,
         * create a full bucket.
         */

        users.putIfAbsent(
                user,
                new RateLimitData(
                        bucketCapacity,
                        currentTime
                )
        );


        RateLimitData data =
                users.get(user);


        /*
         * Calculate time passed
         * since the last refill.
         */

        long elapsedTime =
                currentTime
                        - data.getLastRefillTime();


        /*
         * Convert milliseconds
         * into seconds.
         */

        double elapsedSeconds =
                elapsedTime / 1000.0;


        /*
         * Calculate how many
         * tokens should be added.
         */

        double tokensToAdd =
                elapsedSeconds
                        * refillRate;


        /*
         * Refill bucket.
         *
         * Never exceed capacity.
         */

        double newTokenCount =
                Math.min(
                        bucketCapacity,
                        data.getAvailableTokens()
                                + tokensToAdd
                );


        data.setAvailableTokens(
                newTokenCount
        );


        /*
         * Update refill time.
         */

        data.setLastRefillTime(
                currentTime
        );


        /*
         * Check whether at least
         * one token is available.
         */

        if (data.getAvailableTokens() < 1) {

            System.out.println(
                    "Request rejected for user: "
                            + user.getName()
            );

            return false;
        }


        /*
         * Consume one token.
         */

        data.setAvailableTokens(
                data.getAvailableTokens() - 1
        );


        System.out.println(
                "Request allowed for user: "
                        + user.getName()
        );


        return true;
    }
}


public class TokenBucketRateLimiter {

    public static void main(String[] args) {

        User john =
                new User(
                        "John",
                        "192.168.1.1"
                );


        /*
         * Bucket capacity = 5
         *
         * Refill rate = 1 token/second
         */

        RateLimiter rateLimiter =
                new RateLimiter(
                        5,
                        1
                );


        /*
         * Send 7 requests.
         */

        for (int i = 1; i <= 7; i++) {

            Request request =
                    new Request(john);


            boolean allowed =
                    rateLimiter
                            .allowRequest(request);


            System.out.println(
                    "Request "
                            + i
                            + " : "
                            + allowed
            );
        }
    }
}