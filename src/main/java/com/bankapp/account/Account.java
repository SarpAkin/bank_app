/**
 * Represents a bank account that stores balance and transaction history.
 */
package com.bankapp.account;

import com.bankapp.db.Database;
import com.bankapp.utils.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Account {
    private double balance;
    private Database database;
    private User user;
    private static final Logger logger = LogManager.getLogger(Account.class);

    public Account(User user, Database database) {
        this.user = user;
        this.balance = 0.0;
        this.database = database;
    }

    /**
     * Saves the transaction in the database.
     *
     * @param amount The amount of the transaction.
     * @param type   The type of the transaction (Deposit or Withdrawal).
     * @return true if the transaction was saved successfully, false otherwise.
     */
    public boolean saveTransaction(double amount, String type) {
        String insertSQL = "INSERT INTO TRANSACTION (date, amount, type) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = database.getConnection().prepareStatement(insertSQL)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setDouble(2, amount);
            pstmt.setString(3, type);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Error saving transaction: " + e.getMessage());
            return false;
        }
    }


    /**
     * Prints the account statement to the standard output.
     */
    public void printStatement() {
        printStatement(System.out);
    }

    /**
     * Prints the account statement to the given output stream.
     *
     * @param out the output stream to print to
     */
    public void printStatement(PrintStream out) {
        String query = "SELECT * FROM TRANSACTION";
        double balance = 0.0;
        try (Connection conn = database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            out.println("********** Date | Operation | Amount **********");

            while (rs.next()) {
                Timestamp date = rs.getTimestamp("date");
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                out.println(DateUtils.formatDate(date) + " | " + type + " | " + amount);

                if (type.equalsIgnoreCase("deposit")) {
                    balance += amount;
                } else if (type.equalsIgnoreCase("withdrawal")) {
                    balance -= amount;
                }
            }
            out.println("********** Current Balance ********** : " + balance);

        } catch (SQLException e) {
            logger.error("Error printing statement: " + e.getMessage());
        }
    }


    // Getters and Setters
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }
}