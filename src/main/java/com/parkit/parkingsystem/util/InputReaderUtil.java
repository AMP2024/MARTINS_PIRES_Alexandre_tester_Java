package com.parkit.parkingsystem.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

/**
 * This class provides utility methods for reading user input from the console.
 */
public class InputReaderUtil {

    /**
     * scan is a constant instance of the Scanner class that is used to read the user's input from the standard input (console).
     */
    private static final Scanner scan = new Scanner(System.in);

    /**
     * logger is a constant instance of Logger, initialized with the name "InputReaderUtil".
     * It is used for logging error, warning, and info messages.
     */
    private static final Logger logger = LogManager.getLogger("InputReaderUtil");

    /**
     * Reads the user's selection from the input.
     *
     * @return the user's selection as an integer
     */
    public int readSelection() {
        try {
            return Integer.parseInt(scan.nextLine());
        } catch(Exception e) {
            logger.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. Please enter valid number for proceeding further");
            return -1;
        }
    }

    /**
     * Reads the vehicle registration number from the user input.
     *
     * @return the vehicle registration number entered by the user
     *
     * @throws IllegalArgumentException if an invalid input is provided
     */
    public String readVehicleRegistrationNumber() throws IllegalArgumentException {
        try {
            String vehicleRegNumber= scan.nextLine();
            if(vehicleRegNumber == null || vehicleRegNumber.trim().isEmpty()) {

                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        } catch(IllegalArgumentException e){
            logger.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. Please enter a valid string for vehicle registration number");
            throw e;
        }
    }


}
