package com.mwaltman.podriverframework.common.loadables.pageobjects;

import com.mwaltman.podriverframework.common.loadables.AbstractLoadable;
import com.mwaltman.podriverframework.common.session.Session;

/**
 * Base class/page object that all page objects should extend.
 */
public abstract class AbstractPage extends AbstractLoadable {

    public AbstractPage(Session session) {
        super(session);
    }

    public AbstractPage(AbstractLoadable loadable) {
        super(loadable);
    }

    /**
     * Navigate to the URL as defined by {@link Session#getHost()} for the current session.
     *
     * This method exists to complete the abstraction layer between WebDriver and page objects
     * so that tests should not ever need to access the WebDriver layer.
     *
     * @param <T> The type of page object the caller would like to get returned, A.K.A, what
     *            type the page object is that represents the page that will load when the
     *            base URL is navigated to
     *
     * @return This instance casted to type {@code T}
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractPage> T navigateToBaseUrl() {
        getSession().getDriverEnvironment().goToUrl(getSession().getHost());
        return (T) this;
    }

    /**
     * Get all HTML comments on the current page. Since there is no CSS selector
     * for comments, this is outside of the normal flow.
     *
     * @return The HTML comments on the current page as an {@link Object}
     */
    public Object getComments() {
        return getSession().getDriverEnvironment()
                .executeJS("$('*').contents().filter(function() { return this.nodeType === 8; })", false);
    }
}
