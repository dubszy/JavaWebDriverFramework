package com.mwaltman.podriverframework.common.loadables.pagecomponents;

import com.mwaltman.podriverframework.common.loadables.AbstractLoadable;
import com.mwaltman.podriverframework.common.webdriver.Selector;

/**
 * Base class/component object that all component objects should extend.
 */
public abstract class AbstractComponent extends AbstractLoadable {

    /**
     * The page object or component object that encapsulates this component object.
     */
    private final AbstractLoadable owner;

    /**
     * The {@link Selector} that points to a web element on the page that is the
     * immediate parent to the entire component.
     */
    private final Selector container;

    /**
     * Construct a new instance of {@link AbstractComponent} with an owner and a container.
     *
     * @param owner The page object or component object that encapsulates this component object
     * @param container The Selector that points to a web element on the page that is the
     *                  immediate parent to the entire component
     *
     * @exception NullPointerException If {@code container} is null
     * @exception IllegalArgumentException If a call to {@link Selector#getLocator()} returns null or an empty string
     */
    public AbstractComponent(AbstractLoadable owner, Selector container) {
        super(owner.getSession());

        if (container == null) {
            throw new NullPointerException("The container for a component object cannot be null");
        }

        if (container.getLocator() == null || container.getLocator().isEmpty()) {
            throw new IllegalArgumentException("The container's locator for a component object cannot be null or empty");
        }

        this.owner = owner;
        this.container = container;
    }

    /**
     * Construct a new instance of {@link AbstractComponent} from another component.
     * @param anotherComponent
     */
    public AbstractComponent(AbstractComponent anotherComponent) {
        this(anotherComponent.owner, anotherComponent.container);
    }

    /**
     * Get the page object or component object that encapsulates this component object.
     * Must be overridden in all subclasses.
     *
     * @return The owning instance of a page object or component object that encapsulates
     *          this component object
     */
    public abstract AbstractLoadable getOwner();

    /**
     * Get the {@link Selector} that points to a web element on the page that is the
     * immediate parent to the entire component.
     *
     * @return This component's container
     */
    public Selector getContainer() {
        return container;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractComponent that = (AbstractComponent) o;

        return (owner != null ? owner.equals(that.owner) : that.owner == null) &&
                (container != null ? container.equals(that.container) : that.container == null);
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + (container != null ? container.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractComponent{" +
                "owner=" + owner +
                ", container=" + container +
                '}';
    }
}
