package com.bankapp.account;

import com.bankapp.db.Database;
import com.bankapp.support.TransactionTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountTest {
    private static final double DELTA = 1e-9;
    private static final String HEADER = "********** Date | Operation | Amount **********";
    private static final String FOOTER = "********** Current Balance ********** : ";
    private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    private Database database;
    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        database = new Database();
        user = new User("Test User", "test@example.com");
        account = new Account(user, database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private List<String> statementLines() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        account.printStatement(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8).lines().collect(Collectors.toList());
    }

    private String lastStatementLine() {
        List<String> lines = statementLines();
        return lines.get(lines.size() - 1);
    }

    @Test
    @DisplayName("new account starts at 0")
    void newAccountState() {
        assertAll(
                () -> assertEquals(0.0, account.getBalance(), DELTA),
                () -> assertSame(user, account.getUser())
        );
    }

    @Test
    @DisplayName("setBalance overwrites")
    void setBalanceOverwrites() {
        account.setBalance(100);
        account.setBalance(42.5);

        assertEquals(42.5, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("saveTransaction stores the row")
    void saveTransactionStoresRow() throws SQLException {
        assertTrue(account.saveTransaction(100, "Deposit"));
        assertIterableEquals(List.of("Deposit:100.0"), TransactionTable.entries(database));
    }

    @Test
    @DisplayName("saveTransaction stamps current time")
    void saveTransactionStoresCurrentTime() throws SQLException {
        account.saveTransaction(100, "Deposit");

        Timestamp stored;
        try (Statement stmt = database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT date FROM TRANSACTION")) {
            rs.next();
            stored = rs.getTimestamp("date");
        }

        assertNotNull(stored);
        assertTrue(Duration.between(stored.toLocalDateTime(), LocalDateTime.now()).abs().getSeconds() < 5);
    }

    @Test
    @DisplayName("saveTransaction leaves balance unchanged")
    void saveTransactionDoesNotChangeBalance() {
        account.saveTransaction(100, "Deposit");

        assertEquals(0.0, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("saveTransaction fails when closed")
    void saveTransactionFailsOnClosedDatabase() {
        database.close();

        assertFalse(account.saveTransaction(10, "Deposit"));
    }

    @Test
    @DisplayName("empty statement")
    void emptyStatement() {
        assertLinesMatch(List.of(HEADER, FOOTER + "0.0"), statementLines());
    }

    @Test
    @DisplayName("lists transactions and computes balance")
    void statementListsTransactions() {
        account.saveTransaction(100, "Deposit");
        account.saveTransaction(30, "Withdrawal");

        assertLinesMatch(List.of(
                HEADER,
                DATE_REGEX + " \\| Deposit \\| 100\\.0",
                DATE_REGEX + " \\| Withdrawal \\| 30\\.0",
                FOOTER + "70.0"
        ), statementLines());
    }

    @ParameterizedTest(name = "type \"{0}\" -> statement balance {1}")
    @CsvSource({
            "DEPOSIT,    50.0",
            "withdrawal, -50.0",
            "Transfer,   0.0"
    })
    @DisplayName("balance depends on type")
    void statementBalanceDependsOnType(String type, String expectedBalance) {
        account.saveTransaction(50, type);

        List<String> lines = statementLines();

        assertTrue(lines.get(1).endsWith("| " + type + " | 50.0"));
        assertEquals(FOOTER + expectedBalance, lines.get(lines.size() - 1));
    }

    @Test
    @DisplayName("balance comes from history")
    void statementIgnoresBalanceField() {
        account.setBalance(999);

        assertEquals(FOOTER + "0.0", lastStatementLine());
    }

    @Test
    @DisplayName("printing keeps database usable")
    void printStatementKeepsConnectionOpen() {
        statementLines();

        assertTrue(database.isConnected());
        assertTrue(account.saveTransaction(10, "Deposit"));
    }

    @Test
    @DisplayName("prints to System.out")
    void printStatementWritesToSystemOut() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

            assertDoesNotThrow(() -> account.printStatement());
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(buffer.toString(StandardCharsets.UTF_8).startsWith(HEADER));
    }
}