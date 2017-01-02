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
package org.kordamp.basilisk.runtime.core;

import basilisk.core.ApplicationClassLoader;
import basilisk.core.BasiliskApplication;
import basilisk.core.LifecycleHandler;
import basilisk.exceptions.InstanceNotFoundException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static basilisk.util.AnnotationUtils.named;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class LifecycleHandlerProvider implements Provider<LifecycleHandler> {
    private final String basename;

    private BasiliskApplication application;
    private ApplicationClassLoader applicationClassLoader;

    public LifecycleHandlerProvider(@Nonnull String basename) {
        this.basename = requireNonBlank(basename, "Argument 'basename' must not be blank");
    }

    @Inject
    public void setBasiliskApplication(@Nonnull BasiliskApplication application) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
    }

    @Inject
    public void setApplicationClassLoader(ApplicationClassLoader applicationClassLoader) {
        this.applicationClassLoader = requireNonNull(applicationClassLoader, "Argument 'applicationClassLoader' must not be null");
    }

    @Override
    @SuppressWarnings("unchecked")
    public LifecycleHandler get() {
        Class<LifecycleHandler> handlerClass;
        try {
            handlerClass = (Class<LifecycleHandler>) applicationClassLoader.get().loadClass(basename);
        } catch (ClassNotFoundException e) {
            return new NoopLifecycleHandler(application);
        }

        try {
            Constructor<LifecycleHandler> ctor = handlerClass.getDeclaredConstructor(BasiliskApplication.class);
            LifecycleHandler handler = ctor.newInstance(application);
            application.getInjector().injectMembers(handler);
            return handler;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new InstanceNotFoundException(handlerClass, named(basename), e);
        } catch (InvocationTargetException e) {
            throw new InstanceNotFoundException(handlerClass, named(basename), e.getTargetException());
        }
    }

    private static class NoopLifecycleHandler extends AbstractLifecycleHandler {
        public NoopLifecycleHandler(@Nonnull BasiliskApplication application) {
            super(application);
        }

        @Override
        public void execute() {
            // noop
        }
    }
}