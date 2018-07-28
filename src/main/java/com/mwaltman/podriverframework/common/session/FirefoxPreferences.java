package com.mwaltman.podriverframework.common.session;

public enum FirefoxPreferences {
    NETWORK_PROXY_TYPE("network.proxy.type"),
    NETWORK_PROXY_HTTP("network.proxy.http"),
    NETWORK_PROXY_PORT("network.proxy.port");

    private String value;

    FirefoxPreferences(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
