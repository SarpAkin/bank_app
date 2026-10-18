package com.bankapp;

import com.bankapp.account.Account;
import com.bankapp.account.User;
import com.bankapp.db.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankAppTest {
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
    private List<String> run(String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        BankApp.runMenu(new Scanner(input), out, account);
        return buffer.toString(StandardCharsets.UTF_8).lines().collect(Collectors.toList());
    }
    private String runAndJoin(String input) {
        return String.join("\n", run(input));
    }
    @Test
    @DisplayName("option 4 shows menu and exits")
    void exitOptionShowsMenuAndExits() {
        assertLinesMatch(List.of(
                "",
                "Welcome, Test User!",
                "Please select an operation:",
                "1. Deposit money",
                "2. Withdraw money",
                "3. Print account statement",
                "4. Exit",
                "Exiting the application. Goodbye!"
        ), run("4"));
    }
    @Test
    @DisplayName("option 1 deposits")
    void depositThroughMenu() {
        String output = runAndJoin("1\n250\n4");
        assertTrue(output.contains("Enter the amount to deposit: Deposit of 250.0 successful."));
        assertEquals(250.0, account.getBalance(), DELTA);
    }
    @Test
    @DisplayName("NaN deposit is reported")
    void nanDepositIsReported() {
        String output = runAndJoin("1\nNaN\n4");
        assertTrue(output.contains("Invalid amount: Amount must be greater than zero."));
        assertEquals(0.0, account.getBalance(), DELTA);
    }
    @Test
    @DisplayName("option 2 withdraws")
    void withdrawalThroughMenu() {
        String output = runAndJoin("1\n100\n2\n40\n4");
        assertEquals(60.0, account.getBalance(), DELTA);
        assertFalse(output.contains("Error:"));
    }
    @Test
    @DisplayName("insufficient balance is reported")
    void insufficientWithdrawalIsReported() {
        assertTrue(runAndJoin("2\n100\n4").contains("Error: Insufficient balance."));
    }
    @Test
    @DisplayName("unknown option shows warning")
    void unknownOptionShowsWarning() {
        List<String> lines = run("9\n4");
        assertTrue(lines.contains("Invalid option, please try again."));
        assertEquals(2, Collections.frequency(lines, "Welcome, Test User!"));
    }
    @Test
    @DisplayName("statement works after repeated use")
    void statementWorksAfterRepeatedUse() {
        List<String> footers = run("1\n100\n3\n1\n50\n3\n4").stream()
                .filter(line -> line.contains("Current Balance"))
                .collect(Collectors.toList());
        assertIterableEquals(List.of(
                "********** Current Balance ********** : 100.0",
                "********** Current Balance ********** : 150.0"
        ), footers);
    }
    @Test
    @DisplayName("bad input is not handled")
    void menuDoesNotHandleBadInput() {
        assertAll(
                () -> assertThrows(InputMismatchException.class, () -> run("abc")),
                () -> assertThrows(NoSuchElementException.class, () -> run(""))
        );
    }
}