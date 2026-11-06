package com.bankapp.suites;

import com.bankapp.BankAppDecisionTableTest;
import com.bankapp.account.AccountStatementBasisPathTest;
import com.bankapp.operation.WithdrawalBasisPathTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("HW2 Test Suite")
@SelectClasses({
        WithdrawalBasisPathTest.class,
        AccountStatementBasisPathTest.class,
        BankAppDecisionTableTest.class
})
public class Hw2TestSuite {
}
