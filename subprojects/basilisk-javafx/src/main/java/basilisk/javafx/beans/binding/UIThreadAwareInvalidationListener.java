/*
 * Copyright 2008-2016 the original author or authors.
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

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;
import static javafx.application.Platform.isFxApplicationThread;
import static javafx.application.Platform.runLater;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
class UIThreadAwareInvalidationListener implements InvalidationListener, UIThreadAware {
    private final InvalidationListener delegate;

    UIThreadAwareInvalidationListener(@Nonnull InvalidationListener delegate) {
        this.delegate = requireNonNull(delegate, "Argument 'delegate' must not be null");
    }

    @Override
    public void invalidated(final Observable observable) {
        if (isFxApplicationThread()) {
            delegate.invalidated(observable);
        } else {
            runLater(new Runnable() {
                @Override
                public void run() {
                    UIThreadAwareInvalidationListener.this.invalidated(observable);
                }
            });
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" + delegate.toString();
    }
}
