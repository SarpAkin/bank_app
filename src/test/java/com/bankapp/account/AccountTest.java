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

@DisplayName("Account")
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
    @DisplayName("a new account starts with balance 0 and keeps the same User object")
    void newAccountState() {
        assertAll(
                () -> assertEquals(0.0, account.getBalance(), DELTA),
                () -> assertSame(user, account.getUser())
        );
    }

    @Test
    @DisplayName("setBalance overwrites the previous balance")
    void setBalanceOverwrites() {
        account.setBalance(100);
        account.setBalance(42.5);

        assertEquals(42.5, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("saveTransaction stores the row and reports success")
    void saveTransactionStoresRow() throws SQLException {
        assertTrue(account.saveTransaction(100, "Deposit"));
        assertIterableEquals(List.of("Deposit:100.0"), TransactionTable.entries(database));
    }

    @Test
    @DisplayName("saveTransaction stamps the row with the current time")
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
    @DisplayName("saveTransaction only records; it does not change the balance")
    void saveTransactionDoesNotChangeBalance() {
        account.saveTransaction(100, "Deposit");

        assertEquals(0.0, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("saveTransaction returns false when the database is closed")
    void saveTransactionFailsOnClosedDatabase() {
        database.close();

        assertFalse(account.saveTransaction(10, "Deposit"));
    }

    @Test
    @DisplayName("an empty statement shows only the header and a zero balance")
    void emptyStatement() {
        assertLinesMatch(List.of(HEADER, FOOTER + "0.0"), statementLines());
    }

    @Test
    @DisplayName("the statement lists each transaction with its date and computes the balance")
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
    @DisplayName("the statement balance depends on the transaction type")
    void statementBalanceDependsOnType(String type, String expectedBalance) {
        account.saveTransaction(50, type);

        List<String> lines = statementLines();

        assertTrue(lines.get(1).endsWith("| " + type + " | 50.0"));
        assertEquals(FOOTER + expectedBalance, lines.get(lines.size() - 1));
    }

    @Test
    @DisplayName("the statement balance comes from the history, not from setBalance")
    void statementIgnoresBalanceField() {
        account.setBalance(999);

        assertEquals(FOOTER + "0.0", lastStatementLine());
    }

    @Test
    @DisplayName("printing the statement keeps the database usable")
    void printStatementKeepsConnectionOpen() {
        statementLines();

        assertTrue(database.isConnected());
        assertTrue(account.saveTransaction(10, "Deposit"));
    }

    @Test
    @DisplayName("printStatement() without arguments writes to System.out")
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