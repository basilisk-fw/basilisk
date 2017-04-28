/*
 * Copyright 2008-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package basilisk.javafx.beans.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class UIThreadAwareBindings {
    private static final String ERROR_LISTENER_NULL = "Argument 'listener' must not be null";
    private static final String ERROR_CONSUMER_NULL = "Argument 'consumer' must not be null";
    private static final String ERROR_RUNNABLE_NULL = "Argument 'runnable' must not be null";
    private static final String ERROR_OBSERVABLE_NULL = "Argument 'observable' must not be null";
    private static final String ERROR_PROPERTY_NULL = "Argument 'property' must not be null";

    private UIThreadAwareBindings() {
        // prevent instantiation
    }

    /**
     * Registers a {@code ChangeListener} on the supplied observable that will notify the target property.
     *
     * @param property   the property that will be notified of value changes.
     * @param observable the observable on which the listener will be registered.
     *
     * @return the wrapped change listener.
     *
     * @since 1.0.0
     */
    public static <T> ChangeListener<T> uiThreadAwareBind(@Nonnull final Property<T> property, @Nonnull final ObservableValue<T> observable) {
        requireNonNull(property, ERROR_PROPERTY_NULL);
        property.setValue(observable.getValue());
        ChangeListener<T> listener = new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> v, T o, T n) {
                property.setValue(n);
            }
        };
        return uiThreadAwareChangeListener(observable, listener);
    }

    /**
     * Registers a {@code ChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param listener   the wrapped change listener.
     *
     * @return a {@code ChangeListener}.
     */
    public static <T> ChangeListener<T> uiThreadAwareChangeListener(@Nonnull final ObservableValue<T> observable, @Nonnull ChangeListener<T> listener) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        ChangeListener<T> uiListener = uiThreadAwareChangeListener(listener);
        observable.addListener(uiListener);
        return uiListener;
    }

    /**
     * Creates a {@code ChangeListener} that always handles notifications inside the UI thread.
     *
     * @param listener the wrapped change listener.
     *
     * @return a {@code ChangeListener}.
     */
    @Nonnull
    public static <T> ChangeListener<T> uiThreadAwareChangeListener(@Nonnull ChangeListener<T> listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        return listener instanceof UIThreadAware ? listener : new UIThreadAwareChangeListener<T>(listener);
    }

    /**
     * Registers a {@code ChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param consumer   the consumer of the {@code newValue} argument.
     *
     * @return a {@code ChangeListener}.
     */
    public static <T> ChangeListener<T> uiThreadAwareChangeListener(@Nonnull final ObservableValue<T> observable, @Nonnull final Consumer<T> consumer) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        ChangeListener<T> listener = uiThreadAwareChangeListener(consumer);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code ChangeListener} that always handles notifications inside the UI thread.
     *
     * @param consumer the consumer of the {@code newValue} argument.
     *
     * @return a {@code ChangeListener}.
     */
    @Nonnull
    public static <T> ChangeListener<T> uiThreadAwareChangeListener(@Nonnull final Consumer<T> consumer) {
        requireNonNull(consumer, ERROR_CONSUMER_NULL);
        return new UIThreadAwareChangeListener<>(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                consumer.accept(newValue);
            }
        });
    }

    /**
     * Registers a {@code ChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param runnable   the code to be executed when the listener is notified.
     *
     * @return a {@code ChangeListener}.
     */
    public static <T> ChangeListener<T> uiThreadAwareChangeListener(@Nonnull final ObservableValue<T> observable, @Nonnull final Runnable runnable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        ChangeListener<T> listener = uiThreadAwareChangeListener(runnable);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code ChangeListener} that always handles notifications inside the UI thread.
     *
     * @param runnable the code to be executed when the listener is notified.
     *
     * @return a {@code ChangeListener}.
     */
    @Nonnull
    public static <T> ChangeListener<T> uiThreadAwareChangeListener(@Nonnull final Runnable runnable) {
        requireNonNull(runnable, ERROR_RUNNABLE_NULL);
        return new UIThreadAwareChangeListener<>(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                runnable.run();
            }
        });
    }

    /**
     * Registers a {@code InvalidationListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param listener   the wrapped invalidation listener.
     *
     * @return an {@code InvalidationListener}.
     */
    public static InvalidationListener uiThreadAwareInvalidationListener(@Nonnull final Observable observable, @Nonnull InvalidationListener listener) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        InvalidationListener uiListener = uiThreadAwareInvalidationListener(listener);
        observable.addListener(uiListener);
        return uiListener;
    }

    /**
     * Creates a {@code InvalidationListener} that always handles notifications inside the UI thread.
     *
     * @param listener the wrapped invalidation listener.
     *
     * @return a {@code InvalidationListener}.
     */
    @Nonnull
    public static InvalidationListener uiThreadAwareInvalidationListener(@Nonnull InvalidationListener listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        return listener instanceof UIThreadAware ? listener : new UIThreadAwareInvalidationListener(listener);
    }

    /**
     * Registers a {@code InvalidationListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param consumer   the consumer of the {@code observable} argument.
     *
     * @return a {@code InvalidationListener}.
     */
    public static InvalidationListener uiThreadAwareInvalidationListener(@Nonnull final Observable observable, @Nonnull final Consumer<Observable> consumer) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        InvalidationListener listener = uiThreadAwareInvalidationListener(consumer);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code InvalidationListener} that always handles notifications inside the UI thread.
     *
     * @param consumer the consumer of the {@code observable} argument.
     *
     * @return a {@code InvalidationListener}.
     */
    @Nonnull
    public static InvalidationListener uiThreadAwareInvalidationListener(@Nonnull final Consumer<Observable> consumer) {
        requireNonNull(consumer, ERROR_CONSUMER_NULL);
        return new UIThreadAwareInvalidationListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable t) {
                consumer.accept(t);
            }
        });
    }

    /**
     * Registers a {@code InvalidationListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param runnable   the code to be executed when the listener is notified.
     *
     * @return a {@code InvalidationListener}.
     */
    public static InvalidationListener uiThreadAwareInvalidationListener(@Nonnull final Observable observable, @Nonnull final Runnable runnable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        InvalidationListener listener = uiThreadAwareInvalidationListener(runnable);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code InvalidationListener} that always handles notifications inside the UI thread.
     *
     * @param runnable the code to be executed when the listener is notified.
     *
     * @return a {@code InvalidationListener}.
     */
    @Nonnull
    public static InvalidationListener uiThreadAwareInvalidationListener(@Nonnull final Runnable runnable) {
        requireNonNull(runnable, ERROR_RUNNABLE_NULL);
        return new UIThreadAwareInvalidationListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                runnable.run();
            }
        });
    }

    /**
     * Registers a {@code ListChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param listener   the wrapped list change listener.
     *
     * @return a {@code ListChangeListener}.
     */
    public static <E> ListChangeListener<E> uiThreadAwareListChangeListener(@Nonnull final ObservableList<E> observable, @Nonnull ListChangeListener<E> listener) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        ListChangeListener<E> uiListener = uiThreadAwareListChangeListener(listener);
        observable.addListener(uiListener);
        return listener;
    }

    /**
     * Creates a {@code ListChangeListener} that always handles notifications inside the UI thread.
     *
     * @param listener the wrapped list change listener.
     *
     * @return a {@code ListChangeListener}.
     */
    @Nonnull
    public static <E> ListChangeListener<E> uiThreadAwareListChangeListener(@Nonnull ListChangeListener<E> listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        return listener instanceof UIThreadAware ? listener : new UIThreadAwareListChangeListener<E>(listener);
    }

    /**
     * Registers a {@code ListChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param consumer   the consumer of the {@code newValue} argument.
     *
     * @return a {@code ListChangeListener}.
     */
    public static <E> ListChangeListener<E> uiThreadAwareListChangeListener(@Nonnull final ObservableList<E> observable, @Nonnull final Consumer<ListChangeListener.Change<? extends E>> consumer) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        ListChangeListener<E> listener = uiThreadAwareListChangeListener(consumer);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code ListChangeListener} that always handles notifications inside the UI thread.
     *
     * @param consumer the consumer of the {@code change} argument.
     *
     * @return a {@code ListChangeListener}.
     */
    @Nonnull
    public static <E> ListChangeListener<E> uiThreadAwareListChangeListener(@Nonnull final Consumer<ListChangeListener.Change<? extends E>> consumer) {
        requireNonNull(consumer, ERROR_CONSUMER_NULL);
        return new UIThreadAwareListChangeListener<E>(new ListChangeListener<E>() {
            @Override
            public void onChanged(Change<? extends E> t) {
                consumer.accept(t);
            }
        });
    }

    /**
     * Registers a {@code ListChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param runnable   the code to be executed when the listener is notified.
     *
     * @return a {@code ListChangeListener}.
     */
    public static <E> ListChangeListener<E> uiThreadAwareListChangeListener(@Nonnull final ObservableList<E> observable, @Nonnull final Runnable runnable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        ListChangeListener<E> listener = uiThreadAwareListChangeListener(runnable);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code ListChangeListener} that always handles notifications inside the UI thread.
     *
     * @param runnable the code to be executed when the listener is notified.
     *
     * @return a {@code ListChangeListener}.
     */
    @Nonnull
    public static <E> ListChangeListener<E> uiThreadAwareListChangeListener(@Nonnull final Runnable runnable) {
        requireNonNull(runnable, ERROR_RUNNABLE_NULL);
        return new UIThreadAwareListChangeListener<>(new ListChangeListener<E>() {
            @Override
            public void onChanged(Change<? extends E> change) {
                runnable.run();
            }
        });
    }

    /**
     * Registers a {@code MapChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param listener   the wrapped map change listener.
     *
     * @return a {@code MapChangeListener}.
     */
    public static <K, V> MapChangeListener<K, V> uiThreadAwareMapChangeListener(@Nonnull final ObservableMap<K, V> observable, @Nonnull MapChangeListener<K, V> listener) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        MapChangeListener<K, V> uiListener = uiThreadAwareMapChangeListener(listener);
        observable.addListener(uiListener);
        return listener;
    }

    /**
     * Creates a {@code MapChangeListener} that always handles notifications inside the UI thread.
     *
     * @param listener the wrapped map change listener.
     *
     * @return a {@code MapChangeListener}.
     */
    @Nonnull
    public static <K, V> MapChangeListener<K, V> uiThreadAwareMapChangeListener(@Nonnull MapChangeListener<K, V> listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        return listener instanceof UIThreadAware ? listener : new UIThreadAwareMapChangeListener<K, V>(listener);
    }

    /**
     * Registers a {@code MapChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param consumer   the consumer of the {@code newValue} argument.
     *
     * @return a {@code MapChangeListener}.
     */
    public static <K, V> MapChangeListener<K, V> uiThreadAwareMapChangeListener(@Nonnull final ObservableMap<K, V> observable, @Nonnull final Consumer<MapChangeListener.Change<? extends K, ? extends V>> consumer) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        MapChangeListener<K, V> listener = uiThreadAwareMapChangeListener(consumer);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code MapChangeListener} that always handles notifications inside the UI thread.
     *
     * @param consumer the consumer of the {@code change} argument.
     *
     * @return a {@code MapChangeListener}.
     */
    @Nonnull
    public static <K, V> MapChangeListener<K, V> uiThreadAwareMapChangeListener(@Nonnull final Consumer<MapChangeListener.Change<? extends K, ? extends V>> consumer) {
        requireNonNull(consumer, ERROR_CONSUMER_NULL);
        return new UIThreadAwareMapChangeListener<K, V>(new MapChangeListener<K, V>() {
            @Override
            public void onChanged(Change<? extends K, ? extends V> t) {
                consumer.accept(t);
            }
        });
    }

    /**
     * Registers a {@code MapChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param runnable   the code to be executed when the listener is notified.
     *
     * @return a {@code MapChangeListener}.
     */
    public static <K, V> MapChangeListener<K, V> uiThreadAwareMapChangeListener(@Nonnull final ObservableMap<K, V> observable, @Nonnull final Runnable runnable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        MapChangeListener<K, V> listener = uiThreadAwareMapChangeListener(runnable);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code MapChangeListener} that always handles notifications inside the UI thread.
     *
     * @param runnable the code to be executed when the listener is notified.
     *
     * @return a {@code MapChangeListener}.
     */
    @Nonnull
    public static <K, V> MapChangeListener<K, V> uiThreadAwareMapChangeListener(@Nonnull final Runnable runnable) {
        requireNonNull(runnable, ERROR_RUNNABLE_NULL);
        return new UIThreadAwareMapChangeListener<>(new MapChangeListener<K, V>() {
            @Override
            public void onChanged(Change<? extends K, ? extends V> change) {
                runnable.run();
            }
        });
    }

    /**
     * Registers a {@code SetChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param listener   the wrapped set change listener.
     *
     * @return a {@code SetChangeListener}.
     */
    public static <E> SetChangeListener<E> uiThreadAwareSetChangeListener(@Nonnull final ObservableSet<E> observable, @Nonnull SetChangeListener<E> listener) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        SetChangeListener<E> uiListener = uiThreadAwareSetChangeListener(listener);
        observable.addListener(uiListener);
        return listener;
    }

    /**
     * Creates a {@code SetChangeListener} that always handles notifications inside the UI thread.
     *
     * @param listener the wrapped set change listener.
     *
     * @return a {@code SetChangeListener}.
     */
    @Nonnull
    public static <E> SetChangeListener<E> uiThreadAwareSetChangeListener(@Nonnull SetChangeListener<E> listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        return listener instanceof UIThreadAware ? listener : new UIThreadAwareSetChangeListener<E>(listener);
    }

    /**
     * Registers a {@code SetChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param consumer   the consumer of the {@code newValue} argument.
     *
     * @return a {@code SetChangeListener}.
     */
    public static <E> SetChangeListener<E> uiThreadAwareSetChangeListener(@Nonnull final ObservableSet<E> observable, @Nonnull final Consumer<SetChangeListener.Change<? extends E>> consumer) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        SetChangeListener<E> listener = uiThreadAwareSetChangeListener(consumer);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code SetChangeListener} that always handles notifications inside the UI thread.
     *
     * @param consumer the consumer of the {@code change} argument.
     *
     * @return a {@code SetChangeListener}.
     */
    @Nonnull
    public static <E> SetChangeListener<E> uiThreadAwareSetChangeListener(@Nonnull final Consumer<SetChangeListener.Change<? extends E>> consumer) {
        requireNonNull(consumer, ERROR_CONSUMER_NULL);
        return new UIThreadAwareSetChangeListener<E>(new SetChangeListener<E>() {
            @Override
            public void onChanged(Change<? extends E> t) {
                consumer.accept(t);
            }
        });
    }

    /**
     * Registers a {@code SetChangeListener} that always handles notifications inside the UI thread.
     *
     * @param observable the observable on which the listener will be registered.
     * @param runnable   the code to be executed when the listener is notified.
     *
     * @return a {@code SetChangeListener}.
     */
    public static <E> SetChangeListener<E> uiThreadAwareSetChangeListener(@Nonnull final ObservableSet<E> observable, @Nonnull final Runnable runnable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        SetChangeListener<E> listener = uiThreadAwareSetChangeListener(runnable);
        observable.addListener(listener);
        return listener;
    }

    /**
     * Creates a {@code SetChangeListener} that always handles notifications inside the UI thread.
     *
     * @param runnable the code to be executed when the listener is notified.
     *
     * @return a {@code SetChangeListener}.
     */
    @Nonnull
    public static <E> SetChangeListener<E> uiThreadAwareSetChangeListener(@Nonnull final Runnable runnable) {
        requireNonNull(runnable, ERROR_RUNNABLE_NULL);
        return new UIThreadAwareSetChangeListener<>(new SetChangeListener<E>() {
            @Override
            public void onChanged(Change<? extends E> change) {
                runnable.run();
            }
        });
    }

    /**
     * Creates an observable boolean property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable boolean property to wrap.
     *
     * @return an observable boolean property.
     */
    @Nonnull
    public static BooleanProperty uiThreadAwareBooleanProperty(@Nonnull BooleanProperty observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareBooleanProperty(observable);
    }

    /**
     * Creates an observable integer property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable integer property to wrap.
     *
     * @return an observable integer property.
     */
    @Nonnull
    public static IntegerProperty uiThreadAwareIntegerProperty(@Nonnull IntegerProperty observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareIntegerProperty(observable);
    }

    /**
     * Creates an observable long property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable long property to wrap.
     *
     * @return an observable long property.
     */
    @Nonnull
    public static LongProperty uiThreadAwareLongProperty(@Nonnull LongProperty observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareLongProperty(observable);
    }

    /**
     * Creates an observable float property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable float property to wrap.
     *
     * @return an observable float property.
     */
    @Nonnull
    public static FloatProperty uiThreadAwareFloatProperty(@Nonnull FloatProperty observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareFloatProperty(observable);
    }

    /**
     * Creates an observable double property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable double property to wrap.
     *
     * @return an observable double property.
     */
    @Nonnull
    public static DoubleProperty uiThreadAwareDoubleProperty(@Nonnull DoubleProperty observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareDoubleProperty(observable);
    }

    /**
     * Creates an observable string property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable string property to wrap.
     *
     * @return an observable string property.
     */
    @Nonnull
    public static StringProperty uiThreadAwareStringProperty(@Nonnull StringProperty observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareStringProperty(observable);
    }

    /**
     * Creates an observable boolean property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable boolean property to wrap.
     *
     * @return an observable boolean property.
     */
    @Nonnull
    public static Property<Boolean> uiThreadAwarePropertyBoolean(@Nonnull Property<Boolean> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwarePropertyBoolean(observable);
    }

    /**
     * Creates an observable integer property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable integer property to wrap.
     *
     * @return an observable integer property.
     */
    @Nonnull
    public static Property<Integer> uiThreadAwarePropertyInteger(@Nonnull Property<Integer> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwarePropertyInteger(observable);
    }

    /**
     * Creates an observable long property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable long property to wrap.
     *
     * @return an observable long property.
     */
    @Nonnull
    public static Property<Long> uiThreadAwarePropertyLong(@Nonnull Property<Long> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwarePropertyLong(observable);
    }

    /**
     * Creates an observable float property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable float property to wrap.
     *
     * @return an observable float property.
     */
    @Nonnull
    public static Property<Float> uiThreadAwarePropertyFloat(@Nonnull Property<Float> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwarePropertyFloat(observable);
    }

    /**
     * Creates an observable double property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable double property to wrap.
     *
     * @return an observable double property.
     */
    @Nonnull
    public static Property<Double> uiThreadAwarePropertyDouble(@Nonnull Property<Double> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwarePropertyDouble(observable);
    }

    /**
     * Creates an observable string property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable string property to wrap.
     *
     * @return an observable string property.
     */
    @Nonnull
    public static Property<String> uiThreadAwarePropertyString(@Nonnull Property<String> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwarePropertyString(observable);
    }

    /**
     * Creates an observable object property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable object property to wrap.
     *
     * @return an observable object property.
     */
    @Nonnull
    public static <T> ObjectProperty<T> uiThreadAwareObjectProperty(@Nonnull final ObjectProperty<T> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObjectProperty<>(observable);
    }

    /**
     * Creates an observable list property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable list property to wrap.
     *
     * @return an observable list property.
     */
    @Nonnull
    public static <E> ListProperty<E> uiThreadAwareListProperty(@Nonnull ListProperty<E> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareListProperty<>(observable);
    }

    /**
     * Creates an observable set property that notifies its seteners inside the UI thread.
     *
     * @param observable the observable set property to wrap.
     *
     * @return an observable set property.
     */
    @Nonnull
    public static <E> SetProperty<E> uiThreadAwareSetProperty(@Nonnull SetProperty<E> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareSetProperty<>(observable);
    }

    /**
     * Creates an observable map property that notifies its listeners inside the UI thread.
     *
     * @param observable the observable map property to wrap.
     *
     * @return an observable map property.
     */
    @Nonnull
    public static <K, V> MapProperty<K, V> uiThreadAwareMapProperty(@Nonnull MapProperty<K, V> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareMapProperty<>(observable);
    }

    /**
     * Creates an observable value that notifies its listeners inside the UI thread.
     *
     * @param observable the observable to wrap.
     *
     * @return an observable value.
     */
    @Nonnull
    public static <T> ObservableValue<T> uiThreadAwareObservable(@Nonnull final ObservableValue<T> observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObservableValue<>(observable);
    }

    /**
     * Creates an observable string value that notifies its listeners inside the UI thread.
     *
     * @param observable the observable string to wrap.
     *
     * @return an observable string value.
     */
    @Nonnull
    public static ObservableStringValue uiThreadAwareObservableString(@Nonnull final ObservableStringValue observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObservableStringValue(observable);
    }

    /**
     * Creates an observable boolean value that notifies its listeners inside the UI thread.
     *
     * @param observable the observable boolean to wrap.
     *
     * @return an observable boolean value.
     */
    @Nonnull
    public static ObservableBooleanValue uiThreadAwareObservableBoolean(@Nonnull final ObservableBooleanValue observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObservableBooleanValue(observable);
    }

    /**
     * Creates an observable integer value that notifies its listeners inside the UI thread.
     *
     * @param observable the observable integer to wrap.
     *
     * @return an observable integer value.
     */
    @Nonnull
    public static ObservableIntegerValue uiThreadAwareObservableInteger(@Nonnull final ObservableIntegerValue observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObservableIntegerValue(observable);
    }

    /**
     * Creates an observable long value that notifies its listeners inside the UI thread.
     *
     * @param observable the observable long to wrap.
     *
     * @return an observable long value.
     */
    @Nonnull
    public static ObservableLongValue uiThreadAwareObservableLong(@Nonnull final ObservableLongValue observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObservableLongValue(observable);
    }

    /**
     * Creates an observable float value that notifies its listeners inside the UI thread.
     *
     * @param observable the observable float to wrap.
     *
     * @return an observable float value.
     */
    @Nonnull
    public static ObservableFloatValue uiThreadAwareObservableFloat(@Nonnull final ObservableFloatValue observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObservableFloatValue(observable);
    }

    /**
     * Creates an observable double value that notifies its listeners inside the UI thread.
     *
     * @param observable the observable double to wrap.
     *
     * @return an observable double value.
     */
    @Nonnull
    public static ObservableDoubleValue uiThreadAwareObservableDouble(@Nonnull final ObservableDoubleValue observable) {
        requireNonNull(observable, ERROR_OBSERVABLE_NULL);
        return observable instanceof UIThreadAware ? observable : new UIThreadAwareObservableDoubleValue(observable);
    }
}