/**
 * Represents a deposit operation on a bank account.
 */
package com.bankapp.operation;

import com.bankapp.account.Account;
import com.bankapp.exceptions.InvalidAmountException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Deposit implements Operation {
    private Account account;
    private static final Logger logger = LogManager.getLogger(Deposit.class);

    public Deposit(Account account) {
        this.account = account;
    }

    /**
     * Executes the deposit operation.
     * @param amount The amount to deposit.
     * @throws InvalidAmountException if the amount is less than or equal to zero, or is NaN/Infinity.
     */
    @Override
    public void execute(double amount) throws InvalidAmountException {
        // NaN passes "amount <= 0" (every comparison with NaN is false), so it is checked explicitly
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        account.setBalance(account.getBalance() + amount);
        account.saveTransaction(amount, "Deposit");
        logger.info("Deposit of " + amount + " successful.");
    }
}
