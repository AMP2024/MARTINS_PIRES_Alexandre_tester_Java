package com.parkit.parkingsystem.integration;

import java.text.SimpleDateFormat;

import static com.parkit.parkingsystem.constants.TestConstants.NUMBER_OF_TICKETS;
import static com.parkit.parkingsystem.constants.Fare.CAR_RATE_PER_HOUR;
import static com.parkit.parkingsystem.constants.TestConstants.OUT_TIME_TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;


/**
 * This class is in charge of integration testing for parking within the database.
 * It handles typical operations such as parking a vehicle, exiting the parking lot,
 * and adding functionality for recurring users.
 */
@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    /**
     * This method instantiates necessary objects with appropriate configurations before all tests.
     */
    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    /**
     * This method sets up a common scenario to be used per testcase, utilising a mock to facilitate testing.
     */
    @BeforeEach
    public void setUpPerTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    /**
     * This test method checks the correct functioning of vehicle parking, including ticket saving and parking space occupation.
     */
    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        // Retrieves the saved ticket
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        //  Check if the ticket has been saved
        assertNotNull(ticket, "The ticket was not saved.");

        // Check if the parking space is still available
        assertFalse(ticket.getParkingSpot().isAvailable(), "The parking space is still available.");
    }

    /**
     * This method is essentially an internal version of 'testParkingACar', allows easy
     * call within other testing methods by passing boolean value that manages specifics in processIncomingVehicle method.
     */
    public void testParkingACar(boolean test) {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle(test);

        // Retrieve the registered ticket
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        // Check if the ticket has been stored
        assertNotNull(ticket, "The ticket has not been stored.");

        // Check if the parking space is still available
        assertFalse(ticket.getParkingSpot().isAvailable(), "The parking space is still available.");
    }

    /**
     * This test method examines proper parking checkout procedure,
     * including ticket price calculation and the correct setting of exit time.
     */
    @Test
    public void testParkingLotExit() {
        boolean test = true;
        testParkingACar(test);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle(test);

        double expectedTicketPrice = 4 * CAR_RATE_PER_HOUR;
        expectedTicketPrice = Math.round(expectedTicketPrice * 100.0) / 100.0;// Rounded to two decimal places.

        // Retrieve the existing ticket
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticket, "The ticket retrieval failed");
        assertEquals(expectedTicketPrice, ticket.getPrice(), "Ticket price for the recurring user is not calculated correctly");

        // Format the 'Date' object into the string format 'yyyy-MM-dd HH:mm:ss.S'
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String formattedOutTime = dateFormat.format(ticket.getOutTime());
        String expectedOutTime = dateFormat.format(OUT_TIME_TEST[0]);

        // Compare the formatted dates
        assertEquals(expectedOutTime, formattedOutTime, "The exit time is not correctly defined.");
    }

    /**
     * This test method ensures that the system correctly handles recurring users,
     * applying a discount and correctly calculating the new ticket price.
     */
    @Test
    public void testParkingLotExitRecurringUser() {

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        boolean test = true;
        double expectedTicketPrice = 4 * CAR_RATE_PER_HOUR;
        double expectedReducedTicketPrice = 0.95 * expectedTicketPrice; //5% discount
        expectedReducedTicketPrice = Math.round(expectedReducedTicketPrice * 100.0) / 100.0;// Rounded to two decimal places.
        Ticket ticket;

        for (int i = 0; i < NUMBER_OF_TICKETS; i++) {

            parkingService.processIncomingVehicle(test);

            parkingService.processExitingVehicle(test);

            ticket = ticketDAO.getTicket("ABCDEF");

            if (ticket == null) {
                System.out.println("\nError No ticket found for vehicle ");
                return;
            }

            if (i == 0) {
                // Apply 5% discount on recurring user
                assertEquals(expectedTicketPrice, ticket.getPrice(), "Ticket price for the recurring user is not calculated correctly");
            } else {
                assertEquals(expectedReducedTicketPrice, ticket.getPrice(), "Reduced Ticket price for the recurring user is not calculated correctly");
            }

        }

        int nbTickets = ticketDAO.getNbTicket("ABCDEF");

        assertEquals(NUMBER_OF_TICKETS, nbTickets, "The number of tickets for a recurring user is not correct");

    }


}


