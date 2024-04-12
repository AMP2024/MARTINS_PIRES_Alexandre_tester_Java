package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractiveShell {

    private static final Logger logger = LogManager.getLogger("InteractiveShell");

    public static void loadInterface() {
        logger.info("App initialized!!!");
        System.out.println("\nWelcome to Parking System!");

        boolean continueApp = true;
        InputReaderUtil inputReaderUtil = new InputReaderUtil();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        TicketDAO ticketDAO = new TicketDAO();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        while (continueApp) {
            loadMenu();
            int option = inputReaderUtil.readSelection();
            switch (option) {
                case 1: {
                    parkingService.processIncomingVehicle();
                    break;
                }
                case 2: {
                    parkingService.processExitingVehicle();
                    break;
                }
                case 3: {
                    System.out.println("\nExiting from the system!");
                    continueApp = false;
                    break;
                }
                default:
                    System.out.println("\nUnsupported option. Please enter a number corresponding to the provided menu");
            }
        }
    }

    private static void loadMenu() {
        System.out.println("\nPlease select an option. Simply enter the number to choose an action");
        System.out.println("\n1 New Vehicle Entering - Allocate Parking Space");
        System.out.println("\n2 Vehicle Exiting - Generate Ticket Price");
        System.out.println("\n3 Shutdown System");
    }

}
