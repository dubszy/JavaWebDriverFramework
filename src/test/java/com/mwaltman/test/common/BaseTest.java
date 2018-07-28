package com.mwaltman.test.common;

import com.mwaltman.podriverframework.common.testng.TestListener;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.log4testng.Logger;

/**
 * Base test that all tests utilizing this framework should extend.
 * Provides access to implementations (or ability to supply your own implementations)
 * of common TestNG methods automatically called throughout test runs.
 *
 * @see TestListener For implementations of {@link org.testng.TestListenerAdapter} methods
 */
@Listeners(TestListener.class)
public class BaseTest {

    /**
     * Default logger for all tests that extend this class.
     * Declared as protected so inheritors can use this logger.
     * Declared as final because this logger should not be reassigned
     * as it could potentially be used by multiple threads.
     */
    protected final Logger log = Logger.getLogger(this.getClass());

    /*
     * Declare any common methods that all tests or suites should share here.
     *
     * Keep in mind that these methods can be used by multiple threads, and
     * therefore should be declared synchronized and must be side-effect free.
     *
     * You can use the following TestNG annotations attached to methods in this
     * class to have certain methods run before or after certain events. These
     * annotations are located in the org.testng.annotations package.
     *
     * In the following list:
     * - 'test' refers to any class or method annotated with @org.testng.annotations.Test
     * - 'suite' refers to suites defined by the suiteName parameter of @Test
     * - 'groups' refers to groups defined by the groups parameter of @Test
     * - 'class invoked by TestNG' refers to any class annotated with @Test or any class
     *      who has a method annotated with @Test
     *
     * @BeforeSuite   - run before every suite invoked by TestNG
     * @BeforeGroups  - run before specified groups invoked by TestNG (specified by the groups parameter)
     * @BeforeClass   - run before every class invoked by TestNG
     * @BeforeTest    - run before every test invoked by TestNG
     * @BeforeMethod  - run after every method invoked by TestNG
     * @AfterSuite    - run after every TestNG suite
     * @AfterGroups   - run after specified groups invoked by TestNG (specified by the groups parameter)
     * @AfterClass    - run after every class invoked by TestNG
     * @AfterTest     - run after every test invoked by TestNG
     * @AfterMethod   - run after every method invoked by TestNG
     */

    /**
     * Example method for @AfterMethod annotation.
     *
     * @param testResult Holds the result of the most recently run test method (injected by TestNG)
     */
    @AfterMethod
    public synchronized void printTestResult(ITestResult testResult) {
        // Normally, we would use testResult.toString(), but that only prints
        // the status of the test, we want to print some more info.

        String host = testResult.getHost();

        String testInfo = String.format("%s finished    status: %s    ran on host: %s",
                testResult.getName(),
                getTestStatusFriendlyName(testResult.getStatus()),
                ((host != null) ? host : "localhost")); // If run locally, testResult.getHost() is null

        log.info(testInfo);
    }


    /**
     * Get a human-readable string from an ITestResult.getStatus()
     *
     * @param status the TestNG test status as an integer
     *
     * @return A human-readable string based on the supplied {@code status}
     */
    private synchronized String getTestStatusFriendlyName(int status) {
        switch (status) {
            case 1:
                return "success";
            case 2:
                return "failure";
            case 3:
                return "skip";
            case 4:
                return "success percentage failure";
            case 16:
                return "started";
            default:
                return String.format("unknown status: %d", status);
        }
    }
}
