# Sliding Window Rate Limiter - Low Level Design

## 1. Problem Statement

Design a Rate Limiter using the **Sliding Window algorithm**.

The system should:

- Limit the number of requests made by a user.
- Allow only a configured number of requests within a configured time window.
- Support multiple users.
- Maintain separate request history for every user.
- Reject requests when the rate limit is exceeded.
- Automatically remove old requests that are outside the current time window.

Example:

```text
Maximum Requests = 5

Time Window = 10 seconds
```

This means a user can make at most:

```text
5 requests in any 10-second period
```

---

# 2. What is Sliding Window?

The Sliding Window algorithm keeps track of the timestamps of recent requests.

When a new request arrives:

```text
New Request
      |
      v
Remove expired requests
      |
      v
Check number of remaining requests
      |
      v
Is the limit reached?
      |
     / \
   Yes   No
    |     |
    v     v
 Reject  Add request timestamp
                |
                v
              Allow
```

The important difference from Fixed Window is:

```text
Fixed Window
```

uses fixed time boundaries.

Whereas:

```text
Sliding Window
```

continuously moves with time.

---

# 3. Example

Suppose:

```text
Maximum Requests = 5

Window Size = 10 seconds
```

A user sends requests at:

```text
1 sec
2 sec
3 sec
4 sec
5 sec
```

The stored timestamps are:

```text
[1, 2, 3, 4, 5]
```

Now another request arrives at:

```text
6 sec
```

The previous 10-second window is:

```text
-4 sec ---------------- 6 sec
```

All five previous requests are still inside the window.

Therefore:

```text
Current Requests = 5

Maximum Requests = 5
```

The new request is:

```text
REJECTED
```

---

# 4. Window Sliding Forward

Now suppose the current time becomes:

```text
12 sec
```

The request at:

```text
1 sec
```

is now outside the 10-second window.

Calculation:

```text
12 - 1 = 11 seconds
```

Since:

```text
11 >= 10
```

we remove the request at `1 sec`.

The queue becomes:

```text
[2, 3, 4, 5]
```

Now:

```text
Number of Requests = 4

Maximum Requests = 5
```

So the new request at `12 sec` is allowed.

The queue becomes:

```text
[2, 3, 4, 5, 12]
```

---

# 5. Why We Use a Queue

Initially, we might think about using:

```java
HashSet<Long>
```

But a `HashSet` is not ideal.

For example, two requests could theoretically have the same timestamp:

```text
1000
1000
```

A `HashSet` would only store:

```text
[1000]
```

One request would effectively disappear.

Also, Sliding Window needs to remove the **oldest request first**.

A Queue is perfect for this:

```text
Oldest                        Newest

  [100] → [200] → [300] → [400]
    ^
    |
  peek()
```

We can remove expired requests using:

```java
requestTimes.poll();
```

And add new requests using:

```java
requestTimes.offer(currentTime);
```

Therefore:

```text
Queue<Long> requestTimes
```

is a good data structure for this implementation.

---

# 6. Architecture

The implementation contains four main classes:

```text
User

Request

RateLimitData

RateLimiter
```

Their relationship is:

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
              +--------------------------+
              |       RateLimiter        |
              +--------------------------+
              | maxRequests              |
              | maxWindow                |
              | users                    |
              +------------+-------------+
                           |
                           v
              HashMap<User, RateLimitData>
                           |
              +------------+------------+
              |                         |
              v                         v

        User A Data                 User B Data
              |                         |
              v                         v

      Queue<Long>               Queue<Long>
```

---

# 7. User Class

The `User` represents the person or client making requests.

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
```

Example:

```java
User john =
        new User(
                "John",
                "192.168.1.1"
        );
```

---

# 8. Request Class

Every incoming request is represented using a `Request` object.

A request contains:

```text
User

Timestamp
```

Implementation:

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
```

The timestamp is stored in milliseconds.

```java
System.currentTimeMillis();
```

---

# 9. RateLimitData Class

Every user needs their own request history.

For example:

```text
John

Request Times:

[1000, 2000, 3000]
```

Another user:

```text
Alice

Request Times:

[1500, 4000]
```

Therefore, we create a separate `RateLimitData` object for each user.

```java
class RateLimitData {

    private Queue<Long> requestTimes;

    RateLimitData() {
        this.requestTimes = new LinkedList<>();
    }

    Queue<Long> getRequestTimes() {
        return requestTimes;
    }

    void setRequestTimes(
            Queue<Long> requestTimes
    ) {
        this.requestTimes = requestTimes;
    }
}
```

The queue always contains only the requests currently inside the sliding window.

---

# 10. RateLimiter Class

The `RateLimiter` contains the main rate-limiting logic.

It stores:

```text
Maximum Requests

Maximum Window

User -> RateLimitData mapping
```

The mapping is:

```java
HashMap<User, RateLimitData>
```

Conceptually:

```text
RateLimiter
     |
     +---------------------------+
     |                           |
     v                           v

   John                        Alice
     |                           |
     v                           v

