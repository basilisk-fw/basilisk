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
package org.kordamp.basilisk.runtime.core.i18n;

import basilisk.core.i18n.MessageSource;
import basilisk.util.CompositeResourceBundleBuilder;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class MessageSourceProvider implements Provider<MessageSource> {
    private final String basename;

    @Inject
    private CompositeResourceBundleBuilder resourceBundleBuilder;

    @Inject
    private MessageSourceDecoratorFactory messageSourceDecoratorFactory;

    public MessageSourceProvider(@Nonnull String basename) {
        this.basename = requireNonBlank(basename, "Argument 'basename' must not be blank");
    }

    @Override
    public MessageSource get() {
        requireNonNull(resourceBundleBuilder, "Argument 'resourceBundleBuilder' must not be null");
        requireNonNull(messageSourceDecoratorFactory, "Argument 'messageSourceDecoratorFactory' must not be null");
        DefaultMessageSource messageSource = new DefaultMessageSource(resourceBundleBuilder, basename);
        return messageSourceDecoratorFactory.create(messageSource);
    }
}
