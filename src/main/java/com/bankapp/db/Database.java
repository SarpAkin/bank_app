package com.bankapp.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Class to manage the in-memory database using H2.
 *
 * <p>Each instance owns its own isolated in-memory database. The default
 * constructor uses a unique database name so that every instance starts
 * from a clean state, which makes tests independent of each other.</p>
 */
public class Database {
    private static final Logger logger = LogManager.getLogger(Database.class);
    private static final String DEFAULT_URL_PREFIX = "jdbc:h2:mem:bankappdb";
    private static final String DB_CLOSE_DELAY_SUFFIX = ";DB_CLOSE_DELAY=-1";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private Connection connection;

    public Database() {
        this(DEFAULT_URL_PREFIX + "_" + UUID.randomUUID().toString().replace("-", "") + DB_CLOSE_DELAY_SUFFIX);
    }

    /**
     * Creates a database connected to the given JDBC URL. Two instances pointed
     * at the same URL share the same in-memory database.
     *
     * @param jdbcUrl the JDBC URL to connect to
     */
    public Database(String jdbcUrl) {
        try {
            connection = DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD);
            createTransactionTable();
        } catch (SQLException e) {
            logger.error("Error connecting to the database: " + e.getMessage());
        }
    }

    /**
     * Create the TRANSACTION table in the in-memory database.
     */
    private void createTransactionTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS TRANSACTION ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "date TIMESTAMP, "
                + "type VARCHAR(255), "
                + "amount DOUBLE, "
                + "balance DOUBLE"
                + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Transaction table created successfully.");
        } catch (SQLException e) {
            logger.error("Error creating the transaction table: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Checks whether the connection is open and usable.
     *
     * @return true if the connection is open, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Error checking database connection: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether a table with the given name exists in the database.
     *
     * @param tableName the name of the table to look for
     * @return true if the table exists, false otherwise
     */
    public boolean isTableCreated(String tableName) {
        if (!isConnected()) {
            return false;
        }
        try {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking table: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the underlying connection and releases resources.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection: " + e.getMessage());
            }
        }
    }
}