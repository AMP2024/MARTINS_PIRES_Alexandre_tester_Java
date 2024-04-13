package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The InteractiveShell class represents an interactive shell for the Parking System application.
 * It provides methods for loading the interface and displaying the menu options to the user.
 */
public class InteractiveShell {

    private static final Logger logger = LogManager.getLogger("InteractiveShell");

    /**
     * Initializes the Parking System application and loads the interface for user interaction.
     * It displays a welcome message and presents the menu options to the user.
     * The user can select an option by entering the corresponding number.
     * This method processes the user's selection and executes the corresponding action.
     * The application continues to run until the user chooses to exit.
     */
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

    /**
     * Loads the menu options for the Parking System application.
     * The menu provides different actions that the user can choose by entering the corresponding number.
     * This method is used in the InteractiveShell class to display the menu to the user.
     */
    private static void loadMenu() {
        System.out.println("\nPlease select an option. Simply enter the number to choose an action");
        System.out.println("\n1 New Vehicle Entering - Allocate Parking Space");
        System.out.println("\n2 Vehicle Exiting - Generate Ticket Price");
        System.out.println("\n3 Shutdown System");
    }

}
