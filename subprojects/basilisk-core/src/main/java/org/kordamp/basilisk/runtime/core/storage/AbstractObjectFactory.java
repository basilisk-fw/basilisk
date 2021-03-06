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
package org.kordamp.basilisk.runtime.core.storage;


import basilisk.core.BasiliskApplication;
import basilisk.core.configuration.Configuration;
import basilisk.core.storage.ObjectFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static basilisk.util.ConfigUtils.getConfigValue;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractObjectFactory<T> implements ObjectFactory<T> {
    private final Configuration configuration;
    private final BasiliskApplication application;

    @Inject
    public AbstractObjectFactory(@Nonnull Configuration configuration, @Nonnull BasiliskApplication application) {
        this.configuration = requireNonNull(configuration, "Argument 'configuration' must not be null");
        this.application = requireNonNull(application, "Argument 'application' must not be null");
    }

    @Nonnull
    public Configuration getConfiguration() {
        return configuration;
    }

    @Nonnull
    public BasiliskApplication getApplication() {
        return application;
    }

    @Nonnull
    protected abstract String getSingleKey();

    @Nonnull
    protected abstract String getPluralKey();

    protected void event(@Nonnull String eventName, @Nonnull List<?> args) {
        application.getEventRouter().publishEvent(eventName, args);
    }

    @Nonnull
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected Map<String, Object> narrowConfig(@Nonnull String name) {
        requireNonBlank(name, "Argument 'name' must not be blank");
        if (KEY_DEFAULT.equals(name) && configuration.containsKey(getSingleKey())) {
            return (Map<String, Object>) configuration.get(getSingleKey());
        } else if (configuration.containsKey(getPluralKey())) {
            Map<String, Object> elements = (Map<String, Object>) configuration.get(getPluralKey());
            return getConfigValue(elements, name, Collections.<String, Object>emptyMap());
        }
        return Collections.emptyMap();
    }
}
