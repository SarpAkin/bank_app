package com.bankapp.operation;

import com.bankapp.account.Account;
import com.bankapp.account.User;
import com.bankapp.db.Database;
import com.bankapp.exceptions.InsufficientBalanceException;
import com.bankapp.exceptions.InvalidAmountException;
import com.bankapp.support.TransactionTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Withdrawal")
public class WithdrawalTest {
    private static final double DELTA = 1e-9;

    private Database database;
    private Account account;
    private Withdrawal withdrawal;

    @BeforeEach
    void setUp() {
        database = new Database();
        account = new Account(new User("Test User", "test@example.com"), database);
        withdrawal = new Withdrawal(account);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @ParameterizedTest(name = "balance {0} - withdraw {1} = {2}")
    @CsvSource({
            "100.0, 30.0,  70.0",
            "100.0, 100.0, 0.0",
            "100.0, 0.01,  99.99"
    })
    @DisplayName("a valid withdrawal reduces the balance")
    void validWithdrawalReducesBalance(double initial, double amount, double expected) {
        account.setBalance(initial);

        assertDoesNotThrow(() -> withdrawal.execute(amount));
        assertEquals(expected, account.getBalance(), DELTA);
    }

    @ParameterizedTest(name = "balance {0} - withdraw {1} is rejected")
    @CsvSource({
            "0.0,   1.0",
            "50.0,  50.01",
            "100.0, 1000.0"
    })
    @DisplayName("withdrawing more than the balance is rejected without side effects")
    void withdrawalAboveBalanceIsRejected(double initial, double amount) throws SQLException {
        account.setBalance(initial);

        InsufficientBalanceException ex =
                assertThrows(InsufficientBalanceException.class, () -> withdrawal.execute(amount));

        assertEquals("Insufficient balance.", ex.getMessage());
        assertEquals(initial, account.getBalance(), DELTA);
        assertTrue(TransactionTable.entries(database).isEmpty());
    }

    @ParameterizedTest(name = "withdraw({0}) is an invalid amount")
    @ValueSource(doubles = {0.0, -50.0, Double.NaN, Double.POSITIVE_INFINITY})
    @DisplayName("zero, negative and non-finite amounts are rejected as invalid")
    void invalidAmountIsRejected(double amount) {
        account.setBalance(100);

        assertThrows(InvalidAmountException.class, () -> withdrawal.execute(amount));
        assertEquals(100.0, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("the balance check uses the balance left by earlier withdrawals")
    void balanceCheckUsesUpdatedBalance() throws Exception {
        account.setBalance(100);
        withdrawal.execute(60);

        assertThrows(InsufficientBalanceException.class, () -> withdrawal.execute(50));
        assertEquals(40.0, account.getBalance(), DELTA);
    }

    @Test
    @DisplayName("a withdrawal is stored in the database with type 'Withdrawal'")
    void withdrawalIsRecordedInDatabase() throws Exception {
        account.setBalance(100);
        withdrawal.execute(40);

        assertIterableEquals(List.of("Withdrawal:40.0"), TransactionTable.entries(database));
    }
}