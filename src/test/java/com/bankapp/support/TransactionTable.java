package com.bankapp.support;

import com.bankapp.db.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public final class TransactionTable {

    private TransactionTable() {
    }

    public static List<String> entries(Database database) throws SQLException {
        List<String> result = new ArrayList<>();
        try (Statement stmt = database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT type, amount FROM TRANSACTION ORDER BY id")) {
            while (rs.next()) {
                result.add(rs.getString("type") + ":" + rs.getDouble("amount"));
            }
        }
        return result;
    }


    public static void insert(Database database, String type, double amount) throws SQLException {
        String sql = "INSERT INTO TRANSACTION (date, type, amount) VALUES (CURRENT_TIMESTAMP, ?, ?)";
        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
        }
    }
}