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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Operation interface and custom exceptions")
public class OperationTest {
    private static final double DELTA = 1e-9;

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

    @Test
    @DisplayName("operations used through the Operation interface share one account")
    void operationsWorkThroughTheInterface() throws Exception {
        List<Operation> operations = List.of(new Deposit(account), new Withdrawal(account));

        operations.get(0).execute(200);
        operations.get(1).execute(50);

        assertAll(
                () -> assertEquals(150.0, account.getBalance(), DELTA),
                () -> assertIterableEquals(List.of("Deposit:200.0", "Withdrawal:50.0"),
                        TransactionTable.entries(database))
        );
    }

    @Test
    @DisplayName("execute() declares exactly the exceptions shown in the UML diagram")
    void executeDeclaresUmlExceptions() throws NoSuchMethodException {
        assertArrayEquals(
                new Class<?>[]{InvalidAmountException.class, InsufficientBalanceException.class},
                Operation.class.getMethod("execute", double.class).getExceptionTypes());
        assertArrayEquals(
                new Class<?>[]{InvalidAmountException.class},
                Deposit.class.getMethod("execute", double.class).getExceptionTypes());
        assertArrayEquals(
                new Class<?>[]{InvalidAmountException.class, InsufficientBalanceException.class},
                Withdrawal.class.getMethod("execute", double.class).getExceptionTypes());
    }

    @Test
    @DisplayName("custom exceptions are checked exceptions, so callers must handle them")
    void customExceptionsAreChecked() {
        assertAll(
                () -> assertTrue(Exception.class.isAssignableFrom(InvalidAmountException.class)),
                () -> assertFalse(RuntimeException.class.isAssignableFrom(InvalidAmountException.class)),
                () -> assertTrue(Exception.class.isAssignableFrom(InsufficientBalanceException.class)),
                () -> assertFalse(RuntimeException.class.isAssignableFrom(InsufficientBalanceException.class))
        );
    }
}