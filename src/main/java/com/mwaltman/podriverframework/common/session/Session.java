package com.mwaltman.podriverframework.common.session;

import com.mwaltman.podriverframework.common.util.Property;
import org.testng.log4testng.Logger;

import java.io.IOException;

/**
 * Creates, configures, stores, and maintains the environment and data for a single test.
 *
 * Makes use of:
 *  {@link Store} for storing any data the tester deems necessary for the current test
 *  {@link DriverEnvironment} for managing the connection to {@link org.openqa.selenium.WebDriver}
 *
 * The idea behind this class is to have an easy way to create a fresh environment for each test to avoid leaking data
 * and resources from one test to another. Because this class implements AutoCloseable, it can be used in a
 * try-with-resources block, and cleanup can be fully automatic at the end of a test method. Due to the design of this
 * framework, and this class specifically, it would be considered an anti-pattern to use one Session for more than one
 * test, although it is possible. Creating a new Session at the start of each test allows each test to be effectively
 * free from side-effects and external influences. For example, by starting with a fresh browser instance each time a
 * new test is run, we don't run the risk of the browser carrying its state over to another test, and thus potentially
 * invalidating the results of latter tests.
 */
public class Session implements AutoCloseable {

    private final Logger log = Logger.getLogger(Session.class);

    private Store store;

    private String host;

    /*
     * Transient because if we want to serialize this Session and pick it back up later,
     * we want to start with a fresh DriverEnvironment
     */
    private transient DriverEnvironment driverEnvironment;

    private boolean isClosed;

    protected Session(Store store, String host, DriverEnvironment driverEnvironment) {
        this.store = store;
        this.host = host;
        this.driverEnvironment = driverEnvironment;
    }

    public static Session defaultSession(String startingHost) {
        return new Session(new Store(), startingHost, DriverEnvironment.defaultDriverEnvironment());
    }

    /**
     * Get whether this Session is closed.
     *
     * @return {@code true} if this Session is closed, {@code false} otherwise
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Get the {@link Store} associated with this Session.
     *
     * @return The {@link Store} associated with this Session.
     *
     * @exception IllegalStateException If this Session is closed
     * @exception NullPointerException If the {@link Store} associated with this Session is null
     */
    public Store getStore() {
        if (isClosed()) {
            throw new IllegalStateException("The Session has already been closed");
        }

        if (store == null) {
            throw new NullPointerException("The store for this Session is null");
        }

        return store;
    }

    /**
     * Get the host for this Session.
     *
     * @return The host for this Session
     *
     * @exception IllegalStateException If this Session is closed
     * @exception NullPointerException If the host for this Session is null or an empty string
     */
    public String getHost() {
        if (isClosed()) {
            throw new IllegalStateException("The Session has already been closed");
        }

        if (host == null || host.isEmpty()) {
            throw new NullPointerException("The host for this Session is null or empty");
        }

        return host;
    }

    /**
     * Get the {@link DriverEnvironment} associated with this Session.
     *
     * @return The {@link DriverEnvironment} associated with this Session.
     *
     * @exception IllegalStateException If this Session is closed
     * @exception NullPointerException If the {@link DriverEnvironment} associated with this Session is null
     */
    public DriverEnvironment getDriverEnvironment() {
        if (isClosed()) {
            throw new IllegalStateException("The Session has already been closed");
        }

        if (driverEnvironment == null) {
            throw new NullPointerException("The DriverEnvironment for this Session is null");
        }

        return driverEnvironment;
    }

    /**
     * Switch from the current host to a new host for this Session.
     *
     * @param newHost The new host to assign to this Session
     *
     * @return This instance
     *
     * @exception IllegalStateException If this Session is closed
     * @exception NullPointerException If the current host for this Session is null or an empty string
     * @exception IllegalArgumentException If the new host to assign to this Session is null or an empty string
     * @exception IllegalArgumentException If the new host to assign to this Session is the same as the current host
     */
    public Session switchHosts(String newHost) {
        if (isClosed()) {
            throw new IllegalStateException("The Session has already been closed");
        }

        if (host == null || host.isEmpty()) {
            throw new NullPointerException("The host for this Session is null or empty");
        }

        if (newHost == null || newHost.isEmpty()) {
            throw new IllegalArgumentException("The host to switch to cannot be null or empty");
        }

        if (host.equals(newHost)) {
            throw new IllegalArgumentException("The new host cannot be the same as the current host");
        }

        this.host = newHost;
        return this;
    }

    /**
     * Close this Session.
     */
    @Override
    public void close() {
        isClosed = true;
        store = null;
        host = null;
        driverEnvironment.close();
        driverEnvironment = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        return (store != null ? store.equals(session.store) : session.store == null) &&
                (host != null ? host.equals(session.host) : session.host == null) &&
                (driverEnvironment != null ? driverEnvironment.equals(session.driverEnvironment) : session.driverEnvironment == null);
    }

    @Override
    public int hashCode() {
        int result = store != null ? store.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (driverEnvironment != null ? driverEnvironment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Session{" +
                "store=" + store +
                ", host='" + host + '\'' +
                ", driverEnvironment=" + driverEnvironment +
                ", isClosed=" + isClosed +
                '}';
    }
}
