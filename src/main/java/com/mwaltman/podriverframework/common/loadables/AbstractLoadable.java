package com.mwaltman.podriverframework.common.loadables;

import com.mwaltman.podriverframework.common.session.Session;
import com.mwaltman.podriverframework.common.webdriver.Selector;
import org.testng.log4testng.Logger;

import java.util.Stack;

/**
 * Base class that all loadable objects (ie page objects and components) should extend.
 *
 * The page object setup of this framework is split into two sections: page objects and components.
 * Page objects should represent entire pages (or sub-pages of a parent page). Components should
 * represent reusable components defined throughout a website or a web application (such as page-level
 * -- not browser level -- overlays or containers that encapsulate the same fields used in multiple
 * places).
 */
public abstract class AbstractLoadable {

    protected final Logger log = Logger.getLogger(this.getClass());

    private final Session session;

    protected AbstractLoadable(Session session) {
        this.session = session;
    }

    protected AbstractLoadable(AbstractLoadable loadable) {
        this(loadable.getSession());
    }

    /**
     * Get the {@link Session} associated with this component.
     *
     * @return The {@link Session} associated with this component
     */
    public Session getSession() {
        return session;
    }

    /**
     * Get the relative path to the subclass that this is called from.
     * The path is built from @Path annotations attached at the class-level.
     *
     * @return The relative path to this page (does not include the domain name)
     *
     * @see Path Assigning paths to page objects
     */
    public String getRelativePathTo() {
        Stack<Path> paths = new Stack<>();
        Class currentClass = this.getClass();
        StringBuilder url = new StringBuilder();

        boolean shouldContinue = true;

        if (currentClass.getAnnotation(Path.class) == null) {
            throw new RuntimeException("The " + this.getClass().getSimpleName() +
                    " does not have a relative URL declared. See @Path to add a relative URL to this page.");
        }

        do {
            if (currentClass.getSimpleName().equalsIgnoreCase("object")) {
                shouldContinue = false; // We don't want to be this far up the class hierarchy
            }

            Path path = (Path) currentClass.getAnnotation(Path.class);

            if (path == null) { // Page has no path
                shouldContinue = false;
            } else {
                paths.push(path); // Not null, push it onto the stack
                currentClass = currentClass.getSuperclass(); // Climb to the parent
            }
        } while (shouldContinue);

        while (!paths.isEmpty()) { // Loop through the stack of paths
            String tempPath = paths.pop().value();
            url.append(tempPath);
        }
        return url.toString();
    }

    /**
     * Get the full path to the subclass that this is called from.
     * The path is built from {@link Path}s attached at the class-level
     * and then concatenated with the base URL as defined by {@link Session#getHost()}
     * for the current session.
     *
     * @return The full path to this page (including the domain name)
     *
     * @see #getRelativePathTo() Get the relative path to a page
     * @see Path Assigning paths to page objects
     */
    public String getPathTo() {
        return getSession().getHost() + getRelativePathTo();
    }

    /**
     * Whether a loadable is ready to be interacted with by this framework.
     * (As defined by {@link Is.Ready} and {@link Is.Loader} annotations
     * declared on {@link Selector} and component object fields in a page
     * object or component object.
     *
     * @return The result of calling {@link IsValidator#validateLoadable(AbstractLoadable)}
     *         with the single argument: {@code this}
     */
    public boolean isReady() {
        return new IsValidator().validateLoadable(this);
    }
}
