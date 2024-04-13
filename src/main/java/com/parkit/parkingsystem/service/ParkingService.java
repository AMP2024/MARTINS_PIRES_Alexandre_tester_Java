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

import static com.parkit.parkingsystem.constants.TestConstants.IN_TIME_TEST;
import static com.parkit.parkingsystem.constants.TestConstants.OUT_TIME_TEST;

/**
 * This class represents a parking service that manages the parking spots and tickets.
 * The ParkingService class is responsible for handling the incoming and exiting vehicles in a parking lot.
 */
public class ParkingService {

    /**
     * The Logger object, associated with the "ParkingService",
     * is responsible for logging messages specific to this component of the system or application.
     */
    private static final Logger logger = LogManager.getLogger("ParkingService");

    /**
     * The FareCalculatorService class provides methods to calculate the parking fare based on the parking duration.
     */
    private static final FareCalculatorService fareCalculatorService = new FareCalculatorService(); //

    /**
     * The InputReaderUtil class is used to handle user input.
     * It provides methods that read the user's input from the console.
     */
    private final InputReaderUtil inputReaderUtil;

    /**
     * The ParkingSpotDAO class is responsible for accessing and updating parking spot information in the database.
     */
    private final ParkingSpotDAO parkingSpotDAO;

    /**
     * The TicketDAO class provides methods for saving, updating, and retrieving parking tickets from the database.
     */
    private final TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * Processes the incoming vehicle.
     *
     * @param test A boolean indicating whether the method is being called for a test or not.
     */
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

    /**
     * Processes the incoming vehicle when calling the method without an argument.
     * Call the method with the Boolean 'test' set to false.
     */
    public void processIncomingVehicle() {
        processIncomingVehicle(false);
    }

    /**
     * Retrieves the vehicle registration number entered by the user.
     *
     * @return The vehicle registration number entered by the user.
     */
    private String getVehicleRegNumber() {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * Retrieves the next available parking spot if it is available.
     *
     * @return The next available parking spot, or null if no spot is available.
     */
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

    /**
     * Processes the exiting vehicle.
     *
     * @param test A boolean indicating whether the method is being called for a test or not.
     */
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
            }

        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }

    /**
     * Processes the exiting vehicle when calling the method without an argument.
     * Call the method with the Boolean 'test' set to false.
     */
    public void processExitingVehicle() {
        processExitingVehicle(false);
    }

    /**
     * Retrieves the selected vehicle type from the user.
     *
     * @return The selected vehicle type as a ParkingType enum value.
     * @throws IllegalArgumentException If an incorrect input is provided by the user.
     */
    public ParkingType getVehicleType() {
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

}
