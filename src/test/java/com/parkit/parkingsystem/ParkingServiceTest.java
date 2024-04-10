package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private Ticket ticket;
    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        // Initialization of the parking spot and the ticket only once
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        // Simulate the entry of the vehicle registration number
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // Simulate the behavior of TicketDAO to return the ticket
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

        // Simulate behavior of ParkingSpotDAO to return true for updateParking method
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        // Simulation of the behavior of TicketDAO to return the number of tickets for the vehicle
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1); // Simulation of a ticket for the vehicle

        // Creating a new instance of ParkingService with mocked dependencies
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Calling the method to be tested
        parkingService.processExitingVehicle();

        // Verify that updateTicket was called once
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));

        // Check that ParkingSpotDAO's updateParking method is called once
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

        // Check that TicketDAO's getNbTicket method is called once
        verify(ticketDAO, times(1)).getNbTicket(anyString());

        // Verify that getIncomingTicket was called twice
        verify(ticketDAO, times(1)).getTicket(anyString());

        assertTrue(ticket.getParkingSpot().isAvailable(), "Parking Spot was not freed up by the system");

        // Check if the OutTime for the ticket is not null
        assertNotNull(ticket.getOutTime(), "Out time for ticket is null");

    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {
        // Simulate the entry of vehicle sighting
        when(inputReaderUtil.readSelection()).thenReturn(1);

        // Simulate behavior of ParkingSpotDAO to return the next available spot
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        // Simulate the behavior of the input to return a vehicle registration number
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // Simulate the behavior of TicketDAO to return the number of tickets for the vehicle
        when(ticketDAO.getNbTicket(eq("ABCDEF"))).thenReturn(0);

        // Add a mock for the behavior of the call to updateParking in ParkingSpotDAO
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);// MODIFICATION ADD

        // Calling the method to be tested
        parkingService.processIncomingVehicle();

        // Verifying that the updateParking method of ParkingSpotDAO is called once
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

        // Verify that TicketDAO's saveTicket method is called once
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));

        // Verify that TicketDAO's getNbTicket method is called once with the correct vehicle registration number
        verify(ticketDAO, times(1)).getNbTicket(eq("ABCDEF"));

    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        // Simulate the entry of the vehicle registration number
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // Simulate the behavior of TicketDAO to return the ticket
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        // Simulating the behavior of TicketDAO to return false when calling the updateTicket method
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        // Simulate the behavior of TicketDAO to return the number of tickets for the vehicle
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        // Calling the method to be tested
        parkingService.processExitingVehicle();

        // Check that TicketDAO's getIncomingTicket method is called once with the correct vehicle registration number
        verify(ticketDAO, times(1)).getTicket(eq("ABCDEF"));

        // Check that TicketDAO's getNbTicket method is called once with the correct vehicle registration number
        verify(ticketDAO, times(1)).getNbTicket(eq("ABCDEF"));

        // Check that TicketDAO's updateTicket method is called once
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // Simulate the entry of vehicle sighting
        when(inputReaderUtil.readSelection()).thenReturn(1);

        // Simulate behavior of ParkingSpotDAO to return the next available spot
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        // Calling the method to be tested
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Checking that the returned spot is not null
        assertNotNull(parkingSpot, "Parking slot is null");

        // Checking that the ID of the spot is correct
        assertEquals(1, parkingSpot.getId(), "Parking spot ID is incorrect");

        // Checking that the spot is available
        assertTrue(parkingSpot.isAvailable(), "Parking slot is not available");

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Simulate the entry of vehicle sighting
        when(inputReaderUtil.readSelection()).thenReturn(1);

        // Simulate behavior of ParkingSpotDAO to return 0 when no spot is available
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

        // Calling the method to be tested
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Checking that the returned spot is null
        assertNull(parkingSpot, "The parkingSpot is not null as expected");

        // Check that ParkingSpotDAO's getNextAvailableSlot method is called once
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // Simulate the entry of vehicle sighting with an incorrect argument (e.g., 3)
        when(inputReaderUtil.readSelection()).thenReturn(3);

        // Calling the method to be tested
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Check that the returned spot is null because the entered argument is incorrect
        assertNull(parkingSpot, "The parkingSpot is not null as expected");

        // Check that ParkingSpotDAO's getNextAvailableSlot method is not called
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
    }

}
