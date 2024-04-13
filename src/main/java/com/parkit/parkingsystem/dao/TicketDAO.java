package com.parkit.parkingsystem.dao;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

/**
 * The TicketDAO class provides methods to interact with the database for ticket-related operations.
 */
public class TicketDAO {

    /**
     * Logger for the TicketDAO class.
     */
    private static final Logger logger = LogManager.getLogger("TicketDAO");

    /**
     * The DataBaseConfig class provides methods for configuring and managing the database connection.
     */
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    //Rewritten version with ps.executeUpdate() instead of ps.execute()

    /**
     * Saves a ticket in the database.
     *
     * @param ticket The ticket to be saved.
     * @return true if the ticket is successfully saved, false otherwise.
     */
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = dataBaseConfig.getConnection();

            if (con == null) {
                System.out.println("Failed to establish database connection");
                return false;
            }

            ps = con.prepareStatement(DBConstants.SAVE_TICKET);

            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));

            if (ticket.getOutTime() != null) {
                ps.setTimestamp(5, new Timestamp(ticket.getOutTime().getTime()));
            } else {
                ps.setTimestamp(5, null);
            }

            int updateCount = ps.executeUpdate();

            if (updateCount == 0) {
                System.out.println("Failed to save ticket in database");
                return false;
            }

            return true;

        } catch (Exception ex) {
            // Handle exception
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                dataBaseConfig.closeConnection(con);
            } catch (SQLException e) {
                // Handle exception
            }
        }

        return false;
    }

    //Rewritten version with ps.executeUpdate() instead of ps.execute()

    /**
     * Updates a parking ticket in the database.
     *
     * @param ticket The ticket to be updated.
     * @return True if the ticket is successfully updated, false otherwise.
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        boolean updateResult = false;

        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                updateResult = true;
            }
        } catch (Exception ex) {
            logger.error("Error saving ticket info", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }

        return updateResult;
    }

    /**
     * Retrieves the ticket associated with the given vehicle registration number.
     *
     * @param vehicleRegNumber the vehicle registration number
     * @return the ticket associated with the given vehicle registration number, or null if no ticket is found
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

    /**
     * Returns the number of tickets associated with the given vehicle registration number.
     *
     * @param vehicleRegNumber the vehicle registration number
     * @return the number of tickets associated with the given vehicle registration number
     */
    public int getNbTicket(String vehicleRegNumber) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int nbTicket = 0;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement("SELECT COUNT(*) FROM ticket WHERE VEHICLE_REG_NUMBER = ?");
            ps.setString(1, vehicleRegNumber);
            rs = ps.executeQuery();

            if (rs.next()) {
                nbTicket = rs.getInt(1);
            }
        } catch (Exception ex) {
            logger.error("Error occurred", ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }

        return nbTicket;
    }

}
