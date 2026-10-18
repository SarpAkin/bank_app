package com.bankapp.db;

import com.bankapp.support.TransactionTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseTest {

    private Database database;

    @BeforeEach
    void setUp() {
        database = new Database();
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private static String uniqueSharedUrl() {
        return "jdbc:h2:mem:shared_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1";
    }

    @Test
    @DisplayName("new database is connected")
    void newDatabaseIsConnected() {
        assertNotNull(database.getConnection());
        assertTrue(database.isConnected());
    }

    @Test
    @DisplayName("creates TRANSACTION table")
    void transactionTableIsCreated() {
        assertTrue(database.isTableCreated("TRANSACTION"));
    }

    @Test
    @DisplayName("case-insensitive lookup (Turkish locale)")
    void tableLookupWorksInTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertTrue(database.isTableCreated("transaction"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    @DisplayName("unknown table is not found")
    void unknownTableIsNotFound() {
        assertFalse(database.isTableCreated("ACCOUNTS"));
    }

    @Test
    @DisplayName("expected columns in order")
    void transactionTableHasExpectedColumns() throws SQLException {
        List<String> columns = new ArrayList<>();
        try (ResultSet rs = database.getConnection().getMetaData()
                .getColumns(null, "PUBLIC", "TRANSACTION", null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }

        assertIterableEquals(List.of("ID", "DATE", "TYPE", "AMOUNT", "BALANCE"), columns);
    }

    @Test
    @DisplayName("default databases are isolated")
    void defaultDatabasesAreIsolated() throws SQLException {
        Database other = new Database();
        try {
            TransactionTable.insert(database, "Deposit", 10.0);

            assertEquals(1, TransactionTable.entries(database).size());
            assertEquals(0, TransactionTable.entries(other).size());
        } finally {
            other.close();
        }
    }

    @Test
    @DisplayName("same URL shares data")
    void sameUrlSharesData() throws SQLException {
        String url = uniqueSharedUrl();
        Database first = new Database(url);
        Database second = null;
        try {
            TransactionTable.insert(first, "Deposit", 10.0);
            second = new Database(url);

            assertIterableEquals(List.of("Deposit:10.0"), TransactionTable.entries(second));
        } finally {
            first.close();
            if (second != null) {
                second.close();
            }
        }
    }

    @Test
    @DisplayName("close disconnects")
    void closeDisconnects() {
        database.close();

        assertFalse(database.isConnected());
    }

    @Test
    @DisplayName("invalid URL leaves disconnected")
    void invalidUrlLeavesDatabaseDisconnected() {
        Database broken = new Database("jdbc:invalid:url");

        assertNull(broken.getConnection());
        assertFalse(broken.isConnected());
        assertFalse(broken.isTableCreated("TRANSACTION"));
        assertDoesNotThrow(broken::close);
    }
}