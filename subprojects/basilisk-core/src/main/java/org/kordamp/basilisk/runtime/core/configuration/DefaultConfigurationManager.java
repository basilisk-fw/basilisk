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

import basilisk.core.BasiliskApplication;
import basilisk.core.configuration.Configuration;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static basilisk.util.AnnotationUtils.named;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class DefaultConfigurationManager extends AbstractConfigurationManager {
    private final BasiliskApplication application;

    @Inject
    public DefaultConfigurationManager(@Nonnull BasiliskApplication application) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
    }

    @Nonnull
    @Override
    public Configuration getConfiguration() {
        return application.getInjector().getInstance(Configuration.class);
    }

    @Nonnull
    @Override
    public Configuration getConfiguration(@Nonnull String name) {
        return application.getInjector().getInstance(Configuration.class, named(name));
    }
}
