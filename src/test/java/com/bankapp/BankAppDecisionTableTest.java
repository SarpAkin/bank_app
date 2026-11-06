package com.bankapp;

import com.bankapp.account.Account;
import com.bankapp.account.User;
import com.bankapp.db.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Decision table of BankApp.runMenu")
public class BankAppDecisionTableTest {
    private static final double DELTA = 1e-9;
    private static final String MENU_TITLE = "Welcome, Test User!";

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

    private String run(String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        BankApp.runMenu(new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8), account);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String part) {
        return text.split(Pattern.quote(part), -1).length - 1;
    }

    @ParameterizedTest(name = "{0}: start balance {1}, input \"{2}\"")
    @CsvSource({
            "R1, 0,   '1 250 4', 'Deposit of 250.0 successful.',                      false, 250, 2",
            "R2, 0,   '1 -10 4', 'Invalid amount: Amount must be greater than zero.', false, 0,   2",
            "R3, 100, '2 40 4',  'Enter the amount to withdraw:',                     false, 60,  2",
            "R4, 100, '2 150 4', 'Error: Insufficient balance.',                      true,  100, 2",
            "R5, 100, '2 0 4',   'Error: Amount must be greater than zero.',          true,  100, 2",
            "R6, 0,   '3 4',     '********** Current Balance ********** : 0.0',       false, 0,   2",
            "R7, 0,   '4',       'Exiting the application. Goodbye!',                 false, 0,   1",
            "R8, 0,   '9 4',     'Invalid option, please try again.',                 false, 0,   2"
    })
    @DisplayName("each rule of the decision table produces its actions")
    void rule(String rule, double startBalance, String input, String expectedText,
              boolean errorShown, double expectedBalance, int menuShown) {
        account.setBalance(startBalance);

        String output = run(input);

        assertTrue(output.contains(expectedText), rule + " should print: " + expectedText);
        assertEquals(errorShown, output.contains("Error:"), rule + " error message");
        assertEquals(expectedBalance, account.getBalance(), DELTA, rule + " final balance");
        assertEquals(menuShown, count(output, MENU_TITLE), rule + " number of menus shown");
    }
}
