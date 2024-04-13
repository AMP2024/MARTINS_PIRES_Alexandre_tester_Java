package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * The DataBaseTestConfig class provides methods for managing database connections and closing resources specifically during the execution of integration tests..
 */
public class DataBaseTestConfig extends DataBaseConfig {

    /**
     * A logger instance named "DataBaseTestConfig". Used for logging information and errors about
     * activities specific to database tests configuration, including the state of connections,
     * statements, and result sets.
     */
    private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");

    /**
     * Retrieves a database connection.
     *
     * @return the database connection
     * @throws ClassNotFoundException if the MySQL JDBC driver is not found
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/test","root","rootroot");
    }

    /**
     * Closes the given database connection and releases any associated resources.
     * If the connection is not null, the method calls the close() method of the connection to release the resources.
     *
     * @param con the database connection to be closed
     */
    public void closeConnection(Connection con){
        if(con!=null){
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection",e);
            }
        }
    }

    /**
     * Closes the given PreparedStatement and releases any associated resources.
     * If the PreparedStatement is not null, the method calls the close() method of the PreparedStatement to release the resources.
     *
     * @param ps the PreparedStatement to be closed
     */
    public void closePreparedStatement(PreparedStatement ps) {
        if(ps!=null){
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement",e);
            }
        }
    }

    /**
     * Closes the given ResultSet and releases any associated resources.
     * If the ResultSet is not null, the method calls the close() method of the ResultSet to release the resources.
     *
     * @param rs the ResultSet to be closed
     */
    public void closeResultSet(ResultSet rs) {
        if(rs!=null){
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set",e);
            }
        }
    }
}
