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

import basilisk.core.BasiliskApplication;
import basilisk.core.LifecycleHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@code LifecycleHandler} interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractLifecycleHandler implements LifecycleHandler {
    private final BasiliskApplication application;

    @Inject
    public AbstractLifecycleHandler(@Nonnull BasiliskApplication application) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
    }

    @Nonnull
    protected BasiliskApplication getApplication() {
        return application;
    }

    @Override
    public boolean isUIThread() {
        return application.getUIThreadManager().isUIThread();
    }

    @Override
    @Nonnull
    public <R> Future<R> runFuture(@Nonnull Callable<R> callable) {
        return application.getUIThreadManager().runFuture(callable);
    }

    @Override
    @Nonnull
    public <R> Future<R> runFuture(@Nonnull ExecutorService executorService, @Nonnull Callable<R> callable) {
        return application.getUIThreadManager().runFuture(executorService, callable);
    }

    @Override
    public void runInsideUIAsync(@Nonnull Runnable runnable) {
        application.getUIThreadManager().runInsideUIAsync(runnable);
    }

    @Override
    public void runInsideUISync(@Nonnull Runnable runnable) {
        application.getUIThreadManager().runInsideUISync(runnable);
    }

    @Override
    public void runOutsideUI(@Nonnull Runnable runnable) {
        application.getUIThreadManager().runOutsideUI(runnable);
    }

    @Override
    public void runOutsideUIAsync(@Nonnull Runnable runnable) {
        application.getUIThreadManager().runOutsideUIAsync(runnable);
    }

    @Nullable
    @Override
    public <R> R runInsideUISync(@Nonnull Callable<R> callable) {
        return application.getUIThreadManager().runInsideUISync(callable);
    }
}
