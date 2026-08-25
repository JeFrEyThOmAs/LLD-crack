# Token Bucket Rate Limiter - Low Level Design

## 1. Problem Statement

Design a Rate Limiter using the **Token Bucket Algorithm**.

The system should:

- Limit the number of requests made by a user.
- Allow a burst of requests up to a configured bucket capacity.
- Gradually refill tokens over time.
- Reject requests when no token is available.
- Support multiple users using the same `RateLimiter`.
- Maintain independent rate-limiting data for each user.
- Never allow the number of tokens to exceed the bucket capacity.

---

# 2. Understanding the Token Bucket Algorithm

The Token Bucket algorithm works using two important parameters:

```text
1. Bucket Capacity
2. Refill Rate
```

For example:

```text
Bucket Capacity = 5 tokens

Refill Rate = 1 token per second
```

Initially, the bucket is full:

```text
[ 🪙 ][ 🪙 ][ 🪙 ][ 🪙 ][ 🪙 ]

Available Tokens = 5
```

Each request consumes one token.

---

# 3. Core Behaviour

Whenever a request arrives:

```text
Request arrives
       |
       v
Find user's bucket
       |
       v
Calculate elapsed time
       |
       v
Calculate tokens to refill
       |
       v
Add tokens to bucket
       |
       v
Do not exceed bucket capacity
       |
       v
Are at least 1 token available?
       |
      / \
    Yes   No
     |     |
     v     v
Consume   Reject
1 token
     |
     v
Allow Request
```

---

# 4. Bucket Capacity

The bucket has a fixed maximum size.

Example:

```text
Bucket Capacity = 5
```

The bucket can contain:

```text
0 tokens
1 token
2 tokens
3 tokens
4 tokens
5 tokens
```

But never:

```text
6 tokens
7 tokens
10 tokens
```

Suppose:

```text
Current Tokens = 3

Tokens Added = 4

Bucket Capacity = 5
```

Mathematically:

```text
3 + 4 = 7
```

But we use:

```java
Math.min(bucketCapacity, currentTokens + tokensToAdd);
```

Therefore:

```text
Math.min(5, 7)

= 5
```

The bucket remains full at 5 tokens.

Extra tokens are discarded.

---

# 5. Refill Rate

The refill rate determines how quickly tokens are added.

Example:

```text
Refill Rate = 1 token/second
```

This means:

```text
After 1 second -> 1 token added
After 2 seconds -> 2 tokens added
After 3 seconds -> 3 tokens added
```

However, the bucket can never exceed its capacity.

Example:

```text
Capacity = 5

Current Tokens = 4

Wait 3 seconds

Refill Rate = 1 token/sec
```

Theoretical tokens:

```text
4 + 3 = 7
```

Actual tokens:

```text
min(5, 7) = 5
```

---

# 6. Why We Don't Need Async Programming

A natural question is:

> How do we refill the bucket every second?

We do not need:

```text
- Background threads
- Async operations
- Timers
- Scheduled tasks
```

Instead, we use **lazy refill**.

Tokens are calculated only when a request arrives.

Example:

```text
Last request happened at:

0 seconds
```

No requests happen for:

```text
10 seconds
```

Then a new request arrives.

We calculate:

```text
Elapsed Time = 10 seconds

Refill Rate = 1 token/sec

Tokens To Add =

10 * 1

= 10 tokens
```

If capacity is 5:

```text
min(5, 10)

= 5
```

So the bucket becomes full when we process the next request.

Nothing needed to run during those 10 seconds.

---

# 7. Fractional Tokens

We use:

```java
double availableTokens;
```

instead of:

```java
int availableTokens;
```

because token refills can happen fractionally.

Suppose:

```text
Refill Rate = 1 token/second

Current Tokens = 0
```

After:

```text
0.5 seconds
```

we get:

```text
tokensToAdd =

0.5 * 1

= 0.5
```

Now:

```text
Available Tokens = 0.5
```

A request requires at least one full token:

```java
if (availableTokens < 1) {
    return false;
}
```

Therefore:

```text
0.5 tokens

Request -> REJECTED
```

After another 0.5 seconds:

```text
0.5 + 0.5 = 1 token
```

Now:

```text
1 token

Request -> ALLOWED
```

After consuming:

```text
1 -> 0
```

---

# 8. Architecture

The system contains four main classes:

```text
User
Request
RateLimitData
RateLimiter
```

Architecture:

```text
                    +-------------+
                    |    User     |
                    +-------------+
                    | name        |
                    | ip          |
                    +------+------+
                           |
                           v
                    +-------------+
                    |   Request   |
                    +-------------+
                    | user        |
                    | timestamp   |
                    +------+------+
                           |
                           v
              +------------------------+
              |     RateLimiter        |
              +------------------------+
              | bucketCapacity         |
              | refillRate             |
              | HashMap<User, Data>    |
              +-----------+------------+
                          |
                          v
              +------------------------+
              |    RateLimitData       |
              +------------------------+
              | availableTokens        |
              | lastRefillTime         |
              +------------------------+
```

---

# 9. User Class

The `User` represents the client making requests.

```java
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
```

Example:

```java
User john = new User(
        "John",
        "192.168.1.1"
);
```

---

# 10. Request Class

Each request contains:

- The user making the request.
- The timestamp when the request was created.

```java
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
```

---

# 11. RateLimitData Class

Every user requires their own Token Bucket state.

For example:

```text
User A -> 5 tokens

User B -> 2 tokens

User C -> 0 tokens
```

Therefore we create a separate object:

```java
class RateLimitData {

    private double availableTokens;

    private long lastRefillTime;


    RateLimitData(
            int bucketCapacity,
            long currentTime) {

        // Initially, the bucket is full
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
```

---

# 12. RateLimiter Class

The `RateLimiter` stores:

```text
Bucket Capacity

Refill Rate

User -> RateLimitData mapping
```

The mapping is:

```java
HashMap<User, RateLimitData>
```

Conceptually:

```text
User A
   |
   v
RateLimitData
   |
   +-- availableTokens = 3
   |
   +-- lastRefillTime


User B
   |
   v
RateLimitData
   |
   +-- availableTokens = 5
   |
   +-- lastRefillTime
```

---

# 13. Complete RateLimiter Logic

```java
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

        this.timestamp =
                System.currentTimeMillis();
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

        // A new user's bucket starts full
        this.availableTokens =
                bucketCapacity;

        this.lastRefillTime =
                currentTime;
    }


    double getAvailableTokens() {
        return availableTokens;
    }


    void setAvailableTokens(
            double availableTokens) {

        this.availableTokens =
                availableTokens;
    }


    long getLastRefillTime() {
        return lastRefillTime;
    }


    void setLastRefillTime(
            long lastRefillTime) {

        this.lastRefillTime =
                lastRefillTime;
    }
}


class RateLimiter {

    private int bucketCapacity;

    // Number of tokens added per second
    private double refillRate;

    /*
     * Each user has independent
     * rate-limiting data.
     */

    private HashMap<User, RateLimitData>
            users;


    RateLimiter(
            int bucketCapacity,
            double refillRate) {

        this.bucketCapacity =
                bucketCapacity;

        this.refillRate =
                refillRate;

        this.users =
                new HashMap<>();
    }


    boolean allowRequest(Request request) {

        User user =
                request.getUser();

        long currentTime =
                request.getTimestamp();


        /*
         * If this user does not yet
         * have a bucket, create one.
         *
         * The bucket starts full.
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
         * Calculate how much time
         * has passed since the last
         * refill.
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
         * Calculate how many tokens
         * should have been added.
         */

        double tokensToAdd =
                elapsedSeconds
                        * refillRate;


        /*
         * Add tokens but never allow
         * the bucket to exceed capacity.
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
         * Update the last refill time.
         */

        data.setLastRefillTime(
                currentTime
        );


        /*
         * A request requires
         * at least one token.
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
         * Bucket Capacity = 5
         *
         * Refill Rate = 1 token/sec
         */

        RateLimiter rateLimiter =
                new RateLimiter(
                        5,
                        1
                );


        /*
         * Send 7 immediate requests.
         */

        for (int i = 1;
             i <= 7;
             i++) {

            Request request =
                    new Request(john);


            boolean allowed =
                    rateLimiter
                            .allowRequest(
                                    request
                            );


            System.out.println(
                    "Request "
                            + i
                            + " : "
                            + allowed
            );
        }
    }
}
```

