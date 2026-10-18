package com.bankapp.suites;

import com.bankapp.BankAppTest;
import com.bankapp.operation.DepositTest;
import com.bankapp.operation.OperationTest;
import com.bankapp.operation.WithdrawalTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Operations Test Suite")
@SelectClasses({
        DepositTest.class,
        WithdrawalTest.class,
        OperationTest.class,
        BankAppTest.class
})
public class OperationsTestSuite {
}