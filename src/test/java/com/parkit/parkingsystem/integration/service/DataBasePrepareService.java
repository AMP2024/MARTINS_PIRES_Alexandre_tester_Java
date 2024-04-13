package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;

/**
 * The DataBasePrepareService class is responsible for clearing the entries in the database.
 * It provides a method to clear the parking and ticket entries in the database.
 */
public class DataBasePrepareService {

    /**
     * This is a logger instance used for logging messages, it is associated with the DataBasePrepareService class.
     * LogManager.getLogger(DataBasePrepareService.class) is used to initialize this logger.
     */
    private static final Logger logger = LogManager.getLogger(DataBasePrepareService.class);

    /**
     * The DataBaseTestConfig class provides methods for managing database connections and closing resources specifically during the execution of integration tests.
     */
    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    /**
     * Clears the entries in the database by setting parking entries to available and clearing ticket entries.
     */
    public void clearDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();

        }catch(Exception e){
            logger.error("An error occurred while connecting to the database", e);
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
}