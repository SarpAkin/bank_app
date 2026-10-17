package com.bankapp.suites;

import com.bankapp.account.AccountTest;
import com.bankapp.account.UserTest;
import com.bankapp.db.DatabaseTest;
import com.bankapp.utils.DateUtilsTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Core Test Suite")
@SelectClasses({
        AccountTest.class,
        UserTest.class,
        DatabaseTest.class,
        DateUtilsTest.class
})
public class CoreTestSuite {
}