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

    private int requestCount;
    private long windowStartTime;

    RateLimitData() {
        this.requestCount = 0;
        this.windowStartTime = System.currentTimeMillis();
    }

    int getRequestCount() {
        return requestCount;
    }

    void incrementRequestCount() {
        requestCount++;
    }

    void reset(long currentTime) {
        requestCount = 0;
        windowStartTime = currentTime;
    }

    long getWindowStartTime() {
        return windowStartTime;
    }
}

class RateLimiter {

    private int maxRequests;
    private long maxWindow;

    private HashMap<User, RateLimitData> hm =
            new HashMap<>();


    RateLimiter(int maxRequests, long maxWindow) {
        this.maxRequests = maxRequests;
        this.maxWindow = maxWindow;
    }


    boolean allowRequest(Request request) {

        User user = request.getUser();
        long currentTime = request.getTimestamp();


        // Create data for a new user
        if (!hm.containsKey(user)) {
            hm.put(user, new RateLimitData());
        }


        RateLimitData data = hm.get(user);


        // Check THIS USER's window
        if (currentTime - data.getWindowStartTime()
                >= maxWindow) {

            data.reset(currentTime);
        }


        // Check THIS USER's limit
        if (data.getRequestCount() >= maxRequests) {

            System.out.println(
                    "Request rejected for user: "
                            + user.getName()
            );

            return false;
        }


        data.incrementRequestCount();

        System.out.println(
                "Request allowed for user: "
                        + user.getName()
        );

        return true;
    }
}

public class RateLimiterFixedWindowStrategy {

    public static void main(String[] args) {

        User user = new User(
                "John",
                "192.168.1.1"
        );


        // Allow maximum 5 requests per 5 seconds
        RateLimiter rateLimiter =
                new RateLimiter(
                        5,
                        5000
                );


        for (int i = 1; i <= 7; i++) {

            Request request =
                    new Request(user);

            boolean allowed =
                    rateLimiter.allowRequest(request);

            System.out.println(
                    "Request "
                            + i
                            + " : "
                            + allowed
            );
        }
    }
}