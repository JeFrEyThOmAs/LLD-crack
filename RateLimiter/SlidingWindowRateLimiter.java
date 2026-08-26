import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

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

    void setName(String name) {
        this.name = name;
    }

    String getIp() {
        return ip;
    }

    void setIp(String ip) {
        this.ip = ip;
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

    void setUser(User user) {
        this.user = user;
    }

    long getTimestamp() {
        return timestamp;
    }

    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}


class RateLimitData {

    private Queue<Long> requestTimes;

    RateLimitData() {
        this.requestTimes = new LinkedList<>();
    }

    Queue<Long> getRequestTimes() {
        return requestTimes;
    }

    void setRequestTimes(Queue<Long> requestTimes) {
        this.requestTimes = requestTimes;
    }
}


class RateLimiter {

    private int maxRequests;
    private long maxWindow;

    private HashMap<User, RateLimitData> users;

    RateLimiter(int maxRequests, long maxWindow) {

        this.maxRequests = maxRequests;
        this.maxWindow = maxWindow;

        this.users = new HashMap<>();
    }

    boolean allowRequest(Request request) {

        User user = request.getUser();

        long currentTime = request.getTimestamp();

        users.putIfAbsent(
                user,
                new RateLimitData()
        );

        RateLimitData data = users.get(user);

        Queue<Long> requestTimes =
                data.getRequestTimes();

        while (
                !requestTimes.isEmpty()
                        &&
                        currentTime - requestTimes.peek()
                                >= maxWindow
        ) {
            requestTimes.poll();
        }

        if (requestTimes.size() >= maxRequests) {

            System.out.println(
                    "Request rejected for user: "
                            + user.getName()
            );

            return false;
        }

        requestTimes.offer(currentTime);

        System.out.println(
                "Request allowed for user: "
                        + user.getName()
        );

        return true;
    }

    int getMaxRequests() {
        return maxRequests;
    }

    void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    long getMaxWindow() {
        return maxWindow;
    }

    void setMaxWindow(long maxWindow) {
        this.maxWindow = maxWindow;
    }

    HashMap<User, RateLimitData> getUsers() {
        return users;
    }

    void setUsers(
            HashMap<User, RateLimitData> users
    ) {
        this.users = users;
    }
}


public class SlidingWindowRateLimiter {

    public static void main(String[] args) {

        User john =
                new User(
                        "John",
                        "192.168.1.1"
                );

        RateLimiter rateLimiter =
                new RateLimiter(
                        5,
                        10000
                );

        for (int i = 1; i <= 7; i++) {

            Request request =
                    new Request(john);

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