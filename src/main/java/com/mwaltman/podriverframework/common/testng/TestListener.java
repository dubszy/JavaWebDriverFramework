package com.mwaltman.podriverframework.common.testng;

import com.mwaltman.podriverframework.common.util.Property;
import org.testng.IAttributes;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.log4testng.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic test listener that provides implementations (or ability to supply your own implementations)
 * of common TestNG test listener methods automatically called throughout test runs. Since this class
 * is a subclass of TestListenerAdapter, this also provides access to data for all tests that were run.
 *
 * @see TestListenerAdapter Accessing data for all tests run in TestListenerAdapter
 */
public class TestListener extends TestListenerAdapter {

    private static final Logger log = Logger.getLogger(TestListener.class);

    /**
     * Run before any configuration methods are invoked. Logs the start of a test suite.
     *
     * @param testContext The context for the tests (injected by TestNG)
     */
    @Override
    public void onStart(ITestContext testContext) {
        super.onStart(testContext); // Super must be called to maintain the list of tests in our superclass
        log.info("Starting " + testContext.getName());
        log.info("Loaded properties:");
        for (String aProperty : Property.getAllProperties()) {
            log.info("\t" + aProperty);
        }
    }

    /**
     * Run at the end of a test run. Logs the finish of a test run.
     *
     * @param testContext The context for the tests (injected by TestNG)
     */
    @Override
    public void onFinish(ITestContext testContext) {
        super.onFinish(testContext); // Super must be called to maintain the list of tests in our superclass
        log.info(String.format(
                "Tests Finished:\n\t\t%d successes\n\t\t%d failures\n\t\t%d failures within success percentage\n\t\t%d skipped",
                testContext.getPassedTests().size(),
                testContext.getFailedTests().size(),
                testContext.getFailedButWithinSuccessPercentageTests().size(),
                testContext.getSkippedTests().size()));
    }

    /**
     * Run on each configuration method success. Logs the success.
     *
     * @param result The result of the configuration method (injected by TestNG)
     */
    @Override
    public void onConfigurationSuccess(ITestResult result) {
        super.onConfigurationSuccess(result); // Super must be called to maintain the list of tests in our superclass
        log.info("Configuration " + result.getName() + formatTestNgParams(result) + " succeeded");
    }

    /**
     * Run on each configuration method failure. Logs the failure.
     *
     * @param result The result of the configuration method (injected by TestNG)
     */
    @Override
    public void onConfigurationFailure(ITestResult result) {
        super.onConfigurationFailure(result); // Super must be called to maintain the list of tests in our superclass
        log.info("Configuration " + result.getName() + formatTestNgParams(result) + " failed");
    }

    /**
     * Run on each configuration method skipped. Logs the skip.
     *
     * @param result The result of the configuration method (injected by TestNG)
     */
    @Override
    public void onConfigurationSkip(ITestResult result) {
        super.onConfigurationSkip(result); // Super must be called to maintain the list of tests in our superclass
        log.info("Configuration " + result.getName() + formatTestNgParams(result) + " skipped");
    }

    /**
     * Run on each test start. Logs the start of a test.
     *
     * @param result The result of the test (injected by TestNG)
     */
    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result); // Super must be called to maintain the list of tests in our superclass
        log.info(result.getName() + formatTestNgParams(result) + " started");
    }

    /**
     * Run on each test success. Logs the success.
     *
     * @param result The result of the test (injected by TestNG)
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        super.onTestSuccess(result); // Super must be called to maintain the list of tests in our superclass
        log.info(result.getName() + formatTestNgParams(result) + " succeeded");

        // TODO: Optionally may want to display a system-level notification on the client
    }

    /**
     * Run on each test failure. Logs the failure and the reason.
     *
     * @param result The result of the test (injected by TestNG)
     */
    @SuppressWarnings("ThrowableNotThrown")
    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result); // Super must be called to maintain the list of tests in our superclass

        String reason;
        Throwable throwable = result.getThrowable();

        if (throwable == null) { // If the throwable was null
            reason = "Unknown reason (no exception was thrown)";
        } else { // Throwable was not null, attempt to get the throwable's message
            String throwableMessage = throwable.getMessage();
            if (throwableMessage == null || throwableMessage.equals("null")) { // If the message was null
                reason = "Unknown reason (The assert keyword was used, or no reason was given in the assertion method call)";
            } else { // Message was not null, assign 'reason' to 'throwableMessage'
                if (throwableMessage.contains("\n")) {
                    // Drop all lines from the message after the first one
                    reason = throwableMessage.split("\\n")[0];
                } else {
                    // Simple assignment if it's a single-line message
                    reason = throwableMessage;
                }
            }
        }

        // Log the failure
        log.error(result.getName() + formatTestNgParams(result) + " failed: " + reason);

        // TODO: Optionally may want to display a system-level notification on the client
    }

    /**
     * Run after each test failure within success percentage. Logs the failure.
     *
     * @param result The result of the test (injected by TestNG)
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        super.onTestFailedButWithinSuccessPercentage(result); // Super must be called to maintain the list of tests in our superclass
        log.info(result.getName() + formatTestNgParams(result) + " failed within acceptable success percentage");

        // TODO: May want to log the percentage and acceptable limits, along with the reason for failure (see onTestFailure)

        // TODO: Optionally may want to display a system-level notification on the client
    }

    /**
     * Run after each test skipped. Logs the skip.
     *
     * @param result The result of the test (injected by TestNG)
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        super.onTestSkipped(result); // Super must be called to maintain the list of tests in our superclass
        log.info(result.getName() + formatTestNgParams(result) + " skipped");

        // TODO: Optionally may want to display a system-level notification on the client
    }



    /**
     * Format TestNG parameters contained in an ITestResult to a human-readable string.
     * This method simply formats parameters, it does not print or log anything.
     *
     * @param result The ITestResult to extract the parameters from
     *
     * @return A human-readable list of parameters as a String in the format "[type : name, type : name, etc]"
     */
    private static String formatTestNgParams(ITestResult result) {

        Map<String, String> params = new HashMap<>();

        StringBuilder formattedParams = new StringBuilder();

        for (Object aParam : result.getParameters()) {
            if (!(aParam instanceof IAttributes)) {
                formattedParams
                        .append(((formattedParams.length() == 0) ? "[" : ", "))
                        .append(aParam.getClass().getSimpleName())
                        .append(" : ")
                        .append(aParam.toString());
            }
        }

        if (formattedParams.length() > 0) {
            formattedParams.append("]");
        }

        return formattedParams.toString();
    }
}
