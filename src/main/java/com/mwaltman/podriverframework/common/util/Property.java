package com.mwaltman.podriverframework.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Define supported Java properties for consistent usage and reference.
 *
 * This enum must be kept in sync with the systemProperty declarations in build.gradle
 */
public enum Property {

    // WebDriver properties
    WEBDRIVER_CHROME_DRIVER("webdriver.chrome.driver"),

    SELENIUM_DRIVER_BROWSER("selenium.driver.browser"),
    SELENIUM_DRIVER_HOST("selenium.driver.host"),
    SELENIUM_DRIVER_PORT("selenium.driver.port"),
    SELENIUM_DRIVER_PROFILE("selenium.driver.profile"),

    // Proxy properties
    PROXY_SERVER("proxy.server");

    /**
     * A property name as defined in build.gradle
     */
    private String propertyName;

    Property(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyValue() {
        String property = System.getProperty(getPropertyName());
        return property != null ? property : "";
    }

    public static List<String> getAllProperties() {
        List<String> properties = new ArrayList<>();

        for (Property aProperty : Property.values()) {
            properties.add(aProperty.getPropertyName() + " = " + aProperty.getPropertyValue());
        }

        return properties;
    }
}
