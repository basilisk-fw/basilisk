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

import javafx.collections.SetChangeListener;

import javax.annotation.Nonnull;

import static javafx.application.Platform.isFxApplicationThread;
import static javafx.application.Platform.runLater;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
class UIThreadAwareSetChangeListener<E> extends SetChangeListenerDecorator<E> implements UIThreadAware {
    UIThreadAwareSetChangeListener(@Nonnull SetChangeListener<E> delegate) {
        super(delegate);
    }

    @Override
    public void onChanged(final Change<? extends E> change) {
        if (isFxApplicationThread()) {
            getDelegate().onChanged(change);
        } else {
            runLater(new Runnable() {
                @Override
                public void run() {
                    UIThreadAwareSetChangeListener.this.getDelegate().onChanged(change);
                }
            });
        }
    }
}
