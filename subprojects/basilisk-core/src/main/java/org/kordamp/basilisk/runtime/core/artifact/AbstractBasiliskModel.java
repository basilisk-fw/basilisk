/*
 * Copyright 2008-2015 the original author or authors.
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
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskModelClass;
import com.googlecode.openbeans.PropertyChangeEvent;
import com.googlecode.openbeans.PropertyChangeListener;
import com.googlecode.openbeans.PropertyChangeSupport;
import com.googlecode.openbeans.PropertyVetoException;
import com.googlecode.openbeans.VetoableChangeListener;
import com.googlecode.openbeans.VetoableChangeSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the BasiliskModel interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractBasiliskModel extends AbstractBasiliskMvcArtifact implements BasiliskModel {
    private static final String ERROR_EVENT_NULL = "Argument 'event' must not be null";
    private static final String ERROR_PROPERTY_NAME_BLANK = "Argument 'propertyName' must not be blank";
    protected final PropertyChangeSupport pcs;
    protected final VetoableChangeSupport vcs;

    public AbstractBasiliskModel() {
        pcs = new PropertyChangeSupport(this);
        vcs = new VetoableChangeSupport(this);
    }

    /**
     * Creates a new instance of this class.
     *
     * @param application the BasiliskApplication that holds this artifact.
     * @deprecated Basilisk prefers field injection over constructor injector for artifacts as of 2.1.0
     */
    @Inject
    @Deprecated
    public AbstractBasiliskModel(@Nonnull BasiliskApplication application) {
        super(application);
        pcs = new PropertyChangeSupport(this);
        vcs = new VetoableChangeSupport(this);
    }

    @Nonnull
    @Override
    protected String getArtifactType() {
        return BasiliskModelClass.TYPE;
    }

    @Override
    public void addVetoableChangeListener(@Nullable VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(listener);
    }

    @Override
    public void addVetoableChangeListener(@Nullable String propertyName, @Nullable VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(propertyName, listener);
    }

    @Override
    public void removeVetoableChangeListener(@Nullable VetoableChangeListener listener) {
        vcs.removeVetoableChangeListener(listener);
    }

    @Override
    public void removeVetoableChangeListener(@Nullable String propertyName, @Nullable VetoableChangeListener listener) {
        vcs.removeVetoableChangeListener(propertyName, listener);
    }

    @Nonnull
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vcs.getVetoableChangeListeners();
    }

    @Nonnull
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners(@Nullable String propertyName) {
        return vcs.getVetoableChangeListeners(propertyName);
    }

    @Override
    public void addPropertyChangeListener(@Nullable PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(@Nullable String propertyName, @Nullable PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(@Nullable PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@Nullable String propertyName, @Nullable PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Nonnull
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Nonnull
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(@Nullable String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    protected void firePropertyChange(@Nonnull PropertyChangeEvent event) {
        pcs.firePropertyChange(requireNonNull(event, ERROR_EVENT_NULL));
    }

    protected void firePropertyChange(@Nonnull String propertyName, @Nullable Object oldValue, @Nullable Object newValue) {
        pcs.firePropertyChange(requireNonBlank(propertyName, ERROR_PROPERTY_NAME_BLANK), oldValue, newValue);
    }

    protected void fireVetoableChange(@Nonnull PropertyChangeEvent event) throws PropertyVetoException {
        vcs.fireVetoableChange(requireNonNull(event, ERROR_EVENT_NULL));
    }

    protected void fireVetoableChange(@Nonnull String propertyName, @Nullable Object oldValue, @Nullable Object newValue) throws PropertyVetoException {
        vcs.fireVetoableChange(requireNonBlank(propertyName, ERROR_PROPERTY_NAME_BLANK), oldValue, newValue);
    }
}
