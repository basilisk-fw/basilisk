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
package org.kordamp.basilisk.runtime.core.mvc;

import basilisk.core.BasiliskApplication;
import basilisk.core.Context;
import basilisk.core.ContextFactory;
import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskMvcArtifact;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.mvc.MVCFunction;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.mvc.MVCGroupConfiguration;
import basilisk.core.mvc.MVCGroupConfigurationFactory;
import basilisk.core.mvc.MVCGroupFactory;
import basilisk.core.mvc.MVCGroupFunction;
import basilisk.core.mvc.MVCGroupManager;
import basilisk.core.mvc.TypedMVCGroup;
import basilisk.core.mvc.TypedMVCGroupFunction;
import basilisk.exceptions.ArtifactNotFoundException;
import basilisk.exceptions.MVCGroupConfigurationException;
import basilisk.exceptions.MVCGroupInstantiationException;
import basilisk.util.AnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static basilisk.core.BasiliskExceptionHandler.sanitize;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@code MVCGroupManager} interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractMVCGroupManager implements MVCGroupManager {
    protected static final String ERROR_MVCTYPE_BLANK = "Argument 'mvcType' must not be blank";
    protected static final String ERROR_MVCID_BLANK = "Argument 'mvcId' must not be blank";
    protected static final String ERROR_CONFIGURATION_NULL = "Argument 'configuration' must not be null";
    protected static final String ERROR_GROUP_NULL = "Argument 'group' must not be null";
    protected static final String ERROR_CONFIG_NULL = "Argument 'config' must not be null";
    protected static final String ERROR_ARGS_NULL = "Argument 'args' must not be null";
    protected static final String ERROR_NAME_BLANK = "Argument 'name' cannot be blank";
    protected static final String ERROR_TYPE_NULL = "Argument 'type' cannot be null";
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMVCGroupManager.class);
    private final BasiliskApplication application;

    private final Map<String, MVCGroupConfiguration> configurations = new LinkedHashMap<>();
    private final Map<String, MVCGroup> groups = new LinkedHashMap<>();
    private final Object lock = new Object[0];
    private boolean initialized;

    @Inject
    private MVCGroupConfigurationFactory mvcGroupConfigurationFactory;

    @Inject
    private MVCGroupFactory mvcGroupFactory;

    @Inject
    private ContextFactory contextFactory;

    @Inject
    public AbstractMVCGroupManager(@Nonnull BasiliskApplication application) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
    }

    @Override
    public BasiliskApplication getApplication() {
        return application;
    }

    @Nonnull
    public MVCGroupConfiguration newMVCGroupConfiguration(@Nonnull String mvcType, @Nonnull Map<String, String> members, @Nonnull Map<String, Object> config) {
        return mvcGroupConfigurationFactory.create(mvcType, members, config);
    }

    @Nonnull
    public MVCGroup newMVCGroup(@Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId, @Nonnull Map<String, Object> members, @Nullable MVCGroup parentGroup) {
        return mvcGroupFactory.create(configuration, mvcId, members, parentGroup);
    }

    @Nonnull
    @Override
    public Context newContext(@Nullable MVCGroup parentGroup) {
        Context parentContext = parentGroup != null ? parentGroup.getContext() : getApplication().getContext();
        return contextFactory.create(parentContext);
    }

    @Nonnull
    public Map<String, MVCGroupConfiguration> getConfigurations() {
        synchronized (lock) {
            return unmodifiableMap(configurations);
        }
    }

    @Nonnull
    public Map<String, MVCGroup> getGroups() {
        synchronized (lock) {
            return unmodifiableMap(groups);
        }
    }

    @Nonnull
    public MVCGroupConfiguration findConfiguration(@Nonnull String mvcType) {
        requireNonBlank(mvcType, ERROR_MVCTYPE_BLANK);
        MVCGroupConfiguration configuration;
        synchronized (lock) {
            configuration = configurations.get(mvcType);
        }

        if (configuration == null) {
            throw new MVCGroupConfigurationException("Unknown MVC type '" + mvcType + "'. Known types are " + configurations.keySet(), mvcType);
        }
        return configuration;
    }

    @Nullable
    public MVCGroup findGroup(@Nonnull String mvcId) {
        requireNonBlank(mvcId, ERROR_MVCID_BLANK);
        synchronized (lock) {
            LOG.debug("Searching group {}", mvcId);
            return groups.get(mvcId);
        }
    }

    @Nullable
    public MVCGroup getAt(@Nonnull String mvcId) {
        return findGroup(mvcId);
    }

    public final void initialize(@Nonnull Map<String, MVCGroupConfiguration> configurations) {
        requireNonNull(configurations, "Argument 'configurations' must not be null");
        if (configurations.isEmpty()) { return; }
        synchronized (lock) {
            if (!initialized) {
                doInitialize(configurations);
                initialized = true;
            }
        }
    }

    public void addConfiguration(@Nonnull MVCGroupConfiguration configuration) {
        requireNonNull(configuration, ERROR_CONFIGURATION_NULL);
        synchronized (lock) {
            if (initialized && configurations.get(configuration.getMvcType()) != null) {
                return;
            }
            configurations.put(configuration.getMvcType(), configuration);
        }
    }

    public void removeConfiguration(@Nonnull MVCGroupConfiguration configuration) {
        requireNonNull(configuration, ERROR_CONFIGURATION_NULL);
        removeConfiguration(configuration.getMvcType());
    }

    public void removeConfiguration(@Nonnull String name) {
        requireNonBlank(name, "Argument 'name' must not be blank");
        if (!isBlank(name)) {
            synchronized (lock) {
                configurations.remove(name);
            }
        }
    }

    protected void addGroup(@Nonnull MVCGroup group) {
        requireNonNull(group, ERROR_GROUP_NULL);
        synchronized (lock) {
            LOG.debug("Adding group {}:{}", group.getMvcId(), group);
            groups.put(group.getMvcId(), group);
        }
    }

    protected void removeGroup(@Nonnull MVCGroup group) {
        requireNonNull(group, ERROR_GROUP_NULL);
        synchronized (lock) {
            LOG.debug("Removing group {}:{}", group.getMvcId(), group);
            groups.remove(group.getMvcId());
        }
    }

    @Nonnull
    public final Map<String, ? extends BasiliskModel> getModels() {
        Map<String, BasiliskModel> models = new LinkedHashMap<>();
        synchronized (lock) {
            for (MVCGroup group : groups.values()) {
                BasiliskModel model = group.getModel();
                if (model != null) {
                    models.put(group.getMvcId(), model);
                }
            }
        }
        return unmodifiableMap(models);
    }

    @Nonnull
    public final Map<String, ? extends BasiliskView> getViews() {
        Map<String, BasiliskView> views = new LinkedHashMap<>();
        synchronized (lock) {
            for (MVCGroup group : groups.values()) {
                BasiliskView view = group.getView();
                if (view != null) {
                    views.put(group.getMvcId(), view);
                }
            }
        }
        return unmodifiableMap(views);
    }

    @Nonnull
    public final Map<String, ? extends BasiliskController> getControllers() {
        Map<String, BasiliskController> controllers = new LinkedHashMap<>();
        synchronized (lock) {
            for (MVCGroup group : groups.values()) {
                BasiliskController controller = group.getController();
                if (controller != null) {
                    controllers.put(group.getMvcId(), controller);
                }
            }
        }
        return unmodifiableMap(controllers);
    }

    @Nonnull
    @Override
    public MVCGroupConfiguration cloneMVCGroupConfiguration(@Nonnull String mvcType, @Nonnull Map<String, Object> config) {
        requireNonBlank(mvcType, ERROR_MVCTYPE_BLANK);
        requireNonNull(config, ERROR_CONFIG_NULL);
        MVCGroupConfiguration configuration = findConfiguration(mvcType);
        Map<String, Object> configCopy = new LinkedHashMap<>();
        configCopy.putAll(configuration.getConfig());
        configCopy.putAll(config);
        return newMVCGroupConfiguration(mvcType, configuration.getMembers(), configCopy);
    }

    @Nonnull
    protected List<? extends BasiliskMvcArtifact> createMVC(@Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId, @Nonnull Map<String, Object> args) {
        MVCGroup group = createMVCGroup(findConfiguration(configuration.getMvcType()), mvcId, args);
        return asList(group.getModel(), group.getView(), group.getController());
    }

    @SuppressWarnings("unchecked")
    protected <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVCGroup(@Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        MVCGroup group = null;
        try {
            group = createMVCGroup(configuration, mvcId, args);
            handler.apply((M) group.getModel(), (V) group.getView(), (C) group.getController());
        } finally {
            try {
                if (group != null) {
                    destroyMVCGroup(group.getMvcId());
                }
            } catch (Exception x) {
                LOG.warn("Could not destroy group [{}] of type {}", mvcId, configuration.getMvcType(), sanitize(x));
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void withMVCGroup(@Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        MVCGroup group = null;
        try {
            group = createMVCGroup(configuration, mvcId, args);
            handler.apply(group);
        } finally {
            try {
                if (group != null) {
                    destroyMVCGroup(group.getMvcId());
                }
            } catch (Exception x) {
                LOG.warn("Could not destroy group [{}] of type {}", mvcId, configuration.getMvcType(), sanitize(x));
            }
        }
    }

    @Nonnull
    protected abstract MVCGroup createMVCGroup(@Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId, @Nonnull Map<String, Object> args);

    protected abstract void doInitialize(@Nonnull Map<String, MVCGroupConfiguration> configurations);

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType) {
        return createMVCGroup(findConfiguration(mvcType), null, Collections.<String, Object>emptyMap());
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId) {
        return createMVCGroup(findConfiguration(mvcType), mvcId, Collections.<String, Object>emptyMap());
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return createMVCGroup(findConfiguration(mvcType), null, args);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return createMVCGroup(findConfiguration(mvcType), null, args);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return createMVCGroup(findConfiguration(mvcType), mvcId, args);
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return createMVCGroup(findConfiguration(mvcType), mvcId, args);
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType) {
        return typedMvcGroup(mvcType, createMVCGroup(findConfiguration(nameOf(mvcType)), null, Collections.<String, Object>emptyMap()));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return typedMvcGroup(mvcType, createMVCGroup(findConfiguration(nameOf(mvcType)), mvcId, Collections.<String, Object>emptyMap()));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType) {
        return typedMvcGroup(mvcType, createMVCGroup(findConfiguration(nameOf(mvcType)), null, args));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args) {
        return typedMvcGroup(mvcType, createMVCGroup(findConfiguration(nameOf(mvcType)), null, args));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return typedMvcGroup(mvcType, createMVCGroup(findConfiguration(nameOf(mvcType)), mvcId, args));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return typedMvcGroup(mvcType, createMVCGroup(findConfiguration(nameOf(mvcType)), mvcId, args));
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType) {
        return createMVC(findConfiguration(mvcType), null, Collections.<String, Object>emptyMap());
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return createMVC(findConfiguration(mvcType), null, args);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return createMVC(findConfiguration(mvcType), null, args);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId) {
        return createMVC(findConfiguration(mvcType), mvcId, Collections.<String, Object>emptyMap());
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return createMVC(findConfiguration(mvcType), mvcId, args);
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return createMVC(findConfiguration(mvcType), mvcId, args);
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType) {
        return createMVC(findConfiguration(nameOf(mvcType)), null, Collections.<String, Object>emptyMap());
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType) {
        return createMVC(findConfiguration(nameOf(mvcType)), null, args);
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args) {
        return createMVC(findConfiguration(nameOf(mvcType)), null, args);
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return createMVC(findConfiguration(nameOf(mvcType)), mvcId, Collections.<String, Object>emptyMap());
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return createMVC(findConfiguration(nameOf(mvcType)), mvcId, args);
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return createMVC(findConfiguration(nameOf(mvcType)), mvcId, args);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(mvcType), null, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(mvcType), mvcId, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(mvcType), mvcId, args, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(mvcType), mvcId, args, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(mvcType), null, args, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(mvcType), null, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(nameOf(mvcType)), null, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(nameOf(mvcType)), mvcId, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(nameOf(mvcType)), mvcId, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(nameOf(mvcType)), mvcId, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(nameOf(mvcType)), null, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        withMVCGroup(findConfiguration(nameOf(mvcType)), null, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        withMVCGroup(findConfiguration(mvcType), null, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        withMVCGroup(findConfiguration(mvcType), mvcId, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        withMVCGroup(findConfiguration(mvcType), mvcId, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        withMVCGroup(findConfiguration(mvcType), mvcId, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        withMVCGroup(findConfiguration(mvcType), null, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        withMVCGroup(findConfiguration(mvcType), null, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        withMVCGroup(mvcType, null, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        withMVCGroup(mvcType, mvcId, Collections.<String, Object>emptyMap(), handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        MVC group = null;
        try {
            group = createMVCGroup(mvcType, mvcId, args);
            handler.apply(group);
        } finally {
            try {
                if (group != null) {
                    destroyMVCGroup(group.getMvcId());
                }
            } catch (Exception x) {
                LOG.warn("Could not destroy group [{}] of type {}", mvcId, nameOf(mvcType), sanitize(x));
            }
        }
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        withMVCGroup(mvcType, mvcId, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        withMVCGroup(mvcType, null, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        withMVCGroup(mvcType, null, args, handler);
    }

    @Nonnull
    @Override
    public <C extends BasiliskController> C getController(@Nonnull String name, @Nonnull Class<C> type) throws ArtifactNotFoundException {
        requireNonBlank(name, ERROR_NAME_BLANK);
        requireNonNull(type, ERROR_TYPE_NULL);
        BasiliskController controller = getControllers().get(name);
        if (controller != null) {
            return type.cast(controller);
        }
        throw new ArtifactNotFoundException(type, name);
    }

    @Nonnull
    @Override
    public <M extends BasiliskModel> M getModel(@Nonnull String name, @Nonnull Class<M> type) throws ArtifactNotFoundException {
        requireNonBlank(name, ERROR_NAME_BLANK);
        requireNonNull(type, ERROR_TYPE_NULL);
        BasiliskModel model = getModels().get(name);
        if (model != null) {
            return type.cast(model);
        }
        throw new ArtifactNotFoundException(type, name);
    }

    @Nonnull
    @Override
    public <V extends BasiliskView> V getView(@Nonnull String name, @Nonnull Class<V> type) throws ArtifactNotFoundException {
        requireNonBlank(name, ERROR_NAME_BLANK);
        requireNonNull(type, ERROR_TYPE_NULL);
        BasiliskView view = getViews().get(name);
        if (view != null) {
            return type.cast(view);
        }
        throw new ArtifactNotFoundException(type, name);
    }

    @Nullable
    @Override
    public <C extends BasiliskController> C findController(@Nonnull String name, @Nonnull Class<C> type) {
        try {
            return getController(name, type);
        } catch (ArtifactNotFoundException anfe) {
            return null;
        }
    }

    @Nullable
    @Override
    public <M extends BasiliskModel> M findModel(@Nonnull String name, @Nonnull Class<M> type) {
        try {
            return getModel(name, type);
        } catch (ArtifactNotFoundException anfe) {
            return null;
        }
    }

    @Nullable
    @Override
    public <V extends BasiliskView> V findView(@Nonnull String name, @Nonnull Class<V> type) {
        try {
            return getView(name, type);
        } catch (ArtifactNotFoundException anfe) {
            return null;
        }
    }

    @Nonnull
    protected <MVC extends TypedMVCGroup> String nameOf(@Nonnull Class<? extends MVC> mvcType) {
        return AnnotationUtils.nameFor(mvcType, true);
    }

    @Nonnull
    protected <MVC extends TypedMVCGroup> MVC typedMvcGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull MVCGroup mvcGroup) {
        try {
            Constructor<? extends MVC> constructor = mvcType.getDeclaredConstructor(MVCGroup.class);
            return constructor.newInstance(mvcGroup);
        } catch (Exception e) {
            throw new MVCGroupInstantiationException("Unexpected error", mvcGroup.getMvcType(), mvcGroup.getMvcId(), e);
        }
    }
}
