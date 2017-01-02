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
package org.kordamp.basilisk.runtime.core.artifact;

import basilisk.core.BasiliskApplication;
import basilisk.core.artifact.BasiliskArtifact;
import basilisk.core.artifact.BasiliskClass;
import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskMvcArtifact;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.i18n.NoSuchMessageException;
import basilisk.core.mvc.MVCFunction;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.mvc.MVCGroupFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Base implementation of the BasiliskArtifact interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractBasiliskArtifact implements BasiliskArtifact {
    private final Logger log;
    private final Object lock = new Object[0];
    @Inject
    protected BasiliskApplication application;
    @GuardedBy("lock")
    private BasiliskClass basiliskClass;

    public AbstractBasiliskArtifact() {
        log = LoggerFactory.getLogger("basilisk.app." + getArtifactType() + "." + getClass().getName());
    }

    @Nonnull
    public BasiliskApplication getApplication() {
        return application;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public BasiliskClass getBasiliskClass() {
        synchronized (lock) {
            if (basiliskClass == null) {
                basiliskClass = application.getArtifactManager().findBasiliskClass(getClass());
            }
            return basiliskClass;
        }
    }

    @Nonnull
    @Override
    public Logger getLog() {
        return log;
    }

    @Nonnull
    protected abstract String getArtifactType();

    @Override
    public boolean isUIThread() {
        return application.getUIThreadManager().isUIThread();
    }

    @Nonnull
    @Override
    public <R> Future<R> runFuture(@Nonnull ExecutorService executorService, @Nonnull Callable<R> callable) {
        return application.getUIThreadManager().runFuture(executorService, callable);
    }

    @Nonnull
    @Override
    public <R> Future<R> runFuture(@Nonnull Callable<R> callable) {
        return application.getUIThreadManager().runFuture(callable);
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
    public void runInsideUIAsync(@Nonnull Runnable runnable) {
        application.getUIThreadManager().runInsideUIAsync(runnable);
    }

    @Nullable
    @Override
    public <R> R runInsideUISync(@Nonnull Callable<R> callable) {
        return application.getUIThreadManager().runInsideUISync(callable);
    }

    @Nonnull
    @Override
    public ClassLoader classloader() {
        return application.getResourceHandler().classloader();
    }

    @Nullable
    @Override
    public URL getResourceAsURL(@Nonnull String name) {
        return application.getResourceHandler().getResourceAsURL(name);
    }

    @Nullable
    @Override
    public List<URL> getResources(@Nonnull String name) {
        return application.getResourceHandler().getResources(name);
    }

    @Nullable
    @Override
    public InputStream getResourceAsStream(@Nonnull String name) {
        return application.getResourceHandler().getResourceAsStream(name);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return application.getMvcGroupManager().createMVCGroup(args, mvcType);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType) {
        return application.getMvcGroupManager().createMVC(mvcType);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        application.getMvcGroupManager().withMVC(mvcType, mvcId, handler);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId) {
        return application.getMvcGroupManager().createMVC(mvcType, mvcId);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return application.getMvcGroupManager().createMVC(args, mvcType, mvcId);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        application.getMvcGroupManager().withMVC(args, mvcType, mvcId, handler);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return application.getMvcGroupManager().createMVCGroup(args, mvcType, mvcId);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return application.getMvcGroupManager().createMVCGroup(mvcType, mvcId, args);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return application.getMvcGroupManager().createMVC(args, mvcType);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        application.getMvcGroupManager().withMVC(mvcType, handler);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType) {
        return application.getMvcGroupManager().createMVCGroup(mvcType);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId) {
        return application.getMvcGroupManager().createMVCGroup(mvcType, mvcId);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return application.getMvcGroupManager().createMVC(mvcType, args);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        application.getMvcGroupManager().withMVC(args, mvcType, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        application.getMvcGroupManager().withMVC(mvcType, args, handler);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return application.getMvcGroupManager().createMVCGroup(mvcType, args);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return application.getMvcGroupManager().createMVC(mvcType, mvcId, args);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        application.getMvcGroupManager().withMVC(mvcType, mvcId, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        application.getMvcGroupManager().withMVCGroup(args, mvcType, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        application.getMvcGroupManager().withMVCGroup(args, mvcType, mvcId, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        application.getMvcGroupManager().withMVCGroup(mvcType, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        application.getMvcGroupManager().withMVCGroup(mvcType, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        application.getMvcGroupManager().withMVCGroup(mvcType, mvcId, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        application.getMvcGroupManager().withMVCGroup(mvcType, mvcId, handler);
    }

    @Override
    public void destroyMVCGroup(@Nonnull String mvcId) {
        application.getMvcGroupManager().destroyMVCGroup(mvcId);
    }

    /**
     * Try to resolve the message.
     *
     * @param key Key to lookup, such as 'log4j.appenders.console'
     * @return The resolved message at the given key for the default locale
     * @throws NoSuchMessageException if no message is found
     */
    @Nonnull
    protected String msg(@Nonnull String key) throws NoSuchMessageException {
        return getApplication().getMessageSource().getMessage(key);
    }

    /**
     * Try to resolve the message.
     *
     * @param key  Key to lookup, such as 'log4j.appenders.console'
     * @param args Arguments that will be filled in for params within the message (params look like "{0}" within a
     *             message, but this might differ between implementations), or null if none.
     * @return The resolved message at the given key for the default locale
     * @throws NoSuchMessageException if no message is found
     */
    @Nonnull
    protected String msg(@Nonnull String key, @Nonnull List<?> args) throws NoSuchMessageException {
        return getApplication().getMessageSource().getMessage(key, args);
    }

    /**
     * Try to resolve the message.
     *
     * @param key  Key to lookup, such as 'log4j.appenders.console'
     * @param args Arguments that will be filled in for params within the message (params look like "{0}" within a
     *             message, but this might differ between implementations), or null if none.
     * @return The resolved message at the given key for the default locale
     * @throws NoSuchMessageException if no message is found
     */
    @Nonnull
    protected String msg(@Nonnull String key, @Nonnull Object[] args) throws NoSuchMessageException {
        return getApplication().getMessageSource().getMessage(key, args);
    }

    /**
     * Try to resolve the message.
     *
     * @param key  Key to lookup, such as 'log4j.appenders.console'
     * @param args Arguments that will be filled in for params within the message (params look like "{:key}"
     *             within a message, but this might differ between implementations), or null if none.
     * @return The resolved message at the given key for the default locale
     * @throws NoSuchMessageException if no message is found
     */
    @Nonnull
    protected String msg(@Nonnull String key, @Nonnull Map<String, Object> args) throws NoSuchMessageException {
        return getApplication().getMessageSource().getMessage(key, args);
    }
}
