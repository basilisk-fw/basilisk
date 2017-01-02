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
package org.kordamp.basilisk.runtime.core.resources;

import basilisk.core.resources.NoSuchResourceException;
import basilisk.util.CompositeResourceBundleBuilder;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultResourceResolver extends AbstractResourceResolver {
    private final String basename;
    private final Map<Locale, ResourceBundle> bundles = new ConcurrentHashMap<>();
    private final CompositeResourceBundleBuilder compositeResourceBundleBuilder;

    public DefaultResourceResolver(@Nonnull CompositeResourceBundleBuilder builder, @Nonnull String basename) {
        this.compositeResourceBundleBuilder = requireNonNull(builder, "Argument 'builder' must not be null");
        this.basename = requireNonBlank(basename, "Argument 'basename' must not be blank");
    }

    @Nonnull
    public String getBasename() {
        return basename;
    }

    @Nonnull
    protected Object doResolveResourceValue(@Nonnull String key, @Nonnull Locale locale) throws NoSuchResourceException {
        requireNonBlank(key, ERROR_KEY_BLANK);
        requireNonNull(locale, ERROR_LOCALE_NULL);
        return getBundle(locale).getObject(key);
    }

    @Nonnull
    protected ResourceBundle getBundle(@Nonnull Locale locale) {
        requireNonNull(locale, ERROR_LOCALE_NULL);
        ResourceBundle rb = bundles.get(locale);
        if (null == rb) {
            rb = compositeResourceBundleBuilder.create(basename, locale);
            bundles.put(locale, rb);
        }
        return rb;
    }
}
