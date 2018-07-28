package com.mwaltman.podriverframework.common.session;

import com.mwaltman.podriverframework.common.util.Property;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class WebDriverFactory {

    public static WebDriver get(Browser browser, String proxy) {

        WebDriver driver;

        DesiredCapabilities capabilities = browser.capabilities(proxy);

        switch (browser) {
            case CHROME:
                driver = chrome(capabilities);
                break;
            case FIREFOX:
                driver = firefox(capabilities);
                break;
            case REMOTE_CHROME:
                driver = remoteChrome(capabilities);
                break;
            default:
                throw new IllegalArgumentException("The browser supplied was not found or is not implemented");
        }

        driver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);

        return driver;
    }

    public static WebDriver chrome(DesiredCapabilities capabilities) {
        String chromeDriverLocation = Property.WEBDRIVER_CHROME_DRIVER.getPropertyValue();
        if (chromeDriverLocation.isEmpty()) {
            chromeDriverLocation = "src/main/resources/chromedriver";
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                chromeDriverLocation += ".exe";
            }
            System.setProperty(Property.WEBDRIVER_CHROME_DRIVER.getPropertyName(), chromeDriverLocation);
        }
        return new ChromeDriver(capabilities); // FIXME: Use ChromeDriver(ChromeOptions) instead
    }

    public static WebDriver remoteChrome(DesiredCapabilities capabilities) {
        URL url;
        try {
            url = new URL(
                    "http://" +
                            Property.SELENIUM_DRIVER_HOST.getPropertyValue() +
                            ":" +
                            Property.SELENIUM_DRIVER_PORT.getPropertyValue() +
                            "/wd/hub");
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL for accessing RemoteChrome was malformed, check the values of " +
                    Property.SELENIUM_DRIVER_HOST.getPropertyName() + " and " + Property.SELENIUM_DRIVER_PORT +
                    " in build.gradle");
        }

        return new RemoteWebDriver(url, capabilities);
    }

    public static WebDriver firefox(DesiredCapabilities capabilities) {
        return new FirefoxDriver(capabilities); // FIXME: Use FirefoxDriver(FirefoxOptions) instead
    }
}
