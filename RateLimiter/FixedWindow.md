# Rate Limiter System - Low Level Design

## 1. Problem Statement

Design a Rate Limiter that controls how many requests a user can make within a specified time window.

For example:

- Maximum requests allowed: `5`
- Time window: `5 seconds`

If a user makes 6 requests within 5 seconds:

```text
Request 1 → Allowed
Request 2 → Allowed
Request 3 → Allowed
Request 4 → Allowed
Request 5 → Allowed
Request 6 → Rejected
```

After the time window expires, the user should again be able to make requests.

The system should support multiple users using the same `RateLimiter`.

---

# 2. Functional Requirements

The system should:

1. Accept requests from multiple users.
2. Identify which user made each request.
3. Track the number of requests made by each user.
4. Allow only a configured maximum number of requests.
5. Reject requests after the limit is reached.
6. Reset the request count when the user's time window expires.
7. Maintain independent rate-limiting state for different users.
8. Use the Fixed Window Counter algorithm.

---

# 3. Algorithm Used

We are implementing the:

## Fixed Window Counter Algorithm

Example configuration:

```text
Maximum Requests = 5
Time Window = 5 seconds
```

For a user:

```text
Time: 0 sec

Request 1 → Allowed
Request 2 → Allowed
Request 3 → Allowed
Request 4 → Allowed
Request 5 → Allowed
Request 6 → Rejected
```

Once 5 seconds have passed:

```text
New Window Starts

Request Count = 0
```

The user can again make requests.

---

# 4. High-Level Architecture

```text
                  +--------+
                  |  User  |
                  +--------+
                       |
                       v
                  +---------+
                  | Request |
                  +---------+
                       |
                       v
              +----------------+
              |  RateLimiter   |
              +----------------+
                       |
                       v
        HashMap<User, RateLimitData>
                       |
             +---------+---------+
             |                   |
             v                   v
          John Data          Alice Data

        requestCount        requestCount
        windowStartTime     windowStartTime
```

The `RateLimiter` stores independent state for every user.

---

# 5. Class Design

We have four main classes:

```text
User
Request
RateLimitData
RateLimiter
```

---

# 6. User Class

The `User` class represents a user making requests.

Each user contains:

- Name
- IP Address

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

---

# 7. Request Class

Every incoming request is represented using a `Request` object.

A request contains:

- The user making the request.
- The timestamp when the request was created.

```java
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
```

---

# 8. Why Do We Need RateLimitData?

Initially, we may think of using:

```java
HashMap<User, Integer>
```

Example:

```text
John  → 3 requests
Alice → 5 requests
Bob   → 2 requests
```

However, every user needs more information than just the request count.

Each user also needs:

```text
requestCount

windowStartTime
```

Therefore, instead of:

```text
User → Integer
```

we use:

```text
User → RateLimitData
```

Architecture:

```text
HashMap<User, RateLimitData>

John
 |
 v
RateLimitData
 |
 +-- requestCount
 |
 +-- windowStartTime


Alice
 |
 v
RateLimitData
 |
 +-- requestCount
 |
 +-- windowStartTime
```

This is more extensible and keeps related data together.

---

# 9. RateLimitData Class

`RateLimitData` stores the rate-limiting state of one user.

```java
class RateLimitData {

    private int requestCount;

    private long windowStartTime;


    RateLimitData(long windowStartTime) {

        this.requestCount = 0;

        this.windowStartTime =
                windowStartTime;
    }


    int getRequestCount() {

        return requestCount;
    }


    long getWindowStartTime() {

        return windowStartTime;
    }


    void incrementRequestCount() {

        requestCount++;
    }


    void reset(long currentTime) {

        requestCount = 0;

        windowStartTime =
                currentTime;
    }
}
```

---

# 10. RateLimiter Class

The `RateLimiter` contains the core logic.

It stores:

```text
maxRequests

maxWindow

HashMap<User, RateLimitData>
```

The HashMap stores independent state for every user.

```java
import java.util.HashMap;


class RateLimiter {

    private int maxRequests;

    private long maxWindow;

    private HashMap<User, RateLimitData> users;


    RateLimiter(
            int maxRequests,
            long maxWindow) {

        this.maxRequests =
                maxRequests;

        this.maxWindow =
                maxWindow;

        this.users =
                new HashMap<>();
    }


    boolean allowRequest(
            Request request) {

        User user =
                request.getUser();

        long currentTime =
                request.getTimestamp();


        /*
         * If this user is making
         * their first request,
         * create RateLimitData.
         */

        users.putIfAbsent(

                user,

                new RateLimitData(
                        currentTime
                )
        );


        RateLimitData data =
                users.get(user);


        /*
         * Check whether the user's
         * time window has expired.
         */

        if (currentTime
                - data.getWindowStartTime()
                >= maxWindow) {

            data.reset(
                    currentTime
            );
        }


        /*
         * Check whether the user
         * has reached the limit.
         */

        if (data.getRequestCount()
                >= maxRequests) {

            System.out.println(

                    "Request rejected for user: "

                            + user.getName()
            );

            return false;
        }


        /*
         * Allow the request.
         */

        data.incrementRequestCount();


        System.out.println(

                "Request allowed for user: "

                        + user.getName()
        );


        return true;
    }
}
```

---

# 11. Complete Code

```java
import java.util.HashMap;


class User {

    private String name;

    private String ip;


    User(
            String name,
            String ip) {

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

    private int requestCount;

    private long windowStartTime;


    RateLimitData(
            long windowStartTime) {

        this.requestCount = 0;

        this.windowStartTime =
                windowStartTime;
    }


    int getRequestCount() {

        return requestCount;
    }


    long getWindowStartTime() {

        return windowStartTime;
    }


    void incrementRequestCount() {

        requestCount++;
    }


    void reset(
            long currentTime) {

        requestCount = 0;

        windowStartTime =
                currentTime;
    }
}


class RateLimiter {

    private int maxRequests;

    private long maxWindow;

    private HashMap<User, RateLimitData>
            users;


    RateLimiter(
            int maxRequests,
            long maxWindow) {

        this.maxRequests =
                maxRequests;

        this.maxWindow =
                maxWindow;

        this.users =
                new HashMap<>();
    }


    boolean allowRequest(
            Request request) {

        User user =
                request.getUser();

        long currentTime =
                request.getTimestamp();


        /*
         * Create rate limiting data
         * if this user is new.
         */

        users.putIfAbsent(

                user,

                new RateLimitData(
                        currentTime
                )
        );


        RateLimitData data =
                users.get(user);


        /*
         * Check whether the user's
         * current window has expired.
         */

        if (currentTime
                - data.getWindowStartTime()
                >= maxWindow) {

            data.reset(
                    currentTime
            );
        }


        /*
         * Reject request if
         * maximum limit is reached.
         */

        if (data.getRequestCount()
                >= maxRequests) {

            System.out.println(

                    "Request rejected for user: "

                            + user.getName()
            );

            return false;
        }


        /*
         * Allow the request.
         */

        data.incrementRequestCount();


        System.out.println(

                "Request allowed for user: "

                        + user.getName()
        );


        return true;
    }
}


public class RateLimiterSystem {

    public static void main(
            String[] args) {


        User john =
                new User(

                        "John",

                        "192.168.1.1"
                );


        User alice =
                new User(

                        "Alice",

                        "192.168.1.2"
                );


        /*
         * Maximum:
         *
         * 5 requests
         *
         * Per:
         *
         * 5 seconds
         */

        RateLimiter rateLimiter =
                new RateLimiter(

                        5,

                        5000
                );


        /*
         * John's requests
         */

        for (
                int i = 1;
                i <= 6;
                i++
        ) {

            Request request =
                    new Request(
                            john
                    );


            boolean allowed =
                    rateLimiter
                            .allowRequest(
                                    request
                            );


            System.out.println(

                    "John Request "
                            + i
                            + " : "
                            + allowed
            );
        }


        System.out.println(
                "----------------"
        );


        /*
         * Alice's requests
         */

        for (
                int i = 1;
                i <= 3;
                i++
        ) {

            Request request =
                    new Request(
                            alice
                    );


            boolean allowed =
                    rateLimiter
                            .allowRequest(
                                    request
                            );


            System.out.println(

                    "Alice Request "
                            + i
                            + " : "
                            + allowed
            );
        }
    }
}
```

---

# 12. Example Execution

Suppose:

```text
Maximum Requests = 5

Window = 5 seconds
```

John makes 6 requests:

```text
John Request 1 → Allowed
John Request 2 → Allowed
John Request 3 → Allowed
John Request 4 → Allowed
John Request 5 → Allowed
John Request 6 → Rejected
```

Alice makes 3 requests:

```text
Alice Request 1 → Allowed
Alice Request 2 → Allowed
Alice Request 3 → Allowed
```

John reaching his limit does not affect Alice.

Because:

```text
John
 ↓
RateLimitData

requestCount = 5


Alice
 ↓
RateLimitData

requestCount = 3
```

Each user has independent state.

---

# 13. Request Flow

When a request arrives:

```text
                Request
                    |
                    v
             Get the User
                    |
                    v
     Does RateLimitData exist?
             /          \
           No            Yes
           |              |
           v              |
   Create RateLimitData   |
           |              |
           +------->------+
                    |
                    v
          Check Window Expiry
                    |
              Expired?
              /       \
            Yes       No
             |         |
             v         |
        Reset Count    |
             |         |
             +---->----+
                    |
                    v
       Has user reached maxRequests?
              /              \
            Yes              No
             |                |
             v                v
         Reject ❌       Increment Count
                                |
                                v
                            Allow ✅
```

---

# 14. Class Diagram

```text
+------------------------+
|         User           |
+------------------------+
| - name : String        |
| - ip : String          |
+------------------------+
| + getName()            |
| + getIp()              |
+------------------------+


             |
             |
             v


+------------------------+
|        Request         |
+------------------------+
| - user : User          |
| - timestamp : long     |
+------------------------+
| + getUser()            |
| + getTimestamp()       |
+------------------------+


             |
             |
             v


+-------------------------------------+
|            RateLimiter              |
+-------------------------------------+
| - maxRequests : int                 |
| - maxWindow : long                  |
| - users : HashMap<User,             |
|           RateLimitData>            |
+-------------------------------------+
| + allowRequest(Request) : boolean   |
+-------------------------------------+
                    |
                    |
                    v


+-----------------------------+
|       RateLimitData         |
+-----------------------------+
| - requestCount : int        |
| - windowStartTime : long    |
+-----------------------------+
| + incrementRequestCount()   |
| + reset()                   |
+-----------------------------+
```

---

# 15. Object Relationship

```text
RateLimiter
    |
    |
    v

HashMap<User, RateLimitData>


User: John
    |
    v

RateLimitData
    |
    +-- requestCount = 5
    |
    +-- windowStartTime = 1000


User: Alice
    |
    v

RateLimitData
    |
    +-- requestCount = 3
    |
    +-- windowStartTime = 2000
```

---

# 16. Time Complexity

For every request:

```text
HashMap lookup = O(1) average

Window check = O(1)

Request count update = O(1)
```

Therefore:

```text
Time Complexity = O(1)
```

Space complexity:

```text
O(N)
```

Where:

```text
N = Number of users
```

---

# 17. Why HashMap Is Used

We need to quickly find the rate-limiting information for a user.

We could store users in a list:

```text
List<User>
```

But finding a user's data would require:

```text
O(N)
```

Using:

```java
HashMap<User, RateLimitData>
```

allows:

```text
Average lookup = O(1)
```

This makes the rate limiter efficient.

---

# 18. Important Design Decision

We use:

```text
User
        ↓
RateLimitData
```

instead of:

```text
User
        ↓
Integer
```

because rate-limiting requires multiple pieces of information.

Currently:

```text
requestCount
windowStartTime
```

In the future, we could add:

```text
lastRequestTime

userPlan

customLimit

blockedUntil

tokenCount
```

without changing the overall architecture.

---

# 19. Limitations of Fixed Window

The Fixed Window algorithm has a boundary problem.

Example:

```text
Limit = 5 requests per minute
```

A user can make:

```text
12:00:59

Request 1
Request 2
Request 3
Request 4
Request 5
```

Then immediately after the next window begins:

```text
12:01:01

Request 1
Request 2
Request 3
Request 4
Request 5
```

The user effectively sends:

```text
10 requests
```

within a very short time.

This is the major drawback of the Fixed Window algorithm.

---

# 20. Possible Improvements

The rate limiter can later support other algorithms.

```text
RateLimiterStrategy
        |
        |
        +----------------------+
        |                      |
        v                      v

FixedWindow            SlidingWindow


        |
        |
        +----------------------+
        |                      |
        v                      v

TokenBucket            LeakyBucket
```

Possible algorithms:

1. Fixed Window Counter
2. Sliding Window Log
3. Sliding Window Counter
4. Token Bucket
5. Leaky Bucket

---

# 21. Future Strategy Pattern

We can eventually create:

```java
interface RateLimiterStrategy {

    boolean allowRequest(
            Request request
    );
}
```

Then implement:

```java
class FixedWindowStrategy
        implements RateLimiterStrategy {

    @Override
    public boolean allowRequest(
            Request request) {

        // Fixed Window Logic

        return true;
    }
}
```

Similarly:

```text
SlidingWindowStrategy

TokenBucketStrategy

LeakyBucketStrategy
```

The `RateLimiter` can then delegate to the selected strategy.

---

# 22. Interview Explanation

A concise explanation for an interviewer:

> "I implemented a Fixed Window Counter Rate Limiter. Each incoming request contains the user and the timestamp. The RateLimiter supports multiple users, so I maintain a HashMap from User to RateLimitData. RateLimitData stores the request count and window start time for each user independently. When a request arrives, I first check whether the user already has rate-limiting state. If not, I create it. Then I check whether that user's window has expired. If it has, I reset the counter and start a new window. Finally, I check whether the maximum request limit has been reached. If yes, I reject the request; otherwise, I increment the count and allow it."

---

# 23. Final Architecture

```text
                         +--------+
                         |  User  |
                         +--------+
                              |
                              v
                         +---------+
                         | Request |
                         +---------+
                              |
                              v
                   +-------------------+
                   |    RateLimiter    |
                   +-------------------+
                              |
                              v
            HashMap<User, RateLimitData>
                              |
                  +-----------+-----------+
                  |                       |
                  v                       v

              User John               User Alice
                  |                       |
                  v                       v

           RateLimitData           RateLimitData

           requestCount            requestCount

           windowStartTime         windowStartTime
```

---

# 24. Summary

The current design consists of:

```text
User
 ↓
Request
 ↓
RateLimiter
 ↓
HashMap<User, RateLimitData>
 ↓
Fixed Window Counter Algorithm
 ↓
ALLOW / REJECT
```

The design:

```text
✓ Supports multiple users

✓ Maintains independent counters

✓ Maintains independent time windows

✓ Provides O(1) average request processing

✓ Is easy to extend

✓ Separates request data from rate-limiting state

✓ Can later support Strategy Pattern
```

---

# 25. Important Interview Note

If `User` is used directly as a key in:

```java
HashMap<User, RateLimitData>
```

then in a production-quality implementation, we should override:

```java
equals()
hashCode()
```

in the `User` class.

Otherwise, two different `User` objects representing the same logical user may be treated as different keys.

For example:

```java
User user1 =
        new User(
                "John",
                "192.168.1.1"
        );

User user2 =
        new User(
                "John",
                "192.168.1.1"
        );
```

Without overriding `equals()` and `hashCode()`:

```text
user1 != user2
```

from the HashMap's perspective.

A production design could instead use:

```java
HashMap<String, RateLimitData>
```

where the key is a stable identifier such as:

```text
userId

API key

IP address
```

For an LLD interview, mentioning this is a nice bonus and shows awareness of how `HashMap` keys work.

---

# End

Current implementation:

## Fixed Window Counter Rate Limiter

Next possible implementation:

```text
Token Bucket Rate Limiter
```

This would be a good next algorithm because the overall system remains similar, but the internal `RateLimitData` and request-processing logic change significantly.