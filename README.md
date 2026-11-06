# Bank Account Management App

This project is a simple bank account management application built using **Java** with no external frameworks like Spring. The solution aims to manage bank account operations such as deposits, withdrawals, and generating account statements. All operations are recorded in an in-memory database, and the project includes unit tests and logging.

## Features

- **Deposit**: Allows the user to deposit money into their account.
- **Withdrawal**: Allows the user to withdraw money from their account.
- **Account Statement**: Displays a history of all transactions (date, type of operation, amount).
- **In-Memory Database**: Transaction data is stored in an H2 in-memory database.
- **Logging**: Uses Log4j for logging important events (e.g., transactions, exceptions).
- **Custom Exceptions**: Handles invalid amounts and insufficient balance scenarios.
- **Unit Tests**: Includes unit tests for deposit and withdrawal operations using JUnit 5.

## Structure

The project follows a simple structure with separation between different layers:

/src
  ├── /main
  │     ├── /java
  │     │     ├── /com
  │     │     │    ├── /bankapp
  │     │     │    │    ├── BankApp.java
  │     │     │    ├── /account
  │     │     │    │    ├── Account.java
  │     │     │    │    ├── User.java
  │     │     │    ├── /operation
  │     │     │    │    ├── Operation.java
  │     │     │    │    ├── Deposit.java
  │     │     │    │    ├── Withdrawal.java
  │     │     │    ├── /exceptions
  │     │     │    │    ├── InvalidAmountException.java
  │     │     │    │    ├── InsufficientBalanceException.java
  │     │     │    ├── /db
  │     │     │    │    ├── Database.java
  ├── /test
        ├── /java
              ├── /com
              │    ├── /bankapp
              │    ├── /operation
                    ├── DepositTest.java
                    ├── WithdrawalTest.java
/pom.xml


## How to Run the Project

1. Clone the repository:
   git clone https://github.com/Ta95/gitbank_app.git

2. Build and run the project using Maven:
mvn clean install
mvn exec:java -Dexec.mainClass="com.bankapp.BankApp"

3. To run the tests:
mvn test

## Tests (JUnit 5)

`mvn test` runs the three JUnit 5 test suites (84 tests in total):

| Suite | Test classes | Covers |
|---|---|---|
| `OperationsTestSuite` | `DepositTest`, `WithdrawalTest`, `OperationTest`, `BankAppTest` | Deposit/withdrawal rules, the Operation contract, the interactive menu |
| `CoreTestSuite` | `AccountTest`, `UserTest`, `DatabaseTest`, `DateUtilsTest` | Account and statement, User, the H2 database wrapper, date formatting |
| `Hw2TestSuite` | `WithdrawalBasisPathTest`, `AccountStatementBasisPathTest`, `BankAppDecisionTableTest` | Basis path (white-box) tests and the decision table of the menu |

Mutation testing (HW2): `mutation/mutants/M01..M10` hold ten mutants of `Withdrawal.java`.
`bash mutation/run-mutants.sh` (or `mutation\run-mutants.ps1` on Windows) runs `WithdrawalTest`
against each mutant and reports which ones are killed. Flowcharts are in `docs/hw2/flowcharts`.

Run a single suite or class:

    mvn test -Dtest=OperationsTestSuite
    mvn test -Dtest=DepositTest

Parameterized tests are in `DepositTest`, `WithdrawalTest`, `AccountTest` and `DateUtilsTest`.
`class_diagram.wsd` (rendered as `class_diagram.png`) is the UML class diagram of the application.

## Technologies Used
Java 11: Main programming language
JUnit 5: For unit testing
H2 Database: In-memory database for storing transactions
Log4j: For logging application events
Project Structure
com.bankapp: Contains the main application logic.
com.bankapp.account: Contains the Account and User classes.
com.bankapp.operation: Contains the Operation interface and its implementations (Deposit, Withdrawal).
com.bankapp.db: Contains the Database class, which manages the in-memory database connection.
com.bankapp.exceptions: Contains custom exceptions (InvalidAmountException, InsufficientBalanceException).
