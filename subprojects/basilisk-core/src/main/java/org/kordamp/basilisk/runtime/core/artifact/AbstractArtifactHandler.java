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
import basilisk.core.artifact.ArtifactHandler;
import basilisk.core.artifact.BasiliskArtifact;
import basilisk.core.artifact.BasiliskClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the ArtifactHandler interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractArtifactHandler<A extends BasiliskArtifact> implements ArtifactHandler<A> {
    protected static final String ERROR_CLASS_NULL = "Argument 'class' must not be null";
    private final Class<A> artifactType;
    private final String type;
    private final String trailing;
    private final BasiliskApplication application;
    private final Map<String, BasiliskClass> classesByName = new TreeMap<>();
    private BasiliskClass[] basiliskClasses = new BasiliskClass[0];

    @Inject
    public AbstractArtifactHandler(@Nonnull BasiliskApplication application, @Nonnull Class<A> artifactType, @Nonnull String type, @Nonnull String trailing) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
        this.artifactType = requireNonNull(artifactType, "Argument 'artifactType' must not be null");
        this.type = requireNonBlank(type, "Argument 'type' must not be blank");
        this.trailing = requireNonNull(trailing, "Argument 'trailing' must not be null");
    }

    @Nonnull
    public Class<A> getArtifactType() {
        return artifactType;
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Nonnull
    public String getTrailing() {
        return trailing;
    }

    public void initialize(@Nonnull Class<A>[] classes) {
        basiliskClasses = new BasiliskClass[classes.length];
        for (int i = 0; i < classes.length; i++) {
            Class<A> klass = classes[i];
            BasiliskClass basiliskClass = newBasiliskClassInstance(klass);
            basiliskClasses[i] = basiliskClass;
            classesByName.put(klass.getName(), basiliskClass);
        }
    }

    @Nonnull
    public Map<String, BasiliskClass> getClassesByName() {
        return Collections.unmodifiableMap(classesByName);
    }

    /**
     * Returns true if the target Class is a class artifact
     * handled by this object.<p>
     * This implementation performs an equality check on class.name
     */
    public boolean isArtifact(@Nonnull Class<A> clazz) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        return classesByName.get(clazz.getName()) != null;
    }

    public boolean isArtifact(@Nonnull BasiliskClass clazz) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        for (BasiliskClass basiliskClass : basiliskClasses) {
            if (basiliskClass.equals(clazz)) return true;
        }
        return false;
    }

    @Nonnull
    public BasiliskClass[] getClasses() {
        return basiliskClasses;
    }

    @Nullable
    public BasiliskClass getClassFor(@Nonnull Class<A> clazz) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        return getClassFor(clazz.getName());
    }

    @Nullable
    public BasiliskClass getClassFor(@Nonnull String fqnClassName) {
        requireNonBlank(fqnClassName, "Argument 'fqnClassName' must not be blank");
        return classesByName.get(fqnClassName);
    }

    @Nullable
    public BasiliskClass findClassFor(@Nonnull String propertyName) {
        requireNonBlank(propertyName, "Argument 'propertyName' must not be blank");

        String simpleName = propertyName;

        int lastDot = propertyName.lastIndexOf(".");
        if (lastDot > -1) {
            simpleName = simpleName.substring(lastDot + 1);
        }

        if (simpleName.length() == 1) {
            simpleName = simpleName.toUpperCase();
        } else {
            simpleName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
        }

        if (!simpleName.endsWith(trailing)) {
            simpleName += trailing;
        }

        for (BasiliskClass basiliskClass : basiliskClasses) {
            if (basiliskClass.getClazz().getSimpleName().equals(simpleName)) {
                return basiliskClass;
            }
        }

        return null;
    }

    @Nonnull
    protected BasiliskApplication getApplication() {
        return application;
    }
}
