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
package org.kordamp.basilisk.runtime.core.artifact;

import basilisk.core.artifact.ArtifactHandler;
import basilisk.core.artifact.ArtifactManager;
import basilisk.core.artifact.BasiliskArtifact;
import basilisk.core.artifact.BasiliskClass;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.injection.Injector;
import basilisk.core.threading.UIThreadManager;
import basilisk.exceptions.ArtifactHandlerNotFoundException;
import basilisk.exceptions.ArtifactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@code ArtifactManager} interface.
 *
 * @author Andres Almiray
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractArtifactManager implements ArtifactManager {
    protected static final String ERROR_ARTIFACT_HANDLER_NULL = "Argument 'artifactHandler' must not be null";
    private static final String ERROR_NAME_BLANK = "Argument 'name' must not be blank";
    private static final String ERROR_TYPE_BLANK = "Argument 'type' must not be blank";
    private static final String ERROR_CLASS_NULL = "Argument 'clazz' must not be null";
    private static final String ERROR_ARTIFACT_NULL = "Argument 'artifact' must not be null";
    private static final String ERROR_FULLY_QUALIFIED_CLASSNAME_BLANK = "Argument 'fqClassName' must not be blank";
    private static final Logger LOG = LoggerFactory.getLogger(AbstractArtifactManager.class);
    private final Map<String, Class<? extends BasiliskArtifact>[]> artifacts = new ConcurrentHashMap<>();
    private final Map<String, ArtifactHandler> artifactHandlers = new ConcurrentHashMap<>();
    private final Object lock = new Object[0];

    @Inject
    private Provider<Injector> injectorProvider;

    @Inject
    private UIThreadManager uiThreadManager;

    @Nonnull
    protected Map<String, ArtifactHandler> getArtifactHandlers() {
        return artifactHandlers;
    }

    @SuppressWarnings("unchecked")
    public final void loadArtifactMetadata() {
        Map<String, List<Class<? extends BasiliskArtifact>>> loadedArtifacts = doLoadArtifactMetadata();

        synchronized (lock) {
            for (Map.Entry<String, List<Class<? extends BasiliskArtifact>>> artifactsEntry : loadedArtifacts.entrySet()) {
                String type = artifactsEntry.getKey();
                ArtifactHandler handler = artifactHandlers.get(type);
                if (handler == null) {
                    throw new ArtifactHandlerNotFoundException(type);
                }
                List<Class<? extends BasiliskArtifact>> list = artifactsEntry.getValue();
                artifacts.put(type, list.toArray(new Class[list.size()]));
                handler.initialize(artifacts.get(type));
            }
        }
    }

    @Nonnull
    @Override
    public Set<String> getAllTypes() {
        return Collections.unmodifiableSet(artifactHandlers.keySet());
    }

    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public <A extends BasiliskArtifact> A newInstance(@Nonnull BasiliskClass basiliskClass) {
        try {
            requireNonNull(basiliskClass, "Argument 'basiliskClass' must not be null");
        } catch (RuntimeException re) {
            throw new ArtifactNotFoundException(re);
        }

        return newInstance((Class<A>) basiliskClass.getClazz());
    }

    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public <A extends BasiliskArtifact> A newInstance(final @Nonnull Class<A> clazz) {
        if (findBasiliskClass(clazz) == null) {
            throw new ArtifactNotFoundException(clazz);
        }

        if (BasiliskView.class.isAssignableFrom(clazz)) {
            return uiThreadManager.runInsideUISync(new Callable<A>() {
                @Override
                public A call() throws Exception {
                    return (A) injectorProvider.get().getInstance(clazz);
                }
            });
        }

        return (A) injectorProvider.get().getInstance(clazz);
    }

    @Nonnull
    protected abstract Map<String, List<Class<? extends BasiliskArtifact>>> doLoadArtifactMetadata();

    public void registerArtifactHandler(@Nonnull ArtifactHandler artifactHandler) {
        requireNonNull(artifactHandler, ERROR_ARTIFACT_HANDLER_NULL);
        LOG.debug("Registering artifact handler for type '{}': {}", artifactHandler.getType(), artifactHandler);
        synchronized (lock) {
            artifactHandlers.put(artifactHandler.getType(), artifactHandler);
        }
    }

    public void unregisterArtifactHandler(@Nonnull ArtifactHandler artifactHandler) {
        requireNonNull(artifactHandler, ERROR_ARTIFACT_HANDLER_NULL);
        LOG.debug("Removing artifact handler for type '{}': {}", artifactHandler.getType(), artifactHandler);
        synchronized (lock) {
            artifactHandlers.remove(artifactHandler.getType());
        }
    }

    protected boolean isArtifactTypeSupported(@Nonnull String type) {
        requireNonBlank(type, ERROR_TYPE_BLANK);
        return artifactHandlers.get(type) != null;
    }

    @Nullable
    public BasiliskClass findBasiliskClass(@Nonnull String name, @Nonnull String type) {
        requireNonBlank(name, ERROR_NAME_BLANK);
        requireNonBlank(type, ERROR_TYPE_BLANK);
        LOG.debug("Searching for basiliskClass of {}:{}", type, name);
        synchronized (lock) {
            ArtifactHandler handler = artifactHandlers.get(type);
            return handler != null ? handler.findClassFor(name) : null;
        }
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BasiliskClass findBasiliskClass(@Nonnull Class<? extends BasiliskArtifact> clazz, @Nonnull String type) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        requireNonBlank(type, ERROR_TYPE_BLANK);
        LOG.debug("Searching for basiliskClass of {}:{}", type, clazz.getName());
        synchronized (lock) {
            ArtifactHandler handler = artifactHandlers.get(type);
            return handler != null ? handler.getClassFor(clazz) : null;
        }
    }

    @Nullable
    public <A extends BasiliskArtifact> BasiliskClass findBasiliskClass(@Nonnull A artifact) {
        requireNonNull(artifact, ERROR_ARTIFACT_NULL);
        synchronized (lock) {
            return findBasiliskClass(artifact.getClass());
        }
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BasiliskClass findBasiliskClass(@Nonnull Class<? extends BasiliskArtifact> clazz) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        LOG.debug("Searching for basiliskClass of {}", clazz.getName());
        synchronized (lock) {
            for (ArtifactHandler handler : artifactHandlers.values()) {
                BasiliskClass basiliskClass = handler.getClassFor(clazz);
                if (basiliskClass != null) return basiliskClass;
            }
        }
        return null;
    }

    @Nullable
    public BasiliskClass findBasiliskClass(@Nonnull String fqClassName) {
        requireNonBlank(fqClassName, ERROR_FULLY_QUALIFIED_CLASSNAME_BLANK);
        LOG.debug("Searching for basiliskClass of {}", fqClassName);
        synchronized (lock) {
            for (ArtifactHandler handler : artifactHandlers.values()) {
                BasiliskClass basiliskClass = handler.getClassFor(fqClassName);
                if (basiliskClass != null) return basiliskClass;
            }
        }
        return null;
    }

    @Nonnull
    public List<BasiliskClass> getClassesOfType(@Nonnull String type) {
        requireNonBlank(type, ERROR_TYPE_BLANK);
        synchronized (lock) {
            if (artifacts.containsKey(type)) {
                return asList(artifactHandlers.get(type).getClasses());
            }
        }
        return EMPTY_BASILISK_CLASS_LIST;
    }

    @Nonnull
    public List<BasiliskClass> getAllClasses() {
        List<BasiliskClass> all = new ArrayList<>();
        synchronized (lock) {
            for (ArtifactHandler handler : artifactHandlers.values()) {
                all.addAll(asList(handler.getClasses()));
            }
        }
        return Collections.unmodifiableList(all);
    }

    protected <A extends BasiliskArtifact> boolean isClassOfType(@Nonnull String type, @Nonnull Class<A> clazz) {
        for (Class<? extends BasiliskArtifact> klass : artifacts.get(type)) {
            if (klass.getName().equals(clazz.getName())) {
                return true;
            }
        }
        return false;
    }
}
