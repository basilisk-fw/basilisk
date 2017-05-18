/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package basilisk.javafx.collections.transformation;

import basilisk.javafx.collections.ObservableMapBase;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.WeakMapChangeListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.00
 */
public abstract class TransformationMap<K, V, F> extends ObservableMapBase<K, V> implements ObservableMap<K, V> {
    private final ObservableMap<K, ? extends F> source;
    private MapChangeListener<K, F> sourceListener;

    protected TransformationMap(@Nonnull ObservableMap<K, ? extends F> source) {
        this.source = requireNonNull(source, "Argument 'source' must not be null");
        source.addListener(new WeakMapChangeListener<>(getListener()));
    }

    @Nonnull
    public final ObservableMap<K, ? extends F> getSource() {
        return source;
    }

    public MapChangeListener<K, F> getListener() {
        if (sourceListener == null) {
            sourceListener = new MapChangeListener<K, F>() {
                @Override
                public void onChanged(Change<? extends K, ? extends F> c) {
                    TransformationMap.this.sourceChanged(c);
                }
            };
        }
        return sourceListener;
    }

    public final boolean isInTransformationChain(@Nullable ObservableMap<?, ?> map) {
        if (source == map) {
            return true;
        }
        Map<?, ?> currentSource = source;
        while (currentSource instanceof TransformationMap) {
            currentSource = ((TransformationMap) currentSource).source;
            if (currentSource == map) {
                return true;
            }
        }
        return false;
    }

    protected abstract void sourceChanged(MapChangeListener.Change<? extends K, ? extends F> c);
}
