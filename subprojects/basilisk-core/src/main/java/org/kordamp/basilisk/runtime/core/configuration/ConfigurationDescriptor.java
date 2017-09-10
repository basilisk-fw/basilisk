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
package org.kordamp.basilisk.runtime.core.configuration;

import com.googlecode.openbeans.PropertyEditor;

import javax.annotation.Nonnull;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public abstract class ConfigurationDescriptor {
    private final String configuration;
    private final String key;
    private final String defaultValue;
    private final String format;
    private final Class<? extends PropertyEditor> editor;

    public ConfigurationDescriptor(@Nonnull String configuration, @Nonnull String key, @Nonnull String defaultValue, @Nonnull String format, @Nonnull Class<? extends PropertyEditor> editor) {
        this.configuration = configuration;
        this.key = requireNonBlank(key, "Argument 'key' must not be blank");
        this.defaultValue = defaultValue;
        this.format = format;
        this.editor = editor;
    }

    @Nonnull
    public String getConfiguration() {
        return configuration;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nonnull
    public String getDefaultValue() {
        return defaultValue;
    }

    @Nonnull
    public String getFormat() {
        return format;
    }

    @Nonnull
    public Class<? extends PropertyEditor> getEditor() {
        return editor;
    }

    @Nonnull
    public abstract InjectionPoint asInjectionPoint();
}
