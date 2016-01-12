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
package org.kordamp.basilisk.runtime.core.mvc;

import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskMvcArtifact;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.mvc.MVCFunction;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.mvc.MVCGroupFunction;
import basilisk.core.mvc.MVCGroupManager;
import basilisk.core.mvc.MVCHandler;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the MVCHandler interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractMVCHandler implements MVCHandler {
    private final MVCGroupManager mvcGroupManager;

    @Inject
    public AbstractMVCHandler(@Nonnull MVCGroupManager mvcGroupManager) {
        this.mvcGroupManager = requireNonNull(mvcGroupManager, "Argument 'mvcGroupManager' must not be null");
    }

    protected MVCGroupManager getMvcGroupManager() {
        return mvcGroupManager;
    }

    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return mvcGroupManager.createMVCGroup(args, mvcType);
    }

    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return mvcGroupManager.createMVCGroup(args, mvcType, mvcId);
    }

    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType) {
        return mvcGroupManager.createMVCGroup(mvcType);
    }

    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return mvcGroupManager.createMVCGroup(mvcType, args);
    }

    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId) {
        return mvcGroupManager.createMVCGroup(mvcType, mvcId);
    }

    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return mvcGroupManager.createMVCGroup(mvcType, mvcId, args);
    }

    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return mvcGroupManager.createMVC(args, mvcType);
    }

    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return mvcGroupManager.createMVC(args, mvcType, mvcId);
    }

    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType) {
        return mvcGroupManager.createMVC(mvcType);
    }

    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return mvcGroupManager.createMVC(mvcType, args);
    }

    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId) {
        return mvcGroupManager.createMVC(mvcType, mvcId);
    }

    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return mvcGroupManager.createMVC(mvcType, mvcId, args);
    }

    public void destroyMVCGroup(@Nonnull String mvcId) {
        mvcGroupManager.destroyMVCGroup(mvcId);
    }

    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        mvcGroupManager.withMVC(mvcType, mvcId, handler);
    }

    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        mvcGroupManager.withMVC(args, mvcType, handler);
    }

    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        mvcGroupManager.withMVC(args, mvcType, mvcId, handler);
    }

    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        mvcGroupManager.withMVC(mvcType, args, handler);
    }

    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        mvcGroupManager.withMVC(mvcType, handler);
    }

    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        mvcGroupManager.withMVC(mvcType, mvcId, args, handler);
    }

    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        mvcGroupManager.withMVCGroup(mvcType, mvcId, handler);
    }

    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        mvcGroupManager.withMVCGroup(args, mvcType, handler);
    }

    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        mvcGroupManager.withMVCGroup(args, mvcType, mvcId, handler);
    }

    public void withMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        mvcGroupManager.withMVCGroup(mvcType, args, handler);
    }

    public void withMVCGroup(@Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        mvcGroupManager.withMVCGroup(mvcType, handler);
    }

    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        mvcGroupManager.withMVCGroup(mvcType, mvcId, args, handler);
    }
}
