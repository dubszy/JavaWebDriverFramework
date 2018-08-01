package com.mwaltman.podriverframework.common.webdriver;

import org.openqa.selenium.By;

import java.util.function.Function;

/**
 * Defines {@link By} types that can be used to find web elements on a page.
 * Designed to be used with {@link Selector} to search for web elements.
 *
 * Each constant holds a lambda to create and return an instance of {@link By},
 * using the methods provided in {@link By}.
 */
public enum ByFactory {

    CSS(By::cssSelector),
    XPATH(By::xpath),
    ID(By::id),
    LINK_TEXT(By::linkText),
    PARTIAL_LINK_TEXT(By::partialLinkText),
    TAG_NAME(By::tagName),
    CLASS_NAME(By::className);

    /**
     * The method to call that accepts a single locator argument as a String,
     * and returns a {@link By}.
     */
    Function<String, By> byFunction;

    /**
     * Construct a new instance of ByFactory that contains a single {@link Function}
     * parameter, which is used to construct a new instance of {@link By}.
     * {@code byFunction} must accept a single locator argument as a String, and must
     * return a {@link By}.
     *
     * @param byFunction The function to use to create a new instance of {@link By}
     */
    ByFactory(Function<String, By> byFunction) {
        this.byFunction = byFunction;
    }

    /**
     * Get the {@link By} instance from a constant.
     *
     * @param locator The locator
     * @return
     */
    public By get(String locator) {
        return byFunction.apply(locator);
    }
}