---

# 14. Dry Run

Configuration:

```text
Bucket Capacity = 5

Refill Rate = 1 token/second
```

Initially:

```text
Tokens = 5
```

## Request 1

```text
Tokens Before = 5

Consume 1

Tokens After = 4

Request -> ALLOWED
```

---

## Request 2

```text
Tokens Before = 4

Consume 1

Tokens After = 3

Request -> ALLOWED
```

---

## Request 3

```text
Tokens Before = 3

Consume 1

Tokens After = 2

Request -> ALLOWED
```

---

## Request 4

```text
Tokens Before = 2

Consume 1

Tokens After = 1

Request -> ALLOWED
```

---

## Request 5

```text
Tokens Before = 1

Consume 1

Tokens After = 0

Request -> ALLOWED
```

The bucket is now empty:

```text
[ _ ][ _ ][ _ ][ _ ][ _ ]

Tokens = 0
```

---

# 15. Requests 6 to 10

Suppose the next five requests arrive immediately.

Almost no time has passed.

Therefore:

```text
tokensToAdd approximately equals 0
```

The bucket remains:

```text
Tokens = 0
```

Check:

```text
availableTokens >= 1 ?
```

Result:

```text
0 >= 1

False
```

Therefore:

```text
Request 6  -> REJECTED
Request 7  -> REJECTED
Request 8  -> REJECTED
Request 9  -> REJECTED
Request 10 -> REJECTED
```

---

# 16. What Happens After One Second?

The bucket is empty:

```text
Tokens = 0
```

Refill Rate:

```text
1 token/second
```

After one second, a new request arrives.

We calculate:

```text
Elapsed Time = 1 second

Tokens To Add =

1 * 1

= 1 token
```

Now:

```text
Tokens = 1
```

Check:

```text
Tokens >= 1
```

Yes.

The request is allowed.

Then:

```text
Tokens:

1 -> 0
```

The bucket does not need to become completely full before processing another request.

**One token is enough for one request.**

---

# 17. Example Timeline

```text
Time      Request       Tokens Before       Result        Tokens After

0 sec     Request 1          5              Allow              4
0 sec     Request 2          4              Allow              3
0 sec     Request 3          3              Allow              2
0 sec     Request 4          2              Allow              1
0 sec     Request 5          1              Allow              0

0 sec     Request 6          0              Reject             0
0 sec     Request 7          0              Reject             0
0 sec     Request 8          0              Reject             0
0 sec     Request 9          0              Reject             0
0 sec     Request 10         0              Reject             0

1 sec     Request 11         0 + 1          Allow              0

2 sec     Request 12         0 + 1          Allow              0

3 sec     Request 13         0 + 1          Allow              0
```

---

# 18. Burst Traffic

The bucket capacity controls how much burst traffic is allowed.

Example:

```text
Bucket Capacity = 5
```

The user can immediately make:

```text
5 requests
```

Even if they all arrive at almost the same time.

After that:

```text
No tokens

Further requests -> Rejected
```

This is why:

```text
Bucket Capacity
```

controls the maximum burst.

---

# 19. Long-Term Rate

The refill rate controls the sustained request rate.

Example:

```text
Refill Rate = 1 token/sec
```

After the initial burst is consumed, the user can make approximately:

```text
1 request per second
```

Therefore:

```text
Bucket Capacity
        |
        v
Controls burst traffic


Refill Rate
        |
        v
Controls long-term traffic
```

---

# 20. Multiple Users

The `RateLimiter` can handle multiple users:

