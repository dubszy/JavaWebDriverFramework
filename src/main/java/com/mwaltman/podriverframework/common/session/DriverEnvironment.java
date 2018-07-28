package com.mwaltman.podriverframework.common.session;

import com.mwaltman.podriverframework.common.util.Property;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.log4testng.Logger;

import java.util.concurrent.TimeUnit;

public class DriverEnvironment implements AutoCloseable {

    private static Logger log = Logger.getLogger(DriverEnvironment.class);

    private boolean isClosed;

    /*
     * Transient because if we want to serialize this DriverEnvironment and pick
     * it back up later, we want to start with a fresh WebDriver instance
     */
    private transient WebDriver driver;

    /*
     * Transient because if we want to serialize this DriverEnvironment and pick
     * it back up later, we'll be starting with a fresh WebDriver instance, which
     * is lazily started, so this field needs to be false upon deserialization
     */
    private transient boolean isStarted = false;

    private Browser browser;

    private String proxy = "";

    DriverEnvironment(Browser browser, String proxy) {
        this.browser = browser;
        this.proxy = proxy;
        this.driver = null;
    }

    public static DriverEnvironment defaultDriverEnvironment() {
        String driverProfile = Property.SELENIUM_DRIVER_BROWSER.getPropertyValue();
        String proxy = Property.PROXY_SERVER.getPropertyValue();

        if (driverProfile == null || driverProfile.isEmpty()) {
            throw new IllegalStateException("The property '" + Property.SELENIUM_DRIVER_BROWSER.getPropertyName() +
                    "' is not defined");
        }
        return new DriverEnvironment(Browser.identify(driverProfile), proxy);
    }

    /**
     * Get whether this DriverEnvironment is closed.
     *
     * @return {@code true} if this DriverEnvironment is closed, {@code false} otherwise
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Get the {@link WebDriver} associated with this DriverEnvironment.
     *
     * @return The {@link WebDriver} associated with this DriverEnvironment
     *
     * @exception IllegalStateException If this DriverEnvironment is closed
     * @exception NullPointerException If the {@link WebDriver} associated with this DriverEnvironment is null
     */
    public WebDriver getDriver() {
        if (isClosed()) {
            throw new IllegalStateException("The DriverEnvironment has already been closed");
        } else if (_getDriver() == null) {
            throw new NullPointerException("WebDriver is null for this session. Is the session closed? Are there no browser windows open?");
        }
        return _getDriver();
    }

    /**
     * Get the {@link Browser} associated with this DriverEnvironment.
     *
     * @return The {@link Browser} associated with this DriverEnvironment
     *
     * @exception IllegalStateException If this DriverEnvironment is closed
     * @exception NullPointerException If the {@link Browser} associated with this DriverEnvironment is null
     */
    public Browser getBrowser() {
        if (isClosed()) {
            throw new IllegalStateException("The DriverEnvironment has already been closed");
        } else if (browser == null) {
            throw new NullPointerException("Browser is null for this session. Is the Session closed?");
        }
        return browser;
    }

    /**
     * Get the proxy server associated with this DriverEnvironment.
     *
     * @return The proxy server associated with this DriverEnvironment
     *
     * @exception IllegalStateException If this DriverEnvironment is closed
     */
    public String getProxy() {
        if (isClosed()) {
            throw new IllegalStateException("The DriverEnvironment has already been closed");
        }
        return proxy;
    }

    /**
     * Open a browser window.
     */
    public void openBrowser() {
        if (isClosed()) {
            throw new IllegalStateException("The DriverEnvironment has already been closed");
        } else if (browser == null) {
            throw new NullPointerException("Browser is null for this session. Is the Session closed?");
        }

        if (!isStarted()) {
            _getDriver();
            return;
        }

        if (_getDriver() != null) {
            throw new WebDriverException("The WebDriver instance is not closed, is the browser already open?");
        }

        // If we hit this point, just try kicking WebDriver directly
        _getDriver();
    }

    /**
     * Close the browser window.
     */
    public void closeBrowser() {
        if (isClosed()) {
            throw new IllegalStateException("The DriverEnvironment has already been closed");
        }
        getDriver().quit();
        driver = null;
    }

    /**
     * Get whether the browser is currently open.
     *
     * @return {@code true} if the driver is not null, {@code false} otherwise
     */
    public boolean isBrowserOpen() {
        return driver != null;
    }

    public DriverEnvironment refresh() {
        getDriver().navigate().refresh();
        return this;
    }

    public DriverEnvironment back() {
        getDriver().navigate().back();
        return this;
    }

    public DriverEnvironment forward() {
        getDriver().navigate().forward();
        return this;
    }

    /**
     * Navigate to a supplied URL. This should be used instead of {@link WebDriver#get(String)}
     * because we can drop the attempted URL into the exception thrown if something goes wrong.
     *
     * @param url The URL to navigate to
     *
     * @exception WebDriverException If the URL could not be navigated to
     */
    public void goToUrl(String url) {
        try {
            getDriver().get(url);
        } catch (WebDriverException e) {
            e.addInfo("Attempted URL", url);
            throw e;
        }
    }

    /**
     * Execute JavaScript on the current page. Arguments can be passed to the script using 'arguments[n]'
     * where 'n' denotes the index in the array of args being passed.
     *
     * @param js The script to be executed
     * @param async Whether the script should execute asynchronously
     * @param args An array of arguments to be interpolated into the script
     *
     * @return The result of the executed JavaScript
     */
    public Object executeJS(String js, boolean async, Object... args) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) getDriver();
        return (async) ? jsExecutor.executeAsyncScript(js, args) : jsExecutor.executeScript(js, args);
    }

    /**
     * Close this DriverEnvironment.
     */
    @Override
    public void close() {
        isClosed = true;

        if (driver != null) {
            driver.quit();
        }

        driver = null;
        browser = null;
    }

    /**
     * Get the {@link WebDriver} associated with this DriverEnvironment, creating and starting it if it isn't already.
     *
     * @return The {@link WebDriver} associated with this DriverEnvironment
     */
    private WebDriver _getDriver() {
        if (!isStarted()) {
            // Sanity check: are we abandoning an open stream?
            if (driver != null && !isClosed()) {
                driver.quit();
            }
            start();
        }
        return driver;
    }

    /**
     * Get whether the {@link WebDriver} associated with this DriverEnvironment has been started.
     *
     * @return {@code true} if the {@link WebDriver} associated with this DriverEnvironment has been started,
     *          {@code false} otherwise
     */
    private boolean isStarted() {
        return isStarted;
    }

    /**
     * Start the {@link WebDriver} associated with this DriverEnvironment.
     */
    private void start() {
        isStarted = true;

        // Sanity check: because we haven't started the driver yet, it should be null
        if (driver != null) {
            driver.quit();
        }

        this.driver = WebDriverFactory.get(browser, proxy);
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DriverEnvironment that = (DriverEnvironment) o;

        return (driver != null ? driver.equals(that.driver) : that.driver == null) &&
                browser == that.browser &&
                (proxy != null ? proxy.equals(that.proxy) : that.proxy == null);
    }

    @Override
    public int hashCode() {
        int result = driver != null ? driver.hashCode() : 0;
        result = 31 * result + (browser != null ? browser.hashCode() : 0);
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DriverEnvironment{" +
                "isClosed=" + isClosed +
                ", driver=" + driver +
                ", isStarted=" + isStarted +
                ", browser=" + browser +
                ", proxy='" + proxy + '\'' +
                '}';
    }
}
