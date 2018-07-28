package com.mwaltman.podriverframework.common.util;

public enum RegexUtil {

    INTERPOLATE_SINGLE("\\$\\{(\\w+)}");

    private String pattern;

    RegexUtil(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
