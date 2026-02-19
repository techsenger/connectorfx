package com.techsenger.connectorfx.scenegraph.attributes;

import com.techsenger.connectorfx.LocalElement;
import com.techsenger.connectorfx.event.AttributeListEvent;
import com.techsenger.connectorfx.event.AttributeUpdatedEvent;
import com.techsenger.connectorfx.event.EventBus;
import com.techsenger.connectorfx.event.EventSource;
import com.techsenger.connectorfx.event.ExceptionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The tracker is the base class for reading scene graph node properties and monitoring
 * changes to those properties. Each individual property is wrapped in the corresponding
 * {@link Attribute} instance, which provides additional information on how to work with
 * the property. Any changes are emitted through the given {@link EventBus}.
 * <p>
 * It is also designed (though not yet implemented) to include additional logic for changing
 * the property values.
 * <p>
 * There are several specific implementations for common types in the JavaFX scene graph
 * hierarchy, as well as one generic implementation (the {@link ReflectiveTracker}), which
 * tries to obtain all available node properties via reflection.
 */
@NullMarked
public abstract sealed class Tracker permits
    ControlTracker,
    GridPaneTracker,
    LabeledTracker,
    ImageViewTracker,
    NodeTracker,
    ParentTracker,
    RegionTracker,
    ReflectiveTracker,
    SceneTracker,
    ShapeTracker,
    TextTracker,
    WindowTracker {

    private static final Logger logger = LoggerFactory.getLogger(Tracker.class);

    private final PropertyListener propertyListener = new PropertyListener() {
        @Override
        protected void onPropertyChanged(String propertyName, ObservableValue<?> obs) {
            // emit property changes (won't work for observable collections)
            reload(propertyName);
        }
    };

    private final EventBus eventBus;
    private final AttributeCategory category;
    private final EventSource eventSource;
    private @Nullable Object target;

    protected Tracker(EventBus eventBus,
                      EventSource eventSource,
                      AttributeCategory category) {
        this.eventBus = eventBus;
        this.eventSource = eventSource;
        this.category = category;
    }

    /**
     * Returns the tracker category, which indicates the type of scene graph node
     * with which this tracker works.
     */
    public AttributeCategory getCategory() {
        return category;
    }

    /**
     * Returns the target node that is being tracked.
     */
    public @Nullable Object getTarget() {
        return target;
    }

    /**
     * Sets or resets the target node to be tracked.
     */
    public void setTarget(@Nullable Object target) {
        if (target == null) {
            reset();
            return;
        }

        // reload (emit) all properties initially
        if (doSetTarget(target)) {
            reload();
        }
    }

    /**
     * Removes the tracked node, clears all listeners and emits the corresponding
     * {@link AttributeListEvent}. This is the equivalent of {@code setTarget(null)}.
     */
    public void reset() {
        // keep direct link to avoid NPE, there's some contention
        var t = target;

        if (t != null) {
            beforeResetTarget(t);
            doSetTarget(null);
            fireAttributeListEvent(List.of());
        }
    }

    /**
     * Reads all specified properties from the target node and emits the corresponding
     * events. If no arguments are passed, it reads and emits all properties of the target node.
     */
    public abstract void reload(String... properties);

    /**
     * Checks whether the given target can be accepted by the tracker implementation.
     */
    public abstract boolean accepts(@Nullable Object target);

    ///////////////////////////////////////////////////////////////////////////

    protected PropertyListener getPropertyListener() {
        return propertyListener;
    }

    /**
     * Replaces the current target with a new one.
     */
    protected boolean doSetTarget(@Nullable Object candidate) {
        if (target == candidate) {
            return false;
        }

        Object old = target;
        if (old != null) {
            propertyListener.release();
        }

        if (candidate != null) {
            try {
                propertyListener.use(candidate);
            } catch (InvocationTargetException | IllegalAccessException e) {
                eventBus.fire(ExceptionEvent.of(eventSource, e));
            }

            // properties must be available prior to running this method
            beforeSetTarget(candidate);
        }
        target = candidate;

        return true;
    }

    /**
     * If an implementation needs to refresh additional resources, it can include the logic here.
     * This method will be called prior to setting the current target to a new value.
     */
    protected void beforeSetTarget(Object target) {
        // pass
    }

    /**
     * If an implementation uses additional resources, it can include the cleanup logic here.
     * This method will be called prior to setting the current target to null.
     */
    protected void beforeResetTarget(Object target) {
        // pass
    }

    /**
     * A handy method to simplify reloading properties in implementations.
     */
    protected void reload(Function<String, @Nullable Attribute<?>> mapper,
                          Collection<String> supportedProperties,
                          String... properties) {
        if (properties.length == 0) { // hot path 1
            var attributes = new ArrayList<Attribute<?>>(properties.length);
            for (var property : supportedProperties) {
                Attribute<?> attr = mapper.apply(property);
                if (attr != null) {
                    attributes.add(attr);
                }
            }
            fireAttributeListEvent(attributes);
        } else if (properties.length == 1) { // hot path 2
            Attribute<?> attr = mapper.apply(properties[0]);
            if (attr != null) {
                fireAttributeUpdatedEvent(attr);
            }
        } else {
            Arrays.stream(properties)
                .map(mapper)
                .filter(Objects::nonNull)
                .forEach(this::fireAttributeUpdatedEvent);
        }
    }

    /**
     * Emits an {@link AttributeListEvent} based on the target type.
     */
    protected void fireAttributeListEvent(List<Attribute<?>> attributes) {
        if (target instanceof Node node) {
            eventBus.fire(
                new AttributeListEvent(eventSource, LocalElement.of(node), category, attributes)
            );
        } else if (target instanceof Window window) {
            eventBus.fire(
                new AttributeListEvent(eventSource, LocalElement.of(window, eventSource), category, attributes)
            );
        } else if (target instanceof Scene scene) {
            eventBus.fire(
                new AttributeListEvent(eventSource, LocalElement.of(scene.getWindow(), eventSource),
                        category, attributes)
            );
        } else if (target != null) {
            logger.warn("Unable to emit event: unknown object type '{}'", target.getClass());
        }
    }

    /**
     * Emits an {@link AttributeUpdatedEvent} based on the target type.
     */
    protected void fireAttributeUpdatedEvent(Attribute<?> attribute) {
        if (target instanceof Node node) {
            eventBus.fire(
                new AttributeUpdatedEvent(eventSource, LocalElement.of(node), category, attribute)
            );
        } else if (target instanceof Window window) {
            eventBus.fire(
                new AttributeUpdatedEvent(eventSource, LocalElement.of(window, eventSource), category, attribute)
            );
        } else if (target instanceof Scene scene) {
            eventBus.fire(
                new AttributeUpdatedEvent(eventSource, LocalElement.of(scene.getWindow(), eventSource),
                        category, attribute)
            );
        } else if (target != null) {
            logger.warn("Unable to emit event: unknown object type '{}'", target.getClass());
        }
    }
}
