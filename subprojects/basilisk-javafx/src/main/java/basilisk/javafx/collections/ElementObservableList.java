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
package basilisk.javafx.collections;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class ElementObservableList<E> extends DelegatingObservableList<E> {
    public interface PropertyContainer {
        @Nonnull
        Property<?>[] properties();
    }

    public interface PropertyExtractor<E> {
        @Nonnull
        Property<?>[] properties(@Nullable E instance);
    }

    private final Map<E, List<ListenerSubscription>> subscriptions = new LinkedHashMap<>();
    private final PropertyExtractor<E> propertyExtractor;

    public ElementObservableList() {
        this(FXCollections.<E>observableArrayList(), new DefaultPropertyExtractor<E>());
    }

    public ElementObservableList(@Nonnull PropertyExtractor<E> propertyExtractor) {
        this(FXCollections.<E>observableArrayList(), propertyExtractor);
    }

    public ElementObservableList(@Nonnull ObservableList<E> delegate) {
        this(delegate, new DefaultPropertyExtractor<E>());
    }

    public ElementObservableList(@Nonnull ObservableList<E> delegate, @Nonnull PropertyExtractor<E> propertyExtractor) {
        super(delegate);
        this.propertyExtractor = requireNonNull(propertyExtractor, "Argument 'propertyExtractor' must not be null");
    }

    @Override
    protected void sourceChanged(@Nonnull ListChangeListener.Change<? extends E> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                for (E element : c.getAddedSubList()) {
                    registerListeners(element);
                }
            } else if (c.wasRemoved()) {
                for (E element : c.getRemoved()) {
                    unregisterListeners(element);
                }
            }
        }
        fireChange(c);
    }

    private void registerListeners(@Nonnull E element) {
        if (subscriptions.containsKey(element)) {
            return;
        }

        List<ListenerSubscription> elementSubscriptions = new ArrayList<>();
        for (Property<?> property : propertyExtractor.properties(element)) {
            elementSubscriptions.add(createChangeListener(element, property));
        }
        subscriptions.put(element, elementSubscriptions);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private ListenerSubscription createChangeListener(@Nonnull final E element, @Nonnull final Property<?> property) {
        final ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                ElementObservableList.this.fireChange(ElementObservableList.this.changeFor(element));
            }
        };
        property.addListener(listener);
        return new ListenerSubscription() {
            @Override
            public void unsubscribe() {
                property.removeListener(listener);
            }
        };
    }

    @Nonnull
    private ListChangeListener.Change<? extends E> changeFor(@Nonnull final E contact) {
        final int position = indexOf(contact);
        final int[] permutations = new int[0];

        return new ListChangeListener.Change<E>(this) {
            private boolean invalid = true;

            @Override
            public boolean next() {
                if (invalid) {
                    invalid = false;
                    return true;
                }
                return false;
            }

            @Override
            public void reset() {
                invalid = true;
            }

            @Override
            public int getFrom() {
                return position;
            }

            @Override
            public int getTo() {
                return position + 1;
            }

            @Override
            public List<E> getRemoved() {
                return Collections.emptyList();
            }

            @Override
            protected int[] getPermutation() {
                return permutations;
            }

            @Override
            public boolean wasUpdated() {
                return true;
            }
        };
    }

    private void unregisterListeners(@Nonnull E contact) {
        List<ListenerSubscription> registeredSubscriptions = subscriptions.remove(contact);
        if (registeredSubscriptions != null) {
            for (ListenerSubscription subscription : registeredSubscriptions) {
                subscription.unsubscribe();
            }
        }
    }


    private interface ListenerSubscription {
        void unsubscribe();
    }
}
