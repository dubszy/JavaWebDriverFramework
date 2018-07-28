package com.mwaltman.podriverframework.common.session;

import org.openqa.selenium.remote.DesiredCapabilities;

public interface CapabilityFactory {

    /**
     * Get the {@link DesiredCapabilities} for this instance.
     *
     * @param proxy The path to a proxy server, if one is being used, if not, pass an empty string
     *
     * @return The {@link DesiredCapabilities} for this instance
     */
    DesiredCapabilities getCapabilities(String proxy);
}
