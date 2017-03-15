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

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class FieldConfigurationDescriptor extends ConfigurationDescriptor {
    private final Field field;

    public FieldConfigurationDescriptor(@Nonnull Field field, @Nonnull String configuration, @Nonnull String key, @Nonnull String defaultValue, @Nonnull String format) {
        super(configuration, key, defaultValue, format);
        this.field = requireNonNull(field, "Argument 'field' must not be null");
    }

    @Nonnull
    public Field getField() {
        return field;
    }

    @Nonnull
    public InjectionPoint asInjectionPoint() {
        return new FieldInjectionPoint(field, getConfiguration(), getKey(), getFormat());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldPreferenceDescriptor{");
        sb.append("field=").append(field);
        sb.append(", configuration='").append(getConfiguration()).append('\'');
        sb.append(", key='").append(getKey()).append('\'');
        sb.append(", defaultValue='").append(getDefaultValue()).append('\'');
        sb.append(", format='").append(getFormat()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
