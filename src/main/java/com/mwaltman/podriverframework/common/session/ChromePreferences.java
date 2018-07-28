package com.mwaltman.podriverframework.common.session;

public enum ChromePreferences {

    /* Preferences */
    MULTIPLE_AUTOMATIC_DOWNLOADS("profile.content_settings.pattern_pairs.*.multiple-automatic-downloads"),
    PROMPT_FOR_DOWNLOAD("download.propmt_for_download"),
    SAFE_BROWSING_ENABLED("safebrowsing.enabled"),

    /* Experimental Options */
    EXCLUDE_SWITCHES("excludeSwitches"),
    IGNORE_CERTIFICATE_ERRORS("ignore-certificate-errors"),
    PREFS("prefs"),

    /* Arguments */
    DISABLE_SAVE_PASSWORD_BUBBLE("disable-save-password-bubble"),
    NEW_WINDOW("new-window"),
    PROXY_SERVER("--proxy-server=");

    private String value;

    ChromePreferences(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
