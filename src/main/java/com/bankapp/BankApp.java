/**
 * Main class for the bank application. Handles deposits, withdrawals, and account statements.
 */
package com.bankapp;

import com.bankapp.account.Account;
import com.bankapp.account.User;
import com.bankapp.db.Database;
import com.bankapp.exceptions.InsufficientBalanceException;
import com.bankapp.exceptions.InvalidAmountException;
import com.bankapp.operation.Deposit;
import com.bankapp.operation.Withdrawal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.util.Scanner;

public class BankApp {
    private static final Logger logger = LogManager.getLogger(BankApp.class);

    public static void main(String[] args) {
        Database database = new Database();
        User user = new User("John Doe", "john.doe@example.com");
        Account account = new Account(user, database);

        try (Scanner scanner = new Scanner(System.in)) {
            runMenu(scanner, System.out, account);
        }
    }

    /**
     * Runs the interactive menu loop. The scanner and output stream are injected
     * so the flow can be driven programmatically in tests.
     *
     * @param scanner source of user input
     * @param out     destination of all user-visible output
     * @param account the account to operate on
     */
    public static void runMenu(Scanner scanner, PrintStream out, Account account) {
        Deposit deposit = new Deposit(account);
        Withdrawal withdrawal = new Withdrawal(account);

        boolean running = true;

        while (running) {
            out.println("\nWelcome, " + account.getUser().getName() + "!");
            out.println("Please select an operation:");
            out.println("1. Deposit money");
            out.println("2. Withdraw money");
            out.println("3. Print account statement");
            out.println("4. Exit");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    out.print("Enter the amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    try {
                        deposit.execute(depositAmount);
                        out.println("Deposit of " + depositAmount + " successful.");
                    } catch (InvalidAmountException e) {
                        logger.error(e.getMessage());
                        out.println("Invalid amount: " + e.getMessage());
                    }
                    break;

                case 2:
                    out.print("Enter the amount to withdraw: ");
                    double withdrawalAmount = scanner.nextDouble();
                    try {
                        withdrawal.execute(withdrawalAmount);
                    } catch (InvalidAmountException | InsufficientBalanceException e) {
                        logger.error(e.getMessage());
                        out.println("Error: " + e.getMessage());
                    }
                    break;

                case 3:
                    account.printStatement(out);
                    break;

                case 4:
                    running = false;
                    out.println("Exiting the application. Goodbye!");
                    break;

                default:
                    out.println("Invalid option, please try again.");
            }
        }
    }
}