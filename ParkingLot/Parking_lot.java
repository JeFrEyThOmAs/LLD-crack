import java.util.ArrayList;
import java.util.List;
abstract class Vehicle {

    private String registrationNumber;

    public Vehicle(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public abstract String getVehicleType();
}


class Car extends Vehicle {

    public Car(String registrationNumber) {
        super(registrationNumber);
    }

    @Override
    public String getVehicleType() {
        return "CAR";
    }
}


class Bike extends Vehicle {

    public Bike(String registrationNumber) {
        super(registrationNumber);
    }

    @Override
    public String getVehicleType() {
        return "BIKE";
    }
}

// --------------------------------------------------------------------------------------------

class ParkingSpot {

    private int spotNumber;

    private String supportedVehicleType;

    private Vehicle parkedVehicle;


    public ParkingSpot(
            int spotNumber,
            String supportedVehicleType
    ) {
        this.spotNumber = spotNumber;
        this.supportedVehicleType =
                supportedVehicleType;
    }


    public boolean isAvailable() {

        return parkedVehicle == null;
    }


    public boolean canPark(
            Vehicle vehicle
    ) {

        return vehicle.getVehicleType()
                .equals(supportedVehicleType);
    }


    public boolean parkVehicle(
            Vehicle vehicle
    ) {

        if (!isAvailable()) {

            System.out.println(
                    "Spot is already occupied."
            );

            return false;
        }


        if (!canPark(vehicle)) {

            System.out.println(
                    "This spot does not support "
                            + vehicle.getVehicleType()
            );

            return false;
        }


        parkedVehicle = vehicle;

        return true;
    }


    public void removeVehicle() {

        parkedVehicle = null;
    }


    public int getSpotNumber() {

        return spotNumber;
    }


    public Vehicle getParkedVehicle() {

        return parkedVehicle;
    }
}

// --------------------------------------------------------------------------------------------

class Ticket {

    private Vehicle vehicle;

    private ParkingSpot parkingSpot;

    private long entryTime;

    private long exitTime;

    private boolean paid;


    public Ticket(
            Vehicle vehicle,
            ParkingSpot parkingSpot
    ) {

        this.vehicle = vehicle;

        this.parkingSpot = parkingSpot;

        this.entryTime =
                System.currentTimeMillis();

        this.paid = false;
    }


    public Vehicle getVehicle() {

        return vehicle;
    }


    public ParkingSpot getParkingSpot() {

        return parkingSpot;
    }


    public long getEntryTime() {

        return entryTime;
    }


    public void recordExitTime() {

        this.exitTime =
                System.currentTimeMillis();
    }


    public long getParkingDuration() {

        if (exitTime == 0) {

            return System.currentTimeMillis()
                    - entryTime;
        }

        return exitTime - entryTime;
    }


    public boolean isPaid() {

        return paid;
    }


    public void markAsPaid() {

        paid = true;
    }
}


// --------------------------------------------------------------------------------------------

class PaymentService {

    public boolean processPayment(double amount) {

        System.out.println(
                "Processing payment of ₹" + amount
        );

        // Assume payment is successful
        System.out.println(
                "Payment successful."
        );

        return true;
    }
}

// --------------------------------------------------------------------------------------------

class FeeCalculator {

    public double calculateFee(Ticket ticket) {

        long durationInMillis =
                ticket.getParkingDuration();

        long durationInHours =
                (long) Math.ceil(
                        durationInMillis
                                / (1000.0 * 60 * 60)
                );


        String vehicleType =
                ticket.getVehicle()
                        .getVehicleType();


        if (vehicleType.equals("CAR")) {

            return durationInHours * 20;
        }


        if (vehicleType.equals("BIKE")) {

            return durationInHours * 10;
        }


        return 0;
    }
}


// --------------------------------------------------------------------------------------------

class ParkingLot {

    private List<ParkingSpot> parkingSpots;


    public ParkingLot() {
        parkingSpots = new ArrayList<>();
    }


    public void addParkingSpot(
            ParkingSpot parkingSpot
    ) {

        parkingSpots.add(parkingSpot);
    }


    public ParkingSpot findAvailableSpot(
            Vehicle vehicle
    ) {

        for (ParkingSpot spot : parkingSpots) {

            if (spot.isAvailable()
                    && spot.canPark(vehicle)) {

                return spot;
            }
        }

        return null;
    }


    public Ticket parkVehicle(Vehicle vehicle) {

        ParkingSpot spot =
                findAvailableSpot(vehicle);

        if (spot == null) {

            System.out.println(
                    "No suitable parking spot available."
            );

            return null;
        }

        boolean parked =
                spot.parkVehicle(vehicle);

        if (!parked) {
            return null;
        }

        Ticket ticket =
                new Ticket(
                        vehicle,
                        spot
                );

        System.out.println(
                vehicle.getRegistrationNumber()
                        + " parked at spot "
                        + spot.getSpotNumber()
        );

        return ticket;
    }

    public void releaseSpot(
            ParkingSpot spot
    ) {

        spot.removeVehicle();

        System.out.println(
                "Spot "
                        + spot.getSpotNumber()
                        + " is now available."
        );
    }
}

// --------------------------------------------------------------------------------------------

class ParkingLotSystem {

    private ParkingLot parkingLot;

    private FeeCalculator feeCalculator;

    private PaymentService paymentService;


    public ParkingLotSystem(
            ParkingLot parkingLot,
            FeeCalculator feeCalculator,
            PaymentService paymentService
    ) {

        this.parkingLot = parkingLot;
        this.feeCalculator = feeCalculator;
        this.paymentService = paymentService;
    }


    public Ticket parkVehicle(
            Vehicle vehicle
    ) {

        return parkingLot.parkVehicle(vehicle);
    }


    public boolean unparkVehicle(
            Ticket ticket
    ) {

        // 1. Record exit time
        ticket.recordExitTime();


        // 2. Calculate fee
        double amount =
                feeCalculator.calculateFee(ticket);

        System.out.println(
                "Total parking fee: ₹" + amount
        );


        // 3. Process payment
        boolean paymentSuccessful =
                paymentService.processPayment(amount);


        // 4. If payment fails, do not release the spot
        if (!paymentSuccessful) {

            System.out.println(
                    "Payment failed. Cannot exit."
            );

            return false;
        }


        // 5. Mark ticket as paid
        ticket.markAsPaid();


        // 6. Release the parking spot
        parkingLot.releaseSpot(
                ticket.getParkingSpot()
        );


        System.out.println(
                "Vehicle can exit now."
        );

        return true;
    }
}
// --------------------------------------------------------------------------------------------


public class Parking_lot_tutorial_version {
    public static void main(String[] args) {

    }
}
