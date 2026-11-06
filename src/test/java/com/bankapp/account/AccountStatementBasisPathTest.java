package com.bankapp.account;

import com.bankapp.db.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Basis paths of Account.printStatement, V(G) = 5")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountStatementBasisPathTest {
    private static final String HEADER = "********** Date | Operation | Amount **********";
    private static final String FOOTER = "********** Current Balance ********** : ";
    private static final String DATE = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    private Database database;
    private Account account;

    @BeforeEach
    void setUp() {
        database = new Database();
        account = new Account(new User("Test User", "test@example.com"), database);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private List<String> statement() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        account.printStatement(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8).lines().collect(Collectors.toList());
    }

    @Test
    @Order(1)
    @DisplayName("Path 1: 1-9-10, the query fails with SQLException (closed database)")
    void path1QueryFails() {
        database.close();

        List<String> lines = statement();

        assertFalse(database.isConnected());
        assertTrue(lines.isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("Path 2: 1-2-3-8-10, no transactions (loop body never runs)")
    void path2NoTransactions() {
        assertLinesMatch(List.of(
                HEADER,
                FOOTER + "0.0"
        ), statement());
    }

    @Test
    @Order(3)
    @DisplayName("Path 3: 1-2-3-4-5-3-8-10, one Deposit row")
    void path3DepositRow() {
        account.saveTransaction(100, "Deposit");

        assertLinesMatch(List.of(
                HEADER,
                DATE + " \\| Deposit \\| 100\\.0",
                FOOTER + "100.0"
        ), statement());
    }

    @Test
    @Order(4)
    @DisplayName("Path 4: 1-2-3-4-6-7-3-8-10, one Withdrawal row")
    void path4WithdrawalRow() {
        account.saveTransaction(30, "Withdrawal");

        assertLinesMatch(List.of(
                HEADER,
                DATE + " \\| Withdrawal \\| 30\\.0",
                FOOTER + "-30.0"
        ), statement());
    }

    @Test
    @Order(5)
    @DisplayName("Path 5: 1-2-3-4-6-3-8-10, one row of another type (Transfer)")
    void path5OtherTypeRow() {
        account.saveTransaction(50, "Transfer");

        assertLinesMatch(List.of(
                HEADER,
                DATE + " \\| Transfer \\| 50\\.0",
                FOOTER + "0.0"
        ), statement());
    }
}
