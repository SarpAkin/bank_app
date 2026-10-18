package com.bankapp.operation;

import com.bankapp.account.Account;
import com.bankapp.account.User;
import com.bankapp.db.Database;
import com.bankapp.exceptions.InvalidAmountException;
import com.bankapp.support.TransactionTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositTest {
    private static final double DELTA = 1e-9;

    private Database database;
    private Account account;
    private Deposit deposit;

    @BeforeEach
    void setUp() {
        database = new Database();
        account = new Account(new User("Test User", "test@example.com"), database);
        deposit = new Deposit(account);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    @DisplayName("increases balance")
    void depositIncreasesBalance() throws InvalidAmountException {
        deposit.execute(100);

        assertEquals(100.0, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("consecutive deposits accumulate")
    void consecutiveDepositsAccumulate() throws InvalidAmountException {
        deposit.execute(100);
        deposit.execute(50.25);

        assertEquals(150.25, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("accepts 0.01")
    void smallestPositiveAmountIsAccepted() {
        assertDoesNotThrow(() -> deposit.execute(0.01));
        assertEquals(0.01, account.getBalance(), DELTA);
    }

    @ParameterizedTest(name = "deposit({0}) is rejected")
    @ValueSource(doubles = {0.0, -0.01, -100.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    @DisplayName("rejects zero, negative and non-finite")
    void invalidAmountIsRejected(double amount) throws SQLException {
        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () -> deposit.execute(amount));

        assertEquals("Amount must be greater than zero.", ex.getMessage());
        assertEquals(0.0, account.getBalance(), DELTA);
        assertTrue(TransactionTable.entries(database).isEmpty());
    }

    @Test
    @DisplayName("stored with type 'Deposit'")
    void depositIsRecordedInDatabase() throws Exception {
        deposit.execute(75);

        assertIterableEquals(List.of("Deposit:75.0"), TransactionTable.entries(database));
    }
}