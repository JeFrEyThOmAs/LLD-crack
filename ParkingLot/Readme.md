# 🚗 Parking Lot System - LLD

This repository contains my implementation of a **Parking Lot System** as part of my Low-Level Design problem-solving practice.

The goal of this project is not to provide the "perfect" or standard solution available online. Instead, it represents **my own approach to understanding the problem, identifying the objects involved, defining their relationships, and implementing the solution using Object-Oriented Programming and SOLID principles.**

---

# 📌 Problem Statement

Design a Parking Lot system that supports different types of vehicles and parking spots.

The system should be able to:

- Park a vehicle in a suitable parking spot.
- Check whether a parking spot supports a particular vehicle type.
- Generate a ticket when a vehicle is parked.
- Record the entry and exit time.
- Calculate the parking fee based on vehicle type and parking duration.
- Process the payment before allowing the vehicle to exit.
- Release the parking spot after successful payment.

For simplicity, the current implementation supports:

- Cars
- Bikes

---

# 🏗️ Basic Design

```text
Vehicle
 ├── Car
 └── Bike


ParkingLotSystem
 ├── ParkingLot
 │     └── ParkingSpot
 │             └── Vehicle
 │
 ├── FeeCalculator
 └── PaymentService


Ticket
 ├── Vehicle
 └── ParkingSpot




                         ┌─────────────────────┐
                         │  ParkingLotSystem   │
                         └──────────┬──────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
       ┌──────────────┐      ┌──────────────┐     ┌───────────────┐
       │  ParkingLot  │      │ FeeCalculator│     │PaymentService │
       └──────┬───────┘      └──────────────┘     └───────────────┘
              │
              │ manages
              ▼
       ┌──────────────┐
       │ ParkingSpot  │
       └──────┬───────┘
              │
              │ holds
              ▼
          ┌─────────┐
          │ Vehicle │
          └────┬────┘
               │
          ┌────┴────┐
          ▼         ▼
        Car       Bike


             Ticket
            /      \
           ▼        ▼
       Vehicle   ParkingSpot



┌──────────────────────────────┐
│           Vehicle            │
├──────────────────────────────┤
│ - registrationNumber : String│
├──────────────────────────────┤
│ + getRegistrationNumber()    │
│ + getVehicleType()           │
└───────────────△──────────────┘
                │
        ┌───────┴────────┐
        │                │
┌───────┴───────┐ ┌──────┴────────┐
│      Car      │ │      Bike      │
├───────────────┤ ├────────────────┤
│ getVehicleType│ │ getVehicleType │
└───────────────┘ └────────────────┘


┌──────────────────────────────┐
│        ParkingSpot           │
├──────────────────────────────┤
│ - spotNumber : int           │
│ - supportedVehicleType       │
│ - parkedVehicle : Vehicle    │
├──────────────────────────────┤
│ + isAvailable()              │
│ + canPark()                  │
│ + parkVehicle()              │
│ + removeVehicle()            │
└───────────────┬──────────────┘
                │
                │ parks
                ▼
             Vehicle


┌──────────────────────────────┐
│            Ticket            │
├──────────────────────────────┤
│ - vehicle : Vehicle          │
│ - parkingSpot : ParkingSpot  │
│ - entryTime : long           │
│ - exitTime : long            │
│ - paid : boolean             │
├──────────────────────────────┤
│ + recordExitTime()           │
│ + getParkingDuration()       │
│ + markAsPaid()               │
└───────────┬───────────┬──────┘
            │           │
            ▼           ▼
        Vehicle     ParkingSpot


┌──────────────────────────────┐
│         ParkingLot           │
├──────────────────────────────┤
│ - parkingSpots : List        │
├──────────────────────────────┤
│ + addParkingSpot()           │
│ + findAvailableSpot()        │
│ + parkVehicle()              │
│ + releaseSpot()              │
└───────────────◇──────────────┘
                │
                │ contains many
                ▼
          ParkingSpot


┌──────────────────────────────┐
│      ParkingLotSystem        │
├──────────────────────────────┤
│ - parkingLot                 │
│ - feeCalculator              │
│ - paymentService             │
├──────────────────────────────┤
│ + parkVehicle()              │
│ + unparkVehicle()            │
└───────┬─────────┬────────────┘
        │         │
        ▼         ▼
   ParkingLot  FeeCalculator
                    │
                    │
             PaymentService



Car ────────┐
            ├── extends ──> Vehicle
Bike ───────┘

ParkingLot ─── contains ───> ParkingSpot

ParkingSpot ─── parks ───> Vehicle

Ticket ─── references ───> Vehicle
       └── references ───> ParkingSpot

ParkingLotSystem ─── uses ───> ParkingLot
                 ├── uses ───> FeeCalculator
                 └── uses ───> PaymentService