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

import basilisk.core.Context;
import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskMvcArtifact;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.mvc.MVCFunction;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.mvc.MVCGroupConfiguration;
import basilisk.core.mvc.MVCGroupFunction;
import basilisk.core.mvc.TypedMVCGroup;
import basilisk.core.mvc.TypedMVCGroupFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class DelegatingMVCGroup implements MVCGroup {
    private final MVCGroup delegate;

    public DelegatingMVCGroup(@Nonnull basilisk.core.mvc.MVCGroup delegate) {
        this.delegate = requireNonNull(delegate, "delegate");
    }

    @Nonnull
    public MVCGroup delegate() {
        return delegate;
    }

    @Override
    @Nonnull
    public MVCGroupConfiguration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    @Nonnull
    public String getMvcType() {
        return delegate.getMvcType();
    }

    @Override
    @Nonnull
    public String getMvcId() {
        return delegate.getMvcId();
    }

    @Override
    @Nullable
    public BasiliskModel getModel() {
        return delegate.getModel();
    }

    @Override
    @Nullable
    public BasiliskView getView() {
        return delegate.getView();
    }

    @Override
    @Nullable
    public BasiliskController getController() {
        return delegate.getController();
    }

    @Override
    @Nullable
    public Object getMember(@Nonnull String name) {
        return delegate.getMember(name);
    }

    @Override
    @Nonnull
    public Map<String, Object> getMembers() {
        return delegate.getMembers();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public boolean isAlive() {
        return delegate.isAlive();
    }

    @Override
    @Nonnull
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    @Nullable
    public basilisk.core.mvc.MVCGroup getParentGroup() {
        return delegate.getParentGroup();
    }

    @Override
    @Nonnull
    public Map<String, MVCGroup> getChildrenGroups() {
        return delegate.getChildrenGroups();
    }

    @Override
    public void notifyMVCGroupDestroyed(@Nonnull String mvcId) {
        delegate.notifyMVCGroupDestroyed(mvcId);
    }

    @Override
    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType) {
        return delegate.createMVCGroup(mvcType);
    }

    @Override
    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId) {
        return delegate.createMVCGroup(mvcType, mvcId);
    }

    @Override
    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return delegate.createMVCGroup(args, mvcType);
    }

    @Override
    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return delegate.createMVCGroup(mvcType, args);
    }

    @Override
    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return delegate.createMVCGroup(args, mvcType, mvcId);
    }

    @Override
    @Nonnull
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return delegate.createMVCGroup(mvcType, mvcId, args);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType) {
        return delegate.createMVCGroup(mvcType);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return delegate.createMVCGroup(mvcType, mvcId);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType) {
        return delegate.createMVCGroup(args, mvcType);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args) {
        return delegate.createMVCGroup(mvcType, args);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return delegate.createMVCGroup(args, mvcType, mvcId);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return delegate.createMVCGroup(mvcType, mvcId, args);
    }

    @Override
    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType) {
        return delegate.createMVC(mvcType);
    }

    @Override
    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return delegate.createMVC(args, mvcType);
    }

    @Override
    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return delegate.createMVC(mvcType, args);
    }

    @Override
    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId) {
        return delegate.createMVC(mvcType, mvcId);
    }

    @Override
    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return delegate.createMVC(args, mvcType, mvcId);
    }

    @Override
    @Nonnull
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return delegate.createMVC(mvcType, mvcId, args);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType) {
        return delegate.createMVC(mvcType);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType) {
        return delegate.createMVC(args, mvcType);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args) {
        return delegate.createMVC(mvcType, args);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return delegate.createMVC(mvcType, mvcId);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return delegate.createMVC(args, mvcType, mvcId);
    }

    @Override
    @Nonnull
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return delegate.createMVC(mvcType, mvcId, args);
    }

    @Override
    public void destroyMVCGroup(@Nonnull String mvcId) {
        delegate.destroyMVCGroup(mvcId);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, mvcId, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, mvcId, args, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(args, mvcType, mvcId, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, args, handler);
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(args, mvcType, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, mvcId, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, mvcId, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(args, mvcType, mvcId, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(mvcType, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        delegate.withMVC(args, mvcType, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        delegate.withMVCGroup(mvcType, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        delegate.withMVCGroup(mvcType, mvcId, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        delegate.withMVCGroup(mvcType, mvcId, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        delegate.withMVCGroup(args, mvcType, mvcId, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        delegate.withMVCGroup(mvcType, args, handler);
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        delegate.withMVCGroup(args, mvcType, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        delegate.withMVCGroup(mvcType, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        delegate.withMVCGroup(mvcType, mvcId, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        delegate.withMVCGroup(mvcType, mvcId, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        delegate.withMVCGroup(args, mvcType, mvcId, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        delegate.withMVCGroup(mvcType, args, handler);
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        delegate.withMVCGroup(args, mvcType, handler);
    }
}