```java
HashMap<User, RateLimitData>
```

For example:

```text
User A -> 2 tokens

User B -> 5 tokens

User C -> 0 tokens
```

Each user has an independent bucket.

A request from User A does not consume tokens from User B.

Conceptually:

```text
                  RateLimiter
                       |
        +--------------+--------------+
        |              |              |
        v              v              v

      User A         User B         User C

        |              |              |
        v              v              v

    2 Tokens       5 Tokens       0 Tokens
```

---

# 21. Fixed Window vs Token Bucket

| Fixed Window | Token Bucket |
|---|---|
| Counts requests | Stores tokens |
| Resets after a window | Refills gradually |
| `requestCount` | `availableTokens` |
| `windowStartTime` | `lastRefillTime` |
| `maxRequests` | `bucketCapacity` |
| `maxWindow` | `refillRate` |
| Can have boundary issues | Handles bursts smoothly |

---

# 22. Key Formula

The core refill logic is:

```java
long elapsedTime =
        currentTime - lastRefillTime;
```

Convert milliseconds to seconds:

```java
double elapsedSeconds =
        elapsedTime / 1000.0;
```

Calculate tokens to add:

```java
double tokensToAdd =
        elapsedSeconds * refillRate;
```

Add tokens without exceeding capacity:

```java
double newTokenCount =
        Math.min(
                bucketCapacity,
                availableTokens + tokensToAdd
        );
```

Then check:

```java
if (availableTokens < 1) {
    return false;
}
```

Otherwise:

```java
availableTokens--;

return true;
```

---

# 23. Complexity

For every request:

```text
HashMap lookup -> O(1)

Token calculation -> O(1)

Update bucket -> O(1)
```

Therefore:

```text
Time Complexity = O(1)
```

Space:

```text
One RateLimitData object per user
```

Therefore:

```text
Space Complexity = O(number of users)
```

---

# 24. Interview Explanation

A concise way to explain the design:

> "I am using the Token Bucket algorithm. Each user has an independent bucket represented by `RateLimitData`, which stores the available tokens and the last refill timestamp. The `RateLimiter` maintains a `HashMap<User, RateLimitData>`. When a request arrives, I calculate the elapsed time since the last refill and lazily calculate how many tokens should be added. I cap the bucket at its maximum capacity. If at least one token is available, I consume it and allow the request; otherwise, I reject it. This allows bursts up to the bucket capacity while controlling the long-term request rate using the refill rate."

---

# 25. Final Architecture Summary

```text
                         +----------------+
                         |      User      |
                         +----------------+
                         | name           |
                         | ip             |
                         +--------+-------+
                                  |
                                  v
                         +----------------+
                         |    Request     |
                         +----------------+
                         | user           |
                         | timestamp      |
                         +--------+-------+
                                  |
                                  v
                    +---------------------------+
                    |       RateLimiter         |
                    +---------------------------+
                    | bucketCapacity            |
                    | refillRate                |
                    | HashMap<User, Data>       |
                    +-------------+-------------+
                                  |
                                  v
                    +---------------------------+
                    |       RateLimitData       |
                    +---------------------------+
                    | availableTokens           |
                    | lastRefillTime            |
                    +---------------------------+
```

---

# Final Summary

The Token Bucket algorithm is based on:

```text
Bucket Capacity + Refill Rate
```

The bucket capacity controls:

```text
Maximum burst traffic
```

The refill rate controls:

```text
Long-term request rate
```

The bucket:

```text
Starts full
        |
        v
Each request consumes one token
        |
        v
Tokens refill based on elapsed time
        |
        v
Tokens never exceed bucket capacity
        |
        v
No token -> Request rejected
```

The important design decision is **lazy refill**:

```text
No background thread

No timer

No async refill
```

Instead:

```text
Request arrives
        |
        v
Calculate elapsed time
        |
        v
Calculate tokens that should have been refilled
        |
        v
Update bucket
        |
        v
Allow or reject request
```

This implementation supports multiple users and gives each user an independent Token Bucket.