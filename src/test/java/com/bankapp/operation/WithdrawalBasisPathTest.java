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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Basis paths of Withdrawal.execute, V(G) = 4")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WithdrawalBasisPathTest {
    private static final double DELTA = 1e-9;
    private static final double START_BALANCE = 100.0;

    private Database database;
    private Account account;
    private Withdrawal withdrawal;

    @BeforeEach
    void setUp() {
        database = new Database();
        account = new Account(new User("Test User", "test@example.com"), database);
        account.setBalance(START_BALANCE);
        withdrawal = new Withdrawal(account);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    @Order(1)
    @DisplayName("Path 1: 1-3-7, amount is not finite (NaN)")
    void path1AmountIsNotFinite() throws SQLException {
        InvalidAmountException ex =
                assertThrows(InvalidAmountException.class, () -> withdrawal.execute(Double.NaN));

        assertEquals("Amount must be greater than zero.", ex.getMessage());
        assertEquals(START_BALANCE, account.getBalance(), DELTA);
        assertTrue(TransactionTable.entries(database).isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("Path 2: 1-2-3-7, amount is finite but not positive (-50)")
    void path2AmountIsNotPositive() throws SQLException {
        InvalidAmountException ex =
                assertThrows(InvalidAmountException.class, () -> withdrawal.execute(-50));

        assertEquals("Amount must be greater than zero.", ex.getMessage());
        assertEquals(START_BALANCE, account.getBalance(), DELTA);
        assertTrue(TransactionTable.entries(database).isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("Path 3: 1-2-4-5-7, amount is greater than the balance (150 > 100)")
    void path3AmountAboveBalance() throws SQLException {
        InsufficientBalanceException ex =
                assertThrows(InsufficientBalanceException.class, () -> withdrawal.execute(150));

        assertEquals("Insufficient balance.", ex.getMessage());
        assertEquals(START_BALANCE, account.getBalance(), DELTA);
        assertTrue(TransactionTable.entries(database).isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("Path 4: 1-2-4-6-7, valid amount within the balance (40 <= 100)")
    void path4ValidWithdrawal() throws SQLException {
        assertDoesNotThrow(() -> withdrawal.execute(40));

        assertEquals(60.0, account.getBalance(), DELTA);
        assertIterableEquals(List.of("Withdrawal:40.0"), TransactionTable.entries(database));
    }
}
