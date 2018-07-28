package com.mwaltman.podriverframework.common.session;

import com.mwaltman.podriverframework.common.util.Property;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.log4testng.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Browser {

    CHROME("chrome", new CapabilityFactory() {
        @Override
        public DesiredCapabilities getCapabilities(String proxy) {
            ChromeOptions options = new ChromeOptions();
            Map<String, Object> preferences = new HashMap<>();

            preferences.put(ChromePreferences.MULTIPLE_AUTOMATIC_DOWNLOADS.getValue(), 1);
            preferences.put(ChromePreferences.PROMPT_FOR_DOWNLOAD.getValue(), false);
            preferences.put(ChromePreferences.SAFE_BROWSING_ENABLED.getValue(), false);

            options.setExperimentalOption(ChromePreferences.EXCLUDE_SWITCHES.getValue(),
                    Collections.singletonList(ChromePreferences.IGNORE_CERTIFICATE_ERRORS.getValue()));

            options.setExperimentalOption(ChromePreferences.PREFS.getValue(), preferences);

            options.addArguments(Arrays.asList(
                    ChromePreferences.DISABLE_SAVE_PASSWORD_BUBBLE.getValue(),
                    ChromePreferences.NEW_WINDOW.getValue()
            ));

            if (proxy != null && !proxy.isEmpty()) {
                options.addArguments(ChromePreferences.PROXY_SERVER.getValue() + proxy);
            }

            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            return capabilities;
        }
    }),

    REMOTE_CHROME("remotechrome", new CapabilityFactory() {
        @Override
        public DesiredCapabilities getCapabilities(String proxy) {
            String chromeDriverLocation = Property.WEBDRIVER_CHROME_DRIVER.getPropertyValue();
            if (chromeDriverLocation == null || chromeDriverLocation.isEmpty()) {
                throw new IllegalStateException(Property.WEBDRIVER_CHROME_DRIVER.getPropertyName() + " is not defined");
            }
            return CHROME.capabilities.getCapabilities(proxy);
        }
    }),

    FIREFOX("firefox", new CapabilityFactory() {
        @Override
        public DesiredCapabilities getCapabilities(String proxy) {
            FirefoxProfile profile = new FirefoxProfile();

            if (proxy != null && !proxy.isEmpty()) {
                profile.setPreference(FirefoxPreferences.NETWORK_PROXY_TYPE.getValue(), 1);
                String[] hostAndPort = proxy.split(":");
                profile.setPreference(FirefoxPreferences.NETWORK_PROXY_HTTP.getValue(), hostAndPort[0]);
                profile.setPreference(FirefoxPreferences.NETWORK_PROXY_PORT.getValue(), hostAndPort[1]);
            }

            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            capabilities.setCapability(FirefoxDriver.PROFILE, profile);
            return capabilities;
        }
    });

    private static final Logger log = Logger.getLogger(Browser.class);

    private final String name;

    private final CapabilityFactory capabilities;

    Browser(String name, CapabilityFactory capabilities) {
        this.name = name;
        this.capabilities = capabilities;
    }

    public String getName() {
        return name;
    }

    public DesiredCapabilities capabilities(String proxy) {
        return capabilities.getCapabilities(proxy);
    }

    public static Browser identify(String name) {
        for (Browser aBrowser : Browser.values()) {
            if (aBrowser.getName().toLowerCase().equals(name.toLowerCase())) {
                return aBrowser;
            }
        }
        throw new IllegalArgumentException("Could not identify a browser by the name of " + name);
    }
}
