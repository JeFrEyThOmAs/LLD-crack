# Elevator System — Low-Level Design

## Problem Statement
Design a simplified elevator system with multiple floors and elevators. A user can press UP or DOWN on a floor. The request goes to an `ElevatorController`, which selects an elevator. Each elevator maintains pending requests and moves one floor at a time.

## Requirements
- Multiple elevators
- Multiple floors
- UP and DOWN buttons
- Central request controller
- Separate UP and DOWN request sets
- One-floor-at-a-time movement
- Door opens and closes at requested floors
- Simple closest-elevator assignment

## Architecture

```text
Floor
  |
  | button press
  v
ElevatorController
  |
  | chooses elevator
  v
ElevatorUnit
  |
  +-- currentFloor
  +-- direction
  +-- UP requests
  +-- DOWN requests
  +-- door operations
```

## Core Classes

### Direction

```java
enum Direction {
    UP,
    DOWN,
    IDLE
}
```

### ElevatorUnit

Responsibilities:
- Maintain current floor and capacity
- Store UP and DOWN requests using `TreeSet`
- Move one floor at a time
- Stop at requested floors
- Open and close doors

Important fields:

```java
private int capacity;
private int currentFloor;
private TreeSet<Integer> upRequests;
private TreeSet<Integer> downRequests;
private Direction direction;
private boolean doorClosed;
```

Request flow:

```text
addRequest(floor)
       ↓
UP or DOWN TreeSet
       ↓
goOneFloor()
       ↓
Move one floor
       ↓
Check whether current floor is requested
       ↓
Stop → Open Door → Remove Request → Close Door
```

### Floor

A floor generates requests. It does not directly move an elevator.

```text
Person presses UP/DOWN
        ↓
Floor
        ↓
ElevatorController
```

### ElevatorController

Responsibilities:
- Receive requests
- Manage multiple elevators
- Select an elevator
- Assign the request

Current strategy:

```text
Find elevator with minimum:
abs(elevator.currentFloor - requestedFloor)
```

## Class Diagram

```text
+----------------------+
| Direction            |
+----------------------+
| UP                   |
| DOWN                 |
| IDLE                 |
+----------------------+


+----------------------+
| ElevatorUnit         |
+----------------------+
| - capacity           |
| - currentFloor       |
| - upRequests         |
| - downRequests       |
| - direction          |
| - doorClosed         |
+----------------------+
| + addRequest()       |
| + hasRequests()      |
| + goOneFloor()       |
| - stopAtFloor()      |
| - openDoor()         |
| - closeDoor()        |
+----------------------+


+----------------------+
| Floor                |
+----------------------+
| - floorNumber        |
| - controller         |
+----------------------+
| + pressUpButton()    |
| + pressDownButton()  |
+----------------------+


+-----------------------------+
| ElevatorController          |
+-----------------------------+
| - List<ElevatorUnit>        |
+-----------------------------+
| + addElevator()             |
| + requestElevator()         |
+-----------------------------+
```

## Complete Request Flow

Example: a person at Floor 7 presses UP.

```text
Floor 7
   |
   | pressUpButton()
   v
ElevatorController
   |
   | requestElevator(7, UP)
   v
Choose closest elevator
   |
   | addRequest(7)
   v
ElevatorUnit
   |
   | Store request
   v
UP / DOWN TreeSet
```

Then:

```text
Current floor: 4
Request: 7

4 → 5
5 → 6
6 → 7

Stop
Open door
Remove request
Close door
```

## Why Move One Floor at a Time?

Instead of:

```text
Calculate entire journey
        ↓
Execute entire journey
```

we use:

```text
Move one floor
      ↓
Check pending requests
      ↓
Stop if required
      ↓
Re-evaluate state
      ↓
Move again
```

This makes the design capable of accepting new requests while the elevator is operating.

For interview simulation, requests can be added synchronously. In production, request submission could be event-driven or concurrent.

## Main Simulation

```java
ElevatorUnit elevator1 =
        new ElevatorUnit(10, 0);

ElevatorUnit elevator2 =
        new ElevatorUnit(10, 5);

ElevatorController controller =
        new ElevatorController();

controller.addElevator(elevator1);
controller.addElevator(elevator2);

Floor floor7 =
        new Floor(7, controller);

Floor floor2 =
        new Floor(2, controller);

floor7.pressUpButton();
floor2.pressDownButton();
```

Movement is simulated with:

```java
while (elevator1.hasRequests()
        || elevator2.hasRequests()) {

    if (elevator1.hasRequests()) {
        elevator1.goOneFloor();
    }

    if (elevator2.hasRequests()) {
        elevator2.goOneFloor();
    }
}
```

## Design Decisions

### Floor
Responsible for:
- Button presses
- Request generation

### ElevatorController
Responsible for:
- Receiving requests
- Selecting an elevator
- Assigning requests

### ElevatorUnit
Responsible for:
- Request storage
- Movement
- Direction
- Stopping
- Door operations

## Future Improvements

### Smarter Scheduling
Consider:
- Elevator direction
- Pending requests
- Whether the elevator is already passing the requested floor
- Estimated arrival time

### Elevator State

```java
enum ElevatorState {
    MOVING,
    IDLE,
    MAINTENANCE
}
```

### Capacity

Add:

```java
private int currentLoad;
```

### Request Object

```java
class ElevatorRequest {
    private int sourceFloor;
    private Direction direction;
}
```

### Inside Requests

After entering an elevator:

```text
Passenger presses destination
        ↓
Elevator.addRequest(destinationFloor)
```

### Selection Strategy

```text
ElevatorSelectionStrategy
        |
        +-- NearestElevatorStrategy
        +-- DirectionBasedStrategy
        +-- LeastLoadedStrategy
```

## Interview Explanation

> I separated the system into `Floor`, `ElevatorController`, and `ElevatorUnit`. Floors generate UP or DOWN requests. The controller receives requests and assigns an elevator using a nearest-elevator strategy. Each elevator maintains its current floor, direction, door state, and separate sorted sets for UP and DOWN requests. The elevator moves one floor at a time and stops when it reaches a requested floor. The design can later be extended with better scheduling, capacity handling, maintenance states, request objects, and concurrency.

## Final Flow

```text
Person presses button
        ↓
Floor
        ↓
ElevatorController
        ↓
Select Elevator
        ↓
ElevatorUnit.addRequest()
        ↓
Store in UP / DOWN TreeSet
        ↓
goOneFloor()
        ↓
Check current floor
        ↓
Stop if requested
        ↓
Open door
        ↓
Remove request
        ↓
Close door
        ↓
Continue
```
