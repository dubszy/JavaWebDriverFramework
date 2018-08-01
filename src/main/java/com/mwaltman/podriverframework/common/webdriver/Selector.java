package com.mwaltman.podriverframework.common.webdriver;

import com.mwaltman.podriverframework.common.loadables.pageobjects.AbstractPage;
import com.mwaltman.podriverframework.common.session.DriverEnvironment;
import com.mwaltman.podriverframework.common.session.Session;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.log4testng.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wraps a web element's selector (CSS, XPATH, etc) for the purpose of
 * quickly creating, using, and discarding {@link WebElement} instances.
 * Selectors can be created using any of the {@link By} mechanisms.
 *
 * The benefit of this class is that, because {@link WebElement} instances
 * are discarded after one use, it greatly reduces the amount of
 * {@link StaleElementReferenceException}s that would be thrown by WebDriver
 * due to DOM changes after creating an instance of {@link WebElement}.
 *
 * @see AbstractPage#getComments() Find comment elements on a page using JavaScript
 */
public class Selector {

    private static final Logger log = Logger.getLogger(Selector.class);

    /**
     * The locator to use (CSS, XPATH, or otherwise) to find WebElement(s).
     *
     * @see ByFactory Available locator types
     */
    private final String locator;

    /**
     * The type of {@link By} used to define how to look for {@link #locator}.
     */
    private final ByFactory byType;

    /**
     * The {@link Session} associated with this Selector (used to access the WebDriver layer).
     *
     * @see DriverEnvironment Accessing WebDriver
     */
    private final Session session;

    /**
     * Used for retrieving system time while waiting.
     *
     * @see #waitUntil(Predicate) Waiting for a WebElement to satisfy a predicate
     * @see #waitUntil(ExpectedCondition) Waiting for a WebElement to satisfy an {@link ExpectedCondition}
     */
    private final Clock clock;

    /**
     * Used to control polling speed while waiting.
     *
     * @see #waitUntil(Predicate) Waiting for a WebElement to satisfy a predicate
     * @see #waitUntil(ExpectedCondition) Waiting for a WebElement to satisfy an {@link ExpectedCondition}
     */
    private final Sleeper sleeper;

    /**
     * Construct a new instance of Selector with a {@link Session}, locator string, and a {@link ByFactory}.
     *
     * @param session The session to associate with this Selector
     * @param locator The locator string to use to find web element(s) on the page
     * @param byType The type of {@link By} used to define how to look for {@code locator}
     */
    public Selector(Session session, String locator, ByFactory byType) {
        this.session = session;
        this.locator = locator;
        this.byType = byType;
        this.clock = new SystemClock();
        this.sleeper = Sleeper.SYSTEM_SLEEPER;
    }

    /**
     * Construct a new instance of Selector with a {@link Session} and a CSS locator string.
     *
     * @param session The session to associate with this Selector
     * @param cssLocator The CSS locator string to use to find web element(s) on the page
     */
    public Selector(Session session, String cssLocator) {
        this(session, cssLocator, ByFactory.CSS);
    }

    /**
     * Create a new Selector using another Selector's locator as a container. Currently, this only supports CSS
     * selectors (for both container and the new Selector).
     *
     * @param session The session to associate this Selector with
     * @param container The container Selector to build upon
     * @param cssLocator The CSS locator of the Selector to find inside {@code container}
     *
     * @return A new instance of Selector with the container's locator concatenated with {@code cssLocator}
     *
     * @exception UnsupportedOperationException If the {@link Selector#byType} of {@code container} is not
     *                                          {@link ByFactory#CSS}
     */
    public static Selector withContainer(Session session, Selector container, String cssLocator) {
        // TODO: Add support for all locator types, as well as mix-and-match
        if (container.byType != ByFactory.CSS) {
            throw new UnsupportedOperationException(
                    "Containers only currently support CSS selectors for locators, the container supplied uses: '" +
                            container.byType + "'");
        }
        return new Selector(session, container.locator.trim() + " " + cssLocator);
    }

    /**
     * Get the locator string of this Selector.
     *
     * @return The locator string of this Selector
     */
    public String getLocator() {
        return locator;
    }

    /**
     * Get the {@link By} type (as a {@link ByFactory}) of this Selector.
     *
     * @return A {@link ByFactory} representing the {@link By} type
     */
    public ByFactory getByType() {
        return byType;
    }

