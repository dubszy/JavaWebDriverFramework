package com.mwaltman.podriverframework.common.loadables;


public enum ReadyState {
    UNINITIALIZED("uninitialized"),
    LOADING("loading"),
    LOADED("loaded"),
    INTERACTIVE("interactive"),
    COMPLETE("complete");

    String browserString;

    ReadyState(String browserString) {
        this.browserString = browserString;
    }

    public String getBrowserString() {
        return browserString;
    }

    public ReadyState identify(String browserString) {
        for (ReadyState aReadyState : values()) {
            if (aReadyState.getBrowserString().toLowerCase().equals(browserString.toLowerCase())) {
                return aReadyState;
            }
        }
        throw new IllegalArgumentException("Could not identify a ReadyState from the browser string: '" + browserString + "'");
    }
}
