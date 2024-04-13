package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The App class represents the entry point of the Parking System application.
 * It initializes the application and loads the user interface for interaction.
 */
public class App {
    private static final Logger logger = LogManager.getLogger("App");
    /**
     * The App class represents the entry point of the Parking System application.
     * It initializes the application and loads the user interface for interaction.
     */
    public static void main(String[] args){
        logger.info("Initializing Parking System");
        InteractiveShell.loadInterface();
    }
}

