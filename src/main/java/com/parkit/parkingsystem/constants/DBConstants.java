package com.parkit.parkingsystem.constants;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class DBConstants {
    public static final int NUMBER_OF_TICKETS = 3;
    public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
    public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";
    //sorted by most recent entry date
    public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? order by t.IN_TIME DESC limit 1";
    public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)";
    public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";

    public static final Date[] IN_TIME_TEST = new Date[3]; // array of dates defined for integration tests
    public static final Date[] OUT_TIME_TEST = new Date[3]; // array of dates defined for integration tests

    static {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            IN_TIME_TEST[0] = dateFormat.parse("2024-04-08 12:00:00.0");
            IN_TIME_TEST[1] = dateFormat.parse("2024-04-09 12:00:00.0");
            IN_TIME_TEST[2] = dateFormat.parse("2024-04-10 12:00:00.0");

            OUT_TIME_TEST[0] = dateFormat.parse("2024-04-08 16:00:00.0");
            OUT_TIME_TEST[1] = dateFormat.parse("2024-04-09 16:00:00.0");
            OUT_TIME_TEST[2] = dateFormat.parse("2024-04-10 16:00:00.0");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}