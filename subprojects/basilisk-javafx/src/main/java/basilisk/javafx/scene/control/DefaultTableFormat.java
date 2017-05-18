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
package basilisk.javafx.scene.control;

import javafx.beans.value.ObservableValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static basilisk.util.BasiliskClassUtils.requireState;
import static basilisk.util.BasiliskNameUtils.getNaturalName;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.lang.System.arraycopy;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class DefaultTableFormat<E> implements TableViewFormat<E> {
    public static class Column<E, T> {
        private final String name;
        private final String title;
        private final Double size;
        private final TableCellFactory<E, T> tableCellFactory;

        public Column(@Nonnull String name) {
            this(name, getNaturalName(name), null, null);
        }

        public Column(@Nonnull String name, @Nonnull String title) {
            this(name, title, null, null);
        }

        public Column(@Nonnull String name, @Nonnull Double size) {
            this(name, getNaturalName(name), size, null);
        }

        public Column(@Nonnull String name, @Nonnull TableCellFactory<E, T> tableCellFactory) {
            this(name, getNaturalName(name), null, tableCellFactory);
        }

        public Column(@Nonnull String name, @Nonnull String title, @Nonnull Double size) {
            this(name, title, size, null);
        }

        public Column(@Nonnull String name, @Nonnull String title, @Nonnull TableCellFactory<E, T> tableCellFactory) {
            this(name, title, null, tableCellFactory);
        }

        public Column(@Nonnull String name, @Nonnull Double size, @Nonnull TableCellFactory<E, T> tableCellFactory) {
            this(name, getNaturalName(name), size, tableCellFactory);
        }

        public Column(@Nonnull String name, @Nonnull String title, @Nullable Double size, @Nonnull TableCellFactory<E, T> tableCellFactory) {
            this.name = requireNonBlank(name, "Argument 'name' must not be blank");
            this.title = requireNonBlank(title, "Argument 'title' must not be blank");
            if (size != null) {
                requireState(size > 0, "Argument 'size' must be greater than 0.0d");
                requireState(size <= 1, "Argument 'size' must be less than or equal to 1.0d");
            }
            this.size = size;
            this.tableCellFactory = tableCellFactory;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public String getTitle() {
            return title;
        }

        @Nullable
        public Double getSize() {
            return size;
        }

        @Nullable
        public TableCellFactory<E, T> getTableCellFactory() {
            return tableCellFactory;
        }
    }

    private final Column[] columns;
    private final Map<Class<?>, Map<String, Method>> observableMetadata = new LinkedHashMap<>();

    public DefaultTableFormat(@Nonnull String... names) {
        requireNonNull(names, "Argument 'names' must not be null");
        requireState(names.length > 0, "Column size must be greater than zero");
        this.columns = new Column[names.length];
        for (int i = 0; i < names.length; i++) {
            this.columns[i] = new Column(names[i]);
        }
    }

    public DefaultTableFormat(@Nonnull Column... columns) {
        requireNonNull(columns, "Argument 'columns' must not be null");
        requireState(columns.length > 0, "Column size must be greater than zero");
        this.columns = new Column[columns.length];
        arraycopy(columns, 0, this.columns, 0, columns.length);
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Nonnull
    @Override
    public String getColumnName(int index) {
        return columns[index].getTitle();
    }

    @Nullable
    @Override
    public Double getColumnSize(int index) {
        return columns[index].getSize();
    }

    @Nonnull
    @Override
    public ObservableValue<?> getObservableValue(@Nonnull E instance, int index) {
        final String columnName = columns[index].getName();
        Class<?> klass = instance.getClass();
        Map<String, Method> metadata = observableMetadata.computeIfAbsent(klass, new Function<Class<?>, Map<String, Method>>() {
            @Override
            public Map<String, Method> apply(Class<?> k) {
                return DefaultTableFormat.this.harvestMetadata(k);
            }
        });

        try {
            Method method = metadata.get(columnName);
            if (method == null) {
                throw new IllegalStateException("Could not find a method in " + klass +
                    " returning " + ObservableValue.class.getSimpleName() + " associated with column " + columnName);
            }
            return (ObservableValue<?>) method.invoke(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        }
    }

    @Nullable
    @Override
    public TableCellFactory getTableCellFactory(int index) {
        return columns[index].getTableCellFactory();
    }

    @Nonnull
    private Map<String, Method> harvestMetadata(@Nonnull Class<?> klass) {
        final Map<String, Method> map = new LinkedHashMap<>();

        for (Method method : klass.getMethods()) {
            final Method m = method;
            if (ObservableValue.class.isAssignableFrom(method.getReturnType()) &&
                method.getParameterCount() == 0) {
                Arrays.stream(columns)
                    .map(new Function<Column, String>() {
                        @Override
                        public String apply(Column column) {
                            return column.getName();
                        }
                    })
                    .filter(new Predicate<String>() {
                        @Override
                        public boolean test(String name) {
                            return m.getName().startsWith(name);
                        }
                    })
                    .findFirst()
                    .ifPresent(new Consumer<String>() {
                        @Override
                        public void accept(String name) {
                            map.put(name, m);
                        }
                    });
            }
        }

        return map;
    }
}
