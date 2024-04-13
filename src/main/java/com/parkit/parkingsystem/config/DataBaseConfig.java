package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * The DataBaseConfig class provides methods for managing database connections and closing resources.
 */
public class DataBaseConfig {

    // The logger instance for logging database operations
    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    /** * Retrieves a connection to the database. * * @return a Connection object representing a
     *  connection to the database * @throws ClassNotFoundException if the MySQL JDBC driver
     *  is not found * @throws SQLException if an error occurs while establishing the connection
     *  */
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/prod","root","rootroot");
    }

    /**
     * Closes the given database connection.
     *
     * @param con the Connection object to be closed
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
     * Closes the given PreparedStatement.
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
     * Closes the given ResultSet object, releasing any resources associated with it.
     * If an error occurs while closing the ResultSet, an error message is logged.
     *
     * @param rs the ResultSet object to be closed
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
