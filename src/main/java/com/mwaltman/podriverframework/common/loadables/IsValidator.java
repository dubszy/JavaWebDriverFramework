package com.mwaltman.podriverframework.common.loadables;

import com.mwaltman.podriverframework.common.loadables.pagecomponents.AbstractComponent;
import com.mwaltman.podriverframework.common.webdriver.Selector;
import org.testng.log4testng.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Validates criteria in {@link Is.Ready} and {@link Is.Loader} annotations attached to {@link Selector} or component
 * fields in page objects.
 *
 * @see Is Attaching criteria to fields in page objects
 * @see AbstractLoadable#isReady() Method for processing criteria
 */
public class IsValidator {

    private final Logger log = Logger.getLogger(IsValidator.class);

    public boolean validateLoadable(AbstractLoadable loadable) {

        String className = loadable.getClass().getSimpleName();

        List<Class> classes = new ArrayList<>();
        classes.add(loadable.getClass());

        // Climb the class hierarchy to get all superclasses of 'this' until we reach the base class
        while ( !className.equals("AbstractComponent") &&
                !className.equals("AbstractPage")) {
            // This is a little dangerous because we're modifying a List while iterating over it
            classes.add(0, classes.get(0).getSuperclass());
            className = classes.get(0).getSuperclass().getSimpleName();
        }

        // Iterate through each class starting with the most super superclass
        for (Class aClass : classes) {
            Map<Field, Map<Is.Ready, Is.Loader>> found = new HashMap<>();

            // Loop through each field declared on this class
            for (Field aField : aClass.getDeclaredFields()) {
                aField.setAccessible(true); // Goodbye accessors, reflection ftw

                Annotation isReadyAnnotation = null;
                Annotation isLoaderAnnotation = null;

                // Loop through each annotation declared on this field
                for (Annotation anAnnotation : aField.getDeclaredAnnotations()) {
                    if (anAnnotation instanceof Is.Ready) {
                        isReadyAnnotation = anAnnotation;
                    } else if (anAnnotation instanceof Is.Loader) {
                        isLoaderAnnotation = anAnnotation;
                    }
                }

                if (isReadyAnnotation != null) {
                    if (!validateField(
                            loadable,
                            aField,
                            (Is.Ready) isReadyAnnotation,
                            Optional.ofNullable((Is.Loader) isLoaderAnnotation))) {
                        return false;
                    }
                }
            }
        }
        return true; // If we made it here, everything validated
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public boolean validateField(AbstractLoadable loadable, Field field, Is.Ready ready, Optional<Is.Loader> loader) {
        if (loadable == null) {
            throw new NullPointerException("Loadable instance cannot be null");
        }

        if (field == null) {
            throw new NullPointerException("Field instance cannot be null");
        }

        Object type = null;
        try {
            type = field.get(loadable);
        } catch (IllegalAccessException e) {
            log.error("Could not access the field: " + field.getName());
        }

        if (type instanceof Selector) {
            return validateSelector((Selector) type, ready, loader);
        } else if (type instanceof AbstractComponent) {
            return validateSelector(((AbstractComponent) type).getContainer(), ready, loader) &&
                    validateLoadable((AbstractComponent) type);
        }
        throw new IllegalArgumentException(
                "Is.Ready and Is.Loader can only be declared on fields of type Selector or AbstractComponent");
    }


    /**
     * Validate parameters in an {@link Is.Ready} annotation attached to a Selector field
     *
     * @param selector The {@link Selector} to validate
     * @param ready    {@link Is.Ready} annotation attached to {@code selector}
     * @param loader   {@link Is.Loader} annotation attached to {@code selector}, if one is present (use
     *                 {@link Optional#empty()} if one is not present
     * @return {@code true} if all fields pass validation, {@code false} otherwise
     * @throws NullPointerException     If {@code selector} is {@code null}
     * @throws NullPointerException     If {@code ready} is {@code null}
     * @throws IllegalArgumentException If a call to {@link Selector#getLocator()} returns {@code null} for
     *                                  {@code selector}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public boolean validateSelector(Selector selector, Is.Ready ready, Optional<Is.Loader> loader) {
        // Null checks and argument validation come first
        if (selector == null) {
            throw new NullPointerException("Selector instance cannot be null");
        }

        if (selector.getLocator() == null || selector.getLocator().isEmpty()) {
            throw new IllegalArgumentException("Selector's locator cannot be null or empty");
        }

        if (ready == null) {
            throw new NullPointerException("Is.Ready annotation instance cannot be null or empty");
        }

        // TODO: Check that the document is in the correct state (validate Is.Ready.documentIs)

        /*
         * Validate presence (or non-presence)
         */
        // Check if we're validating a loader that must not be present
        if (loader.isPresent() && loader.get().mustBeGone() && selector.isPresent()) {
            log.warn("The element located by '" + selector.getLocator() +
                    "' is a loader that is required to be gone, but is present");
            return false;
        }

        // If it's not a loader, it must be at least present
        if (!loader.isPresent() && !selector.isPresent()) {
            log.warn("The element located by '" + selector.getLocator() + "' must be present, but is not");
            return false;
        }

        /*
         * Validate visibility (or invisibility)
         */
        // Check if we're validating a loader that must be invisible
        if (loader.isPresent() && loader.get().mustBeInvisible() && selector.isDisplayed()) {
            log.warn("The element located by '" + selector.getLocator() +
                    "' is a loader that is required to be invisible, but is visible");
            return false;
        }

        // If it's not a loader, and we need it to be visible
        if (!loader.isPresent() && ready.whenVisible() && !selector.isDisplayed()) {
            log.warn("The element located by '" + selector.getLocator() + "' must be visible, but is not");
            return false;
        }

        /*
         * At this point, we don't care if it's a loader anymore, we'll just validate regardless
         */

        /*
         * Validate Text
         */
        // Don't bother validating if text doesn't need to be validated
        if (!ready.whenTextContains().isEmpty()) {
            if (!selector.getText().contains(ready.whenTextContains())) {
                log.warn("The text of the element located by '" + selector.getLocator() +
                        "' does not contain the expected text: '" + ready.whenTextContains() +
                        "' The text of the element is: '" + selector.getText() + "'");
                return false;
            }
        }

        /*
         * Validate CSS classes
         */
        // Don't bother validating if CSS classes don't need to be validated
        if (ready.ifHasCssClasses().length > 0 && !ready.ifHasCssClasses()[0].isEmpty()) {
            if (!selector.hasCssClasses(ready.ifHasCssClasses())) {
                log.warn("The element located by '" + selector.getLocator() +
                        "' does not have all the expected CSS classes: " + Arrays.toString(ready.ifHasCssClasses()) +
                        "\nActual: " + Arrays.toString(selector.getCssClasses()));
                return false;
            }
        }

        /*
         * Validate Count
         */
        int count = selector.getMultiple().size();

        /* Count Exactly First */
        // Only need to validate if the count restriction is greater than 0
        if (ready.whenCountExactly() > 0) {
            if (count != ready.whenCountExactly()) {
                log.warn("Expected to find " + ready.whenCountExactly() + " elements located by '" +
                        selector.getLocator() + "', but found " + count);
            }
        } else { /* We didn't care about count exactly, let's check if we need to count at least or at most */
            // Only need to validate minimum count if restriction is greater than 0
            if (ready.whenCountAtLeast() > 0) {
                if (count < ready.whenCountAtLeast()) {
                    log.warn("Expected to find at least " + ready.whenCountAtLeast() + " elements located by '" +
                            selector.getLocator() + "', but found " + count);
                    return false;
                }
            }

            // Only need to validate maximum count if restriction is greater than 0
            if (ready.whenCountAtMost() > 0) {
                if (count > ready.whenCountAtMost()) {
                    log.warn("Expected to find at most " + ready.whenCountAtMost() + " elements located by '" +
                            selector.getLocator() + "', but found " + count);
                    return false;
                }
            }
        }
        // If we haven't returned false at this point, everything has validated
        return true;
    }

    public boolean validateComponent(AbstractComponent component, Is.Ready ready, Optional<Is.Loader> loader) {
        // Start by just dropping out here if we can't validate the container
        if (!validateSelector(component.getContainer(), ready, loader)) {
            return false;
        }

        validateLoadable(component);

        return true;
    }
}