package com.parkit.parkingsystem.constants;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * A class representing test constants.
 */
public class TestConstants {
    /**
     * The number of tickets in the test suite.
     */
    public static final int NUMBER_OF_TICKETS = 3;

    /**
     * The check-in times for the tickets in the test suite.
     */
    public static final Date[] IN_TIME_TEST = new Date[NUMBER_OF_TICKETS];

    /**
     * The check-out times for the tickets in the test suite.
     */
    public static final Date[] OUT_TIME_TEST = new Date[NUMBER_OF_TICKETS];

    static {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            for (int i = 0; i < NUMBER_OF_TICKETS; i++) {
                IN_TIME_TEST[i] = dateFormat.parse("2024-04-0" + (i + 8) + " 12:00:00.0");
                OUT_TIME_TEST[i] = dateFormat.parse("2024-04-0" + (i + 8) + " 16:00:00.0");
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}