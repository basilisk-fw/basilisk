/*
 * Copyright 2008-2015 the original author or authors.
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
package basilisk.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Andres Almiray
 */
public interface Context {
    /**
     * Searches for the key in this context and its hierarchy.
     *
     * @param key the key to search
     * @return true if the context (or its parent) contains the given key, false otherwise
     */
    boolean containsKey(@Nonnull String key);

    /**
     * Searches for the key in this context only.
     *
     * @param key the key to search
     * @return true if the context contains the given key, false otherwise
     */
    boolean hasKey(@Nonnull String key);

    /**
     * Removes a key from this context. Does not affect the context's hierarchy.
     *
     * @param key the key to be removed
     * @return the value associated with the key or <tt>null</tt> if there wasn't any value.
     */
    @Nullable
    Object remove(@Nonnull String key);

    /**
     * Removes a key from this context. Does not affect the context's hierarchy.
     * Blindly casts the returned value.
     *
     * @param key the key to be removed
     * @return the value associated with the key or <tt>null</tt> if there wasn't any value.
     */
    @Nullable
    <T> T removeAs(@Nonnull String key);

    /**
     * Removes a key from this context. Does not affect the context's hierarchy. The value is
     * converted to type <tt>T</tt> if found using a {@code PropertyEditor}.
     *
     * @param key  the key to be removed
     * @param type the type to be returned
     * @return the value associated with the key or <tt>null</tt> if there wasn't any value.
     */
    @Nullable
    <T> T removeConverted(@Nonnull String key, @Nonnull Class<T> type);

    /**
     * Sets a key/value pair on this context. If the context has a parent and if the
     * key matches a parent key then the value will shadow the parent's, that is, the parent
     * value will not be overwritten.
     *
     * @param key   the key to be registered
     * @param value the value to save
     */
    void put(@Nonnull String key, @Nullable Object value);

    /**
     * Sets a key/value pair on this context. If the context has a parent and if the
     * key matches a parent key then the value will shadow the parent's, that is, the parent
     * value will not be overwritten.
     * Convenience method to use in Groovy aware environments.
     *
     * @param key   the key to be registered
     * @param value the value to save
     */
    void putAt(@Nonnull String key, @Nullable Object value);

    /**
     * Returns the value associated with the given key. This operation will traverse
     * up the context hierarchy until it finds a key.
     *
     * @param key the key to search
     * @return the value associated with the key or <tt>null<</tt> if not found.
     */
    @Nullable
    Object get(@Nonnull String key);

    /**
     * Returns the value associated with the given key. This operation will traverse
     * up the context hierarchy until it finds a key.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key was not found
     * @param <T>          the type of the value
     * @return returns the value associated with the key, <tt>defaultValue</tt> if the key was not found
     */
    @Nullable
    <T> T get(@Nonnull String key, @Nullable T defaultValue);

    /**
     * Returns the value associated with the given key. This operation will traverse
     * up the context hierarchy until it finds a key.
     * Convenience method to use in Groovy aware environments.
     *
     * @param key the key to search
     * @return the value associated with the key or <tt>null<</tt> if not found.
     */
    @Nullable
    Object getAt(@Nonnull String key);

    /**
     * Returns the value associated with the given key. This operation will traverse
     * up the context hierarchy until it finds a key.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key was not found
     * @param <T>          the type of the value
     * @return returns the value associated with the key, <tt>defaultValue</tt> if the key was not found
     */
    @Nullable
    <T> T getAt(@Nonnull String key, @Nullable T defaultValue);

    /**
     * Destroys this context. Once destroyed a context should not be used anymore.
     */
    void destroy();

    /**
     * Returns the parent {@code Context} if it exists.
     */
    @Nullable
    Context getParentContext();

    /**
     * Returns a {@link Set} view of the keys contained in this context.
     *
     * @return a set view of the keys contained in this map
     */
    @Nonnull
    Set<String> keySet();

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>boolean</tt> if found.
     *
     * @param key the key to search
     */
    boolean getAsBoolean(@Nonnull String key);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>boolean</tt> if found. If not found then the
     * supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key is not found
     */
    boolean getAsBoolean(@Nonnull String key, boolean defaultValue);

    /**
     * Finds a value associated with the given key. The value is
     * converted to an <tt>int</tt> if found.
     *
     * @param key the key to search
     */
    int getAsInt(@Nonnull String key);

    /**
     * Finds a value associated with the given key. The value is
     * converted to an <tt>int</tt> if found. If not found then the
     * supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key is not found
     */
    int getAsInt(@Nonnull String key, int defaultValue);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>long</tt> if found.
     *
     * @param key the key to search
     */
    long getAsLong(@Nonnull String key);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>long</tt> if found. If not found then the
     * supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key is not found
     */
    long getAsLong(@Nonnull String key, long defaultValue);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>float</tt> if found.
     *
     * @param key the key to search
     */
    float getAsFloat(@Nonnull String key);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>float</tt> if found. If not found then the
     * supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key is not found
     */
    float getAsFloat(@Nonnull String key, float defaultValue);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>double</tt> if found.
     *
     * @param key the key to search
     */
    double getAsDouble(@Nonnull String key);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>double</tt> if found. If not found then the
     * supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key is not found
     */
    double getAsDouble(@Nonnull String key, double defaultValue);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>String</tt> if found.
     *
     * @param key the key to search
     */
    @Nullable
    String getAsString(@Nonnull String key);

    /**
     * Finds a value associated with the given key. The value is
     * converted to a <tt>String</tt> if found. If not found then the
     * supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key is not found
     */
    @Nullable
    String getAsString(@Nonnull String key, @Nullable String defaultValue);

    /**
     * /**
     * Finds a value associated with the given key. The value is
     * blindly cast to type <tt>T</tt> if found.
     *
     * @param key the key to search
     */
    @Nullable
    <T> T getAs(@Nonnull String key);

    /**
     * Finds a value associated with the given key. The value is
     * blindly cast to type <tt>T</tt> if found. If not found then the
     * supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param defaultValue the value to be returned if the key is not found
     */
    @Nullable
    <T> T getAs(@Nonnull String key, @Nullable T defaultValue);

    /**
     * /**
     * Finds a value associated with the given key. The value is
     * converted to type <tt>T</tt> if found using a {@code PropertyEditor}.
     *
     * @param key  the key to search
     * @param type the type to be returned
     */
    @Nullable
    <T> T getConverted(@Nonnull String key, @Nonnull Class<T> type);

    /**
     * Finds a value associated with the given key. The value is
     * converted to type <tt>T</tt> if found using a {@code PropertyEditor}.
     * If not found then the supplied <tt>defaultValue</tt> will be returned.
     *
     * @param key          the key to search
     * @param type         the type to be returned
     * @param defaultValue the value to be returned if the key is not found
     */
    @Nullable
    <T> T getConverted(@Nonnull String key, @Nonnull Class<T> type, @Nullable T defaultValue);

    /**
     * Inject properties and members annotated with {@code basilisk.inject.Contextal}.
     *
     * @param instance the instance on which contextual members will be injected.
     * @param <T>      the type of the instance
     * @return the instance on which contextual members where injected.
     */
    @Nonnull
    <T> T injectMembers(@Nonnull T instance);
}