    /**
     * Get the {@link Session} associated with this Selector.
     *
     * @return The {@link Session} associated with this Selector
     */
    public Session getSession() {
        return session;
    }

    /**
     * Get a new instance of {@link WebElement} using this Selector's locator to search for it
     *
     * @return A new instance of {@link WebElement} located by {@link #locator}
     */
    public WebElement get() {
        return session.getDriverEnvironment().getDriver().findElement(byType.get(locator));
    }

    /**
     * Get new instances of {@link WebElement} for all elements that match this Selector's locator.
     *
     * @return A {@link List} of {@link WebElement}s that match this Selector's locator
     */
    public List<WebElement> getMultiple() {
        return session.getDriverEnvironment().getDriver().findElements(byType.get(locator));
    }

    /**
     * Get a new instance of {@link WebElement} for the first element that satisfies the {@link Predicate}
     * {@code condition}.
     *
     * @param condition A predicate with a {@link WebElement} parameter to satisfy
     *
     * @return The first {@link WebElement} found that satisfies the predicate
     */
    public WebElement getWhere(Predicate<WebElement> condition) {
        Optional<WebElement> maybeElement;

        try {
            maybeElement = getMultiple().stream().filter(condition).findFirst();
        } catch (NullPointerException e) {
            throw new RuntimeException(
                    "First element in stream was null when attempting to perform a getWhere. Selector locator: '" +
                            locator + "'   WebElement predicate: " + condition, e);
        }

        if (!maybeElement.isPresent()) {
            throw new RuntimeException("Could not find an element that satisfies the predicate. Selector locator: '" +
                    locator + "'   WebElement predicate: " + condition);
        }

        return maybeElement.get();
    }

    /**
     * Get new instances of {@link WebElement} for all elements that satisfy the {@link Predicate} {@code condition}.
     *
     * @param condition A predicate with a {@link WebElement} parameter to satisfy
     *
     * @return A {@link List} of {@link WebElement}s that satisfy the predicate
     */
    public List<WebElement> getMultipleWhere(Predicate<WebElement> condition) {
        List<WebElement> webElements = getMultiple().stream().filter(condition).collect(Collectors.toList());
        if (webElements.isEmpty()) {
            throw new RuntimeException("Could not find any elements that satisfy the predicate. Selector locator: '" +
                    locator + "'   WebElement predicate: " + condition);
        }
        return webElements;
    }

    //***** WebElement Information *****//

    /**
     * Whether a {@link WebElement} can be found by this Selector's {@link #locator}.
     *
     * @return {@code true} if a {@link WebElement} is found, {@code false} otherwise
     *
     * @see #get() Getting a WebElement from a Selector
     */
    public boolean isPresent() {
        try {
            get();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Whether the first {@link WebElement} found by this Selector's {@link #locator} is currently displayed.
     *
     * @return {@code true} if the first {@link WebElement} is currently displayed, {@code false} otherwise
     *
     * @see WebElement#isDisplayed() Whether a WebElement is displayed
     */
    public boolean isDisplayed() {
        return get().isDisplayed();
    }

    /**
     * Whether the first {@link WebElement} found by this Selector's {@link #locator} is currently enabled. This will
     * generally return {@code true} for everything except disabled input elements.
     *
     * @return {@code true} if the first {@link WebElement} is currently enabled, {@code false} otherwise
     *
     * @see WebElement#isEnabled() Whether a WebElement is enabled
     */
    public boolean isEnabled() {
        return get().isEnabled();
    }

    /**
     * Whether the first {@link WebElement} found by this Selector's {@link #locator} is currently selected or checked.
     *
     * @return {@code true} if the first {@link WebElement} is currently selected or checked, {@code false} otherwise
     *
     * @see WebElement#isSelected() Whether a WebElement is selected
     */
    public boolean isSelected() {
        return get().isSelected();
    }

    /**
     * Get the value of a HTML attribute of the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @param name The name of the attribute to get the value of
     *
     * @return The value of the specified attribute of the first {@link WebElement}
     *
     * @see WebElement#getAttribute(String) Getting the value of a HTML attribute of a WebElement
     */
    public String getAttribute(String name) {
        return get().getAttribute(name);
    }

    /**
     * Get the CSS value of a given property of the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @param propertyName The name of the CSS property to get the value of
     *
     * @return The value of the specified CSS property of the first {@link WebElement}
     *
     * @see WebElement#getCssValue(String) Getting the CSS value of a given property of a WebElement
     */
    public String getCssValue(String propertyName) {
        return get().getCssValue(propertyName);
    }

    /**
     * Get the tag name of the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @return The tag name of the first {@link WebElement}
     *
     * @see WebElement#getTagName() Getting the tag name of a WebElement
     */
    public String getTagName() {
        return get().getTagName();
    }

    /**
     * Get the visible text of the first {@link WebElement} (and all sub-elements) found by this Selector's
     * {@link #locator}, without any leading or trailing whitespace.
     *
     * @return The visible text of the first {@link WebElement}
     *
     * @see WebElement#getText() Getting the text of a WebElement
     */
    public String getText() {
        return get().getText();
    }

    /**
     * Get the location of the top-left corner of the first {@link WebElement} found by this Selector's
     * {@link #locator}.
     *
     * @return The location of the top-left corner of the first {@link WebElement}
     *
     * @see WebElement#getLocation() Getting the location of a WebElement
     */
    public Point getLocation() {
        return get().getLocation();
    }

    /**
     * Get the size of the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @return The size of the first {@link WebElement}
     *
     * @see WebElement#getSize() Getting the size of a WebElement
     */
    public Dimension getSize() {
        return get().getSize();
    }

    /**
     * Get the location and size of the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @return The location and size of the first {@link WebElement}
     *
     * @see WebElement#getRect() Getting the location and size of a WebElement
     */
    public Rectangle getRect() {
        return get().getRect();
    }


    //***** WebElement Actions *****//

    /**
     * Clear the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @return This instance of Selector
     *
     * @see WebElement#clear() Clearing a WebElement
     */
    public Selector clear() {
        get().clear();
        return this;
    }

    /**
     * Click the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @return This instance of Selector
     *
     * @see WebElement#click() Clicking a WebElement
     */
    public Selector click() {
        get().click();
        return this;
    }

    /**
     * Send keys to (type into) the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @param chars The characters to send
     *
     * @return This instance of Selector
     *
     * @see WebElement#sendKeys(CharSequence...) Sending keys to a WebElement
     */
    public Selector sendKeys(CharSequence... chars) {
        get().sendKeys(chars);
        return this;
    }

    /**
     * Submit the first {@link WebElement} found by this Selector's {@link #locator}.
     *
     * @return This instance of Selector
     *
     * @see WebElement#submit() Submitting a WebElement
     */
    public Selector submit() {
        get().submit();
        return this;
    }

    //***** Waiting *****//

    /**
     * Wait until a {@link WebElement} that satisfies the {@link Predicate} {@code condition} is found on the page.
     * Waits for a maximum of 10 seconds.
     *
     * @param condition A predicate with a {@link WebElement} parameter to satisfy
     *
     * @return This instance of Selector
     *
     * @exception TimeoutException If a {@link WebElement} that satisfies the predicate was not found in the allotted
     *                             time. A {@link NoSuchElementException} may be piggy-backed onto this exception if
     *                             one was thrown while waiting.
     */
    public Selector waitUntil(Predicate<WebElement> condition) {
        long timeoutMilliseconds = 10000L;
        long delay = clock.laterBy(timeoutMilliseconds);

        Throwable storedException = null;

        while (clock.isNowBefore(delay)) {
            try {
                if (condition.test(get())) {
                    return this;
                }
            } catch (NoSuchElementException e) {
                storedException = e;
            }

            try {
                sleeper.sleep(new Duration(200L, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        String message = "Timed out after " + timeoutMilliseconds +
                " milliseconds waiting for the first found element to match the predicate";

        if (storedException == null) {
            throw new TimeoutException(message);
        }
        throw new TimeoutException(message, storedException);
    }

    /**
     * Wait until a {@link WebElement} that satisfies an {@link ExpectedCondition} is found on the page.
     *
     * @param condition An {@link ExpectedCondition} with a {@link WebElement} parameter to satisfy
     *
     * @return This instance of Selector
     */
    public Selector waitUntil(ExpectedCondition<WebElement> condition) {
        new WebDriverWait(session.getDriverEnvironment().getDriver(), 10).until(condition);
        return this;
    }
}
