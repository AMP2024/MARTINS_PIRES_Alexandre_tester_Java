package com.parkit.parkingsystem.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

import static com.parkit.parkingsystem.constants.DBConstants.IN_TIME_TEST;
import static com.parkit.parkingsystem.constants.DBConstants.OUT_TIME_TEST;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static final FareCalculatorService fareCalculatorService = new FareCalculatorService(); //

    private final InputReaderUtil inputReaderUtil;
    private final ParkingSpotDAO parkingSpotDAO;
    private final TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot == null || parkingSpot.getId() <= 0) {
                System.out.println("\nParkingSpot object is Null or its ID is not valid");
                return;
            }

            String vehicleRegNumber = getVehicleRegNumber();
            if (vehicleRegNumber.isEmpty()) {
                System.out.println("\nUnable to retrieve the vehicle registration number");
                return;
            }

            int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);
            if (nbTickets > 0) {
                System.out.println("\nWelcome back! As a regular user of our parking, you will get a 5% discount.");
            }

            parkingSpot.setAvailable(false);
            boolean isUpdated = parkingSpotDAO.updateParking(parkingSpot);
            if (!isUpdated) {
                System.out.println("\nError when updating the parkingSpot object");
                return;
            }

            Ticket ticket = new Ticket();
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(vehicleRegNumber);
            ticket.setPrice(0);


            ticket.setInTime(new Date());
            ticket.setOutTime(null);


            boolean isSaved = ticketDAO.saveTicket(ticket);
            if (!isSaved) {
                System.out.println("\nError the ticket was not saved");
                return;
            }

            System.out.println("\nThe Ticket has been successfully generated and stored in the database");
            System.out.println("\nPlease park your vehicle in spot number:" + parkingSpot.getId());
            System.out.println("\nRecorded in-time for vehicle number:" + vehicleRegNumber + " is:" + ticket.getInTime());
        } catch (Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    public void processIncomingVehicle(boolean test) {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot == null || parkingSpot.getId() <= 0) {
                System.out.println("\nThe ParkingSpot object is Null or its ID is not valid.");
                return;
            }

            String vehicleRegNumber = getVehicleRegNumber();
            if (vehicleRegNumber.isEmpty()) {
                System.out.println("\nUnable to retrieve the vehicle's registration number.");
                return;
            }

            int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);

            if (nbTickets > 0) {
                System.out.println("\nWelcome back! As a regular user of our parking, you will get a 5% discount.");
            }

            parkingSpot.setAvailable(false);

            boolean isUpdated = parkingSpotDAO.updateParking(parkingSpot);

            if (!isUpdated) {
                System.out.println("\nError while updating the ParkingSpot object");
                return;
            }

            Ticket ticket = new Ticket();
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(vehicleRegNumber);
            ticket.setPrice(0);

            // If test equals true, we change the value of inTime for integration tests
            if (test) {
                ticket.setInTime(IN_TIME_TEST[nbTickets]);
                ticket.setOutTime(null);
            } else {
                ticket.setInTime(new Date());
                ticket.setOutTime(null);
            }

            boolean isSaved = ticketDAO.saveTicket(ticket);
            if (!isSaved) {
                System.out.println("\nError while saving the ticket.");
                return;
            }

            System.out.println("\nThe Ticket has been successfully generated and stored in the database");
            System.out.println("\nPlease park your vehicle in spot number:" + parkingSpot.getId());
            System.out.println("\nRecorded in-time for vehicle number:" + vehicleRegNumber + " is:" + ticket.getInTime());
        } catch (Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    private String getVehicleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch (Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehicleType() {
        System.out.println("\nPlease select vehicle type from menu");
        System.out.println("\n1 CAR");
        System.out.println("\n2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("\nIncorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    public void processExitingVehicle(boolean test) {
        try {
            boolean isRecurringUser;
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            // Check if it is not the first visit
            int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);
            Date outTime;

            if (ticket == null) {
                System.out.println("\nNo ticket found for vehicle " + vehicleRegNumber);
                return;
            } else {
                // If test equals true, we change the value of inTime for integration tests
                if (test) {
                    outTime = OUT_TIME_TEST[(nbTickets - 1)];
                } else {
                    outTime = new Date();
                }
            }

            if (nbTickets > 1) {

                isRecurringUser = true;
                System.out.println("\nWelcome back! As a regular user of our parking, you will get a 5% discount.");
            } else {
                isRecurringUser = false;
            }

            ticket.setOutTime(outTime);

            // Apply discount if it's a regular user
            fareCalculatorService.calculateFare(ticket, isRecurringUser);

            if (ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                //parkingSpot = getNextParkingNumberIfAvailable();
            }

        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }

    public void processExitingVehicle() {
        try {

            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

            Date outTime = new Date();

            // Check if it is not the first visit
            int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);

            boolean isRecurringUser = nbTickets > 1;

            if (isRecurringUser) {
                System.out.println("Welcome back! As a regular user of our parking, you will get a 5% discount.");
            }

            ticket.setOutTime(outTime);

            // Apply discount if it's a regular user
            fareCalculatorService.calculateFare(ticket, isRecurringUser);


            if (ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);

                System.out.println("\nPlease pay the parking fare:" + ticket.getPrice());
                System.out.println("\nRecorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            } else {
                System.out.println("\nUnable to update ticket information. Error occurred");
            }
        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }

}