[100, 200, 300]           [400, 500]
```

Every user has their own independent request history.

---

# 11. Core Sliding Window Logic

The core algorithm has three steps.

## Step 1: Remove expired requests

```java
while (
        !requestTimes.isEmpty()
                &&
        currentTime - requestTimes.peek()
                >= maxWindow
) {
    requestTimes.poll();
}
```

Suppose:

```text
Current Time = 15000 ms

Oldest Request = 4000 ms

Window = 10000 ms
```

Calculation:

```text
15000 - 4000

= 11000 ms
```

Since:

```text
11000 >= 10000
```

the request is outside the window.

Therefore:

```java
requestTimes.poll();
```

removes it.

---

# 12. Step 2: Check the Request Limit

After removing expired requests:

```java
if (requestTimes.size() >= maxRequests) {
    return false;
}
```

Suppose:

```text
Maximum Requests = 5

Current Requests = 5
```

Then:

```text
5 >= 5
```

The new request is rejected.

---

# 13. Step 3: Add the New Request

If the limit has not been reached:

```java
requestTimes.offer(currentTime);
```

Then:

```java
return true;
```

The request is allowed.

---

# 14. Complete Implementation

```java
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

    void setRequestTimes(
            Queue<Long> requestTimes
    ) {
        this.requestTimes = requestTimes;
    }
}


class RateLimiter {

    private int maxRequests;
    private long maxWindow;

    private HashMap<User, RateLimitData> users;

    RateLimiter(
            int maxRequests,
            long maxWindow
    ) {

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


        RateLimitData data =
                users.get(user);


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


        if (
                requestTimes.size()
                        >= maxRequests
        ) {

            System.out.println(
                    "Request rejected for user: "
                            + user.getName()
            );

            return false;
        }


        requestTimes.offer(
                currentTime
        );


        System.out.println(
                "Request allowed for user: "
                        + user.getName()
        );


        return true;
    }


    int getMaxRequests() {
        return maxRequests;
    }

    void setMaxRequests(
            int maxRequests
    ) {
        this.maxRequests = maxRequests;
    }


    long getMaxWindow() {
        return maxWindow;
    }

    void setMaxWindow(
            long maxWindow
    ) {
        this.maxWindow = maxWindow;
    }


    HashMap<User, RateLimitData>
    getUsers() {
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


        for (
                int i = 1;
                i <= 7;
                i++
        ) {

            Request request =
                    new Request(john);


            boolean allowed =
                    rateLimiter.allowRequest(
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

# 15. Dry Run

Configuration:

```text
Maximum Requests = 5

Window = 10 seconds
```

Initially:

```text
Request Queue:

[]
```

---

## Request 1

Current Queue:

```text
[]
```

No expired requests.

Queue size:

```text
0 < 5
```

Allow request.

Add timestamp:

```text
[t1]
```

---

## Request 2

Queue:

```text
[t1]
```

Size:

```text
1 < 5
```

Allow request.

Queue:

```text
[t1, t2]
```

---

## Request 3

```text
[t1, t2]
```

Size:

```text
2 < 5
```

Allow.

Queue:

```text
[t1, t2, t3]
```

---

## Request 4

Queue:

```text
[t1, t2, t3]
```

Size:

```text
3 < 5
```

Allow.

Queue:

```text
[t1, t2, t3, t4]
```

---

## Request 5

Queue:

```text
[t1, t2, t3, t4]
```

Size:

```text
4 < 5
```

Allow.

Queue:

```text
[t1, t2, t3, t4, t5]
```

---

## Request 6

Suppose all requests happen within 10 seconds.

Queue:

```text
[t1, t2, t3, t4, t5]
```

Size:

```text
5
```

Check:

```text
5 >= 5
```

Therefore:

```text
Request 6 -> REJECTED
```

The rejected request is not added to the queue.

---

# 16. When the Window Moves

Suppose:

```text
Window = 10 seconds
```

The current queue is:

```text
[1, 2, 3, 4, 5]
```

Now a request arrives at:

```text
12 seconds
```

Check the oldest request:

```text
12 - 1 = 11 seconds
```

The request at time `1` is expired.

Remove it:

```text
[2, 3, 4, 5]
```

Now:

```text
Queue Size = 4
```

Since:

```text
4 < 5
```

Allow the new request.

Add:

```text
12
```

Final queue:

```text
[2, 3, 4, 5, 12]
```

---

# 17. Fixed Window vs Sliding Window

Suppose:

```text
Limit = 5 requests

Window = 10 seconds
```

## Fixed Window

A user can send:

```text
Time 9.9 sec  -> 5 requests

Time 10.1 sec -> 5 requests
```

That means:

```text
10 requests
```

within a very short period.

This happens because the counter resets at the fixed boundary.

---

## Sliding Window

At:

```text
10.1 sec
```

the requests from `9.9 sec` are still inside the previous 10 seconds.

Therefore, the new requests are rejected.

The Sliding Window algorithm provides smoother rate limiting.

---

# 18. Sliding Window vs Token Bucket

| Sliding Window | Token Bucket |
|---|---|
| Stores request timestamps | Stores available tokens |
| Strict request count in a time range | Allows controlled bursts |
| More memory usage | Less memory usage |
| Removes expired timestamps | Refills tokens |
| Better for strict limits | Better for burst traffic |

---

# 19. Multiple Users

The implementation uses:

```java
HashMap<User, RateLimitData>
```

This allows multiple users to share the same `RateLimiter`.

Example:

```text
RateLimiter
      |
      +--------------------------+
      |                          |
      v                          v

    John                       Alice
      |                          |
      v                          v

 [100, 200]                [150, 300, 400]
```

John's requests do not affect Alice's limit.

Every user has independent `RateLimitData`.

---

# 20. Core Algorithm

The complete Sliding Window logic can be summarized as:

```text
New Request Arrives
        |
        v
Get User
        |
        v
Get User's RateLimitData
        |
        v
Get Queue of Request Timestamps
        |
        v
Remove Expired Requests
        |
        v
Is Queue Size >= Maximum Requests?
        |
       / \
     Yes   No
      |     |
      v     v
   Reject   Add Timestamp
                |
                v
              Allow
```

---

# 21. Time Complexity

For every request:

```text
Adding request -> O(1)

Checking oldest request -> O(1)
```

Expired requests are removed only once.

Therefore, the amortized complexity per request is:

```text
O(1)
```

---

# 22. Space Complexity

For every user, we store the timestamps of requests currently inside the window.

If:

```text
Maximum Requests = N
```

Then approximately:

```text
Space per user = O(N)
```

For multiple users:

```text
O(Number of Users × Maximum Requests)
```

---

# 23. Advantages

The Sliding Window algorithm:

```text
✓ Prevents Fixed Window boundary problems

✓ Provides smoother rate limiting

✓ Supports multiple users

✓ Maintains independent request history

✓ Gives strict control over requests
  in a moving time range
```

---

# 24. Limitations

The algorithm needs to store request timestamps.

For example, if:

```text
1,000,000 users

100 requests per user
```

there could be many timestamps stored in memory.

For very high-scale systems, we may consider:

```text
Redis

Distributed rate limiting

Sliding Window Counter

Approximate algorithms
```

But for an in-memory LLD implementation, the current design is good.

---

# 25. Interview Explanation

A concise explanation would be:

> "I am using a Sliding Window Rate Limiter. Each user has a `RateLimitData` object containing a queue of timestamps for requests currently inside the configured time window. When a new request arrives, I first remove timestamps that are older than the window. Then I check the queue size. If it has already reached the maximum request limit, I reject the request. Otherwise, I add the current request timestamp and allow it. A `HashMap` maps each user to their independent rate-limiting data."

---

# 26. Final Architecture

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
                +--------------------------+
                |       RateLimiter        |
                +--------------------------+
                | maxRequests              |
                | maxWindow                |
                | users                    |
                +------------+-------------+
                             |
                             v
              HashMap<User, RateLimitData>
                             |
                 +-----------+-----------+
                 |                       |
                 v                       v

        +----------------+      +----------------+
        | RateLimitData  |      | RateLimitData  |
        +----------------+      +----------------+
        | requestTimes   |      | requestTimes   |
        +-------+--------+      +-------+--------+
                |                       |
                v                       v

          Queue<Long>              Queue<Long>
```

---

# Final Summary

The Sliding Window Rate Limiter works by storing timestamps of recent requests.

For every new request:

```text
1. Find the user's RateLimitData

2. Get the queue of request timestamps

3. Remove requests outside the current window

4. Check the number of remaining requests

5. If limit is reached:
      Reject

6. Otherwise:
      Add current timestamp
      Allow
```

The main data structure is:

```java
Queue<Long>
```

because:

```text
Oldest requests are removed first.
```

The user data is stored using:

```java
HashMap<User, RateLimitData>
```

which gives every user an independent Sliding Window.

The core logic is:

```java
while (
        !requestTimes.isEmpty()
                &&
        currentTime - requestTimes.peek()
                >= maxWindow
) {
    requestTimes.poll();
}

if (requestTimes.size() >= maxRequests) {
    return false;
}

requestTimes.offer(currentTime);

return true;
```

This completes the three common Rate Limiter implementations:

```text
1. Fixed Window Counter

2. Token Bucket

3. Sliding Window
```

Each algorithm uses a similar LLD structure:

```text
User
  |
Request
  |
RateLimiter
  |
RateLimitData
```

The major difference is the data stored inside `RateLimitData`:

```text
Fixed Window

requestCounter
windowStartTime


Token Bucket

availableTokens
lastRefillTime


Sliding Window

Queue<Long> requestTimes
```

So the overall object design remains similar, while the core rate-limiting algorithm changes.