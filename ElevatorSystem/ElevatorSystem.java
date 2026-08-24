import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

enum Direction {
    UP,
    DOWN,
    IDLE
}

class ElevatorUnit {

    private int capacity;
    private int currentFloor;

    private TreeSet<Integer> upRequests;
    private TreeSet<Integer> downRequests;

    private Direction direction;

    private boolean doorClosed;


    public ElevatorUnit(int capacity, int currentFloor) {

        this.capacity = capacity;
        this.currentFloor = currentFloor;

        this.upRequests = new TreeSet<>();
        this.downRequests = new TreeSet<>();

        this.direction = Direction.IDLE;

        this.doorClosed = true;
    }


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }


    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }


    public TreeSet<Integer> getUpRequests() {
        return upRequests;
    }

    public TreeSet<Integer> getDownRequests() {
        return downRequests;
    }


    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }


    public boolean isDoorClosed() {
        return doorClosed;
    }

    public void setDoorClosed(boolean doorClosed) {
        this.doorClosed = doorClosed;
    }


    /*
     * Add a new floor request.
     *
     * This does NOT move the elevator.
     * It only adds the request.
     */
    public void addRequest(int floor) {

        if (floor > currentFloor) {

            upRequests.add(floor);

        } else if (floor < currentFloor) {

            downRequests.add(floor);

        } else {

            System.out.println(
                    "Elevator already at floor "
                            + floor
            );
        }
    }


    public boolean hasRequests() {

        return !upRequests.isEmpty()
                || !downRequests.isEmpty();
    }


    /*
     * Move exactly ONE floor.
     */
    public void goOneFloor() {

        if (!hasRequests()) {

            direction = Direction.IDLE;

            System.out.println("Elevator is idle");

            return;
        }


        /*
         * Decide direction.
         */

        if (direction == Direction.IDLE) {

            if (!upRequests.isEmpty()) {

                direction = Direction.UP;

            } else {

                direction = Direction.DOWN;
            }
        }


        /*
         * If going UP but no UP requests remain,
         * change direction.
         */

        if (direction == Direction.UP
                && upRequests.isEmpty()) {

            direction = Direction.DOWN;
        }


        /*
         * If going DOWN but no DOWN requests remain,
         * change direction.
         */

        else if (direction == Direction.DOWN
                && downRequests.isEmpty()) {

            direction = Direction.UP;
        }


        /*
         * Move ONE floor.
         */

        if (direction == Direction.UP) {

            currentFloor++;

        } else if (direction == Direction.DOWN) {

            currentFloor--;
        }


        System.out.println(
                "Elevator at floor "
                        + currentFloor
        );


        /*
         * Check whether this floor was requested.
         */

        if (upRequests.contains(currentFloor)
                || downRequests.contains(currentFloor)) {

            stopAtFloor();
        }
    }


    private void stopAtFloor() {

        System.out.println(
                "Stopping at floor "
                        + currentFloor
        );

        openDoor();

        upRequests.remove(currentFloor);

        downRequests.remove(currentFloor);

        closeDoor();
    }


    private void openDoor() {

        doorClosed = false;

        System.out.println("Door opened");
    }


    private void closeDoor() {

        doorClosed = true;

        System.out.println("Door closed");
    }
}


/*
 * Represents a floor in the building.
 */
class Floor {

    private int floorNumber;

    private ElevatorController controller;


    public Floor(
            int floorNumber,
            ElevatorController controller) {

        this.floorNumber = floorNumber;

        this.controller = controller;
    }


    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }


    public ElevatorController getController() {
        return controller;
    }

    public void setController(
            ElevatorController controller) {

        this.controller = controller;
    }


    public void pressUpButton() {

        System.out.println(
                "UP button pressed at floor "
                        + floorNumber
        );

        controller.requestElevator(
                floorNumber,
                Direction.UP
        );
    }


    public void pressDownButton() {

        System.out.println(
                "DOWN button pressed at floor "
                        + floorNumber
        );

        controller.requestElevator(
                floorNumber,
                Direction.DOWN
        );
    }
}


/*
 * Controls multiple elevators.
 */
class ElevatorController {

    private List<ElevatorUnit> elevators;


    public ElevatorController() {

        elevators = new ArrayList<>();
    }


    public List<ElevatorUnit> getElevators() {
        return elevators;
    }


    public void addElevator(
            ElevatorUnit elevator) {

        elevators.add(elevator);
    }


    /*
     * Receives request from a floor
     * and chooses the closest elevator.
     */
    public void requestElevator(
            int floor,
            Direction requestedDirection) {

        ElevatorUnit chosen = null;

        int minimumDistance =
                Integer.MAX_VALUE;


        for (ElevatorUnit elevator : elevators) {

            int distance =
                    Math.abs(
                            elevator.getCurrentFloor()
                                    - floor
                    );

            if (distance < minimumDistance) {

                minimumDistance = distance;

                chosen = elevator;
            }
        }


        if (chosen != null) {

            System.out.println(
                    "Elevator selected for floor "
                            + floor
            );

            chosen.addRequest(floor);
        }
    }
}


public class ElevatorSystem {

    public static void main(String[] args) {

        /*
         * Create elevators.
         */

        ElevatorUnit elevator1 =
                new ElevatorUnit(10, 0);

        ElevatorUnit elevator2 =
                new ElevatorUnit(10, 5);


        /*
         * Create controller.
         */

        ElevatorController controller =
                new ElevatorController();

        controller.addElevator(elevator1);

        controller.addElevator(elevator2);


        /*
         * Create floors.
         */

        Floor floor2 =
                new Floor(2, controller);

        Floor floor7 =
                new Floor(7, controller);

        Floor floor9 =
                new Floor(9, controller);


        /*
         * People press elevator buttons.
         */

        floor7.pressUpButton();

        floor2.pressDownButton();

        floor9.pressDownButton();


        /*
         * Elevator simulation.
         *
         * Every iteration,
         * each elevator moves ONE floor.
         */

        while (elevator1.hasRequests()
                || elevator2.hasRequests()) {

            if (elevator1.hasRequests()) {

                elevator1.goOneFloor();
            }


            if (elevator2.hasRequests()) {

                elevator2.goOneFloor();
            }
        }


        System.out.println(
                "\nAll requests completed."
        );
    }
}