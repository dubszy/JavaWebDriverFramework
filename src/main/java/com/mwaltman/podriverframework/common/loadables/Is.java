package com.mwaltman.podriverframework.common.loadables;

import com.mwaltman.podriverframework.common.webdriver.Selector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This interface contains two @interfaces designed to be attached to {@link Selector}
 * or components in page objects. Both annotations contained in this interface are
 * designed to be used in conjunction with the {@link AbstractLoadable#isReady()} method
 * to determine when a page that has been travelled to is ready to be interacted with.
 */
public interface Is {

    /**
     * This annotation should be attached to {@link Selector} or component fields in
     * page objects or component objects that are required to be in a certain state
     * for the page to be considered "ready to use".
     *
     * Parameters in this annotation are used in conjunction with {@link AbstractLoadable#isReady()}.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Ready {

        /**
         * Defines what ready state the document must be in for the
         * isReady() method to start searching for the Selector or
         * component that @Is.Ready is attached to.
         *
         * @return {@link ReadyState}
         */
        ReadyState documentIs() default ReadyState.UNINITIALIZED;

        /**
         * Whether the element found must also be visible.
         *
         * @return {@code true} if the element must be visible, {@code false} if the
         *          element <b>does not need</b> to be visible.
         */
        boolean whenVisible() default false;

        /**
         * What, if any, text the element found must contain.
         *
         * @return The text the element must contain
         */
        String whenTextContains() default "";

        /**
         * What, if any, CSS classes the element found must have.
         *
         * @return The CSS classes the element must have
         */
        String[] ifHasCssClasses() default {""};

        /**
         * Exactly how many elements the isReady() method must find before
         * returning that the Selector or component @Is.Ready is
         * attached to is ready. Note that no more or no less than the
         * count set in this field may be found for the isReady() method
         * to succeed. This field supersedes {@link #whenCountAtLeast()}
         * and {@link #whenCountAtMost()} even if those fields are set.
         *
         * @return An integer representing exactly how many elements must be found
         *
         * @see #whenCountAtLeast() Find a minimum amount of elements
         * @see #whenCountAtMost() Find a maximum amount of elements
         */
        int whenCountExactly() default -1;

        /**
         * The minimum amount of elements (inclusive) that the
         * isReady() method can find in order for the Selector
         * or component annotated with @Is.Ready to be considered
         * ready. Note that more than the count set in this field
         * may be found (up to and including the value of
         * {@link #whenCountAtMost()}, if that value is greater
         * than 0).
         *
         * @return An integer representing at least how many elements must be found
         *
         * @see #whenCountExactly() Find an exact amount of elements
         * @see #whenCountAtMost() Find a maximum amount of elements
         */
        int whenCountAtLeast() default -1;

        /**
         * The maximum amount of elements (inclusive) that the
         * isReady() method can find in order for the Selector
         * or component annotated with @Is.Ready to be considered
         * ready. Note that less than the count set in this field
         * may be found (down to and including the value of
         * {@link #whenCountAtLeast()}, or 1, whichever is larger).
         *
         * @return An integer representing at most how many elements must be found
         */
        int whenCountAtMost() default -1;
    }

    /**
     * This annotation should be attached to {@link Selector} or component fields in
     * page objects or component objects that are required to be required to be either
     * gone or invisible for the page to be considered "ready to use". For example,
     * loading spinners, progress bars, etc.
     *
     * Parameters in this annotation are used in conjunction with {@link AbstractLoadable#isReady()}.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Loader {

        /**
         * Whether the element found must no longer be present in the DOM.
         *
         * @return {@code true} if the element must no longer be present in
         *         in the DOM, {@code false} if the element <b>does not need</b>
         *         to be gone from the DOM
         */
        boolean mustBeGone() default false;

        /**
         * Whether the element found must be invisible.
         *
         * @return {@code true} if the element must be invisible, {@code false}
         *         otherwise
         */
        boolean mustBeInvisible() default true;
    }
}
