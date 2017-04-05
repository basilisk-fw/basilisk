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
import basilisk.core.artifact.BasiliskControllerClass;
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskModelClass;
import basilisk.core.artifact.BasiliskMvcArtifact;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.artifact.BasiliskViewClass;
import basilisk.core.mvc.MVCFunction;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.mvc.MVCGroupConfiguration;
import basilisk.core.mvc.MVCGroupFunction;
import basilisk.core.mvc.MVCGroupManager;
import basilisk.core.mvc.TypedMVCGroup;
import basilisk.core.mvc.TypedMVCGroupFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static basilisk.util.BasiliskClassUtils.requireState;
import static basilisk.util.BasiliskClassUtils.setPropertyOrFieldValue;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@code MVCGroup} interface
 *
 * @author Andres Almiray
 */
public abstract class AbstractMVCGroup extends AbstractMVCHandler implements MVCGroup {
    protected final MVCGroupConfiguration configuration;
    protected final String mvcId;
    protected final Context context;
    protected final Map<String, Object> members = new LinkedHashMap<>();
    protected final Map<String, MVCGroup> children = new LinkedHashMap<>();
    private final Object[] lock = new Object[0];
    protected MVCGroup parentGroup;
    private boolean alive;
    private final List<Object> injectedInstances = new ArrayList<>();

    public AbstractMVCGroup(@Nonnull MVCGroupManager mvcGroupManager, @Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId, @Nonnull Map<String, Object> members, @Nullable MVCGroup parentGroup) {
        super(mvcGroupManager);
        this.configuration = requireNonNull(configuration, "Argument 'configuration' must not be null");
        this.mvcId = isBlank(mvcId) ? configuration.getMvcType() + "-" + UUID.randomUUID().toString() : mvcId;
        this.members.putAll(requireNonNull(members, "Argument 'members' must not be null"));
        this.alive = true;
        this.parentGroup = parentGroup;
        this.context = mvcGroupManager.newContext(parentGroup);

        for (Object o : this.members.values()) {
            if (o instanceof BasiliskMvcArtifact) {
                setPropertyOrFieldValue(o, "mvcGroup", this);
            }
        }
    }

    @Nonnull
    public List<Object> getInjectedInstances() {
        return injectedInstances;
    }

    @Nonnull
    @Override
    public Context getContext() {
        return context;
    }

    @Nullable
    @Override
    public MVCGroup getParentGroup() {
        return parentGroup;
    }

    @Nonnull
    @Override
    public MVCGroupConfiguration getConfiguration() {
        return configuration;
    }

    @Nonnull
    @Override
    public String getMvcType() {
        return configuration.getMvcType();
    }

    @Nonnull
    @Override
    public String getMvcId() {
        return mvcId;
    }

    @Nullable
    @Override
    public BasiliskModel getModel() {
        return (BasiliskModel) getMember(BasiliskModelClass.TYPE);
    }

    @Nullable
    @Override
    public BasiliskView getView() {
        return (BasiliskView) getMember(BasiliskViewClass.TYPE);
    }

    @Nullable
    @Override
    public BasiliskController getController() {
        return (BasiliskController) getMember(BasiliskControllerClass.TYPE);
    }

    @Nullable
    @Override
    public Object getMember(@Nonnull String name) {
        requireNonBlank(name, "Argument 'name' must not be blank");
        checkIfAlive();
        return members.get(name);
    }

    @Nonnull
    @Override
    public Map<String, Object> getMembers() {
        checkIfAlive();
        return unmodifiableMap(members);
    }

    @Override
    public void destroy() {
        if (isAlive()) {
            List<String> childrenIds = new ArrayList<>(children.keySet());
            Collections.reverse(childrenIds);
            for (String id : childrenIds) {
                getMvcGroupManager().destroyMVCGroup(id);
            }
            getMvcGroupManager().destroyMVCGroup(mvcId);
            members.clear();
            children.clear();
            if (parentGroup != null) {
                parentGroup.notifyMVCGroupDestroyed(mvcId);
            }
            parentGroup = null;
            context.destroy();
            synchronized (lock) {
                alive = false;
            }
        }
    }

    @Override
    public void notifyMVCGroupDestroyed(@Nonnull String mvcId) {
        requireNonBlank(mvcId, "Argument 'mvcId' must not be blank");
        children.remove(mvcId);
    }

    @Override
    public boolean isAlive() {
        synchronized (lock) {
            return alive;
        }
    }

    protected void checkIfAlive() {
        requireState(isAlive(), "Group " + getMvcType() + ":" + mvcId + " has been destroyed already.");
    }


    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId) {
        return manageChildGroup(super.createMVCGroup(mvcType, mvcId, injectParentGroup()));
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType) {
        return manageChildGroup(super.createMVCGroup(mvcType, injectParentGroup()));
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return manageChildGroup(super.createMVCGroup(injectParentGroup(args), mvcType));
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return manageChildGroup(super.createMVCGroup(injectParentGroup(args), mvcType, mvcId));
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return manageChildGroup(super.createMVCGroup(mvcType, injectParentGroup(args)));
    }

    @Nonnull
    @Override
    public MVCGroup createMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return manageChildGroup(super.createMVCGroup(mvcType, mvcId, injectParentGroup(args)));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return manageTypedChildGroup(super.createMVCGroup(mvcType, mvcId, injectParentGroup()));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType) {
        return manageTypedChildGroup(super.createMVCGroup(mvcType, injectParentGroup()));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType) {
        return manageTypedChildGroup(super.createMVCGroup(injectParentGroup(args), mvcType));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return manageTypedChildGroup(super.createMVCGroup(injectParentGroup(args), mvcType, mvcId));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args) {
        return manageTypedChildGroup(super.createMVCGroup(mvcType, injectParentGroup(args)));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> MVC createMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return manageTypedChildGroup(super.createMVCGroup(mvcType, mvcId, injectParentGroup(args)));
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId) {
        return manageChildGroup(super.createMVC(mvcType, mvcId, injectParentGroup()));
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType) {
        return manageChildGroup(super.createMVC(mvcType, injectParentGroup()));
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType) {
        return manageChildGroup(super.createMVC(injectParentGroup(args), mvcType));
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId) {
        return manageChildGroup(super.createMVC(injectParentGroup(args), mvcType, mvcId));
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args) {
        return manageChildGroup(super.createMVC(mvcType, injectParentGroup(args)));
    }

    @Nonnull
    @Override
    public List<? extends BasiliskMvcArtifact> createMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return manageChildGroup(super.createMVC(mvcType, mvcId, injectParentGroup(args)));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return manageChildGroup(super.createMVC(mvcType, mvcId, injectParentGroup()));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType) {
        return manageChildGroup(super.createMVC(mvcType, injectParentGroup()));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType) {
        return manageChildGroup(super.createMVC(injectParentGroup(args), mvcType));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId) {
        return manageChildGroup(super.createMVC(injectParentGroup(args), mvcType, mvcId));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args) {
        return manageChildGroup(super.createMVC(mvcType, injectParentGroup(args)));
    }

    @Nonnull
    @Override
    public <MVC extends TypedMVCGroup> List<? extends BasiliskMvcArtifact> createMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args) {
        return manageChildGroup(super.createMVC(mvcType, mvcId, injectParentGroup(args)));
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(injectParentGroup(args), mvcType, new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(injectParentGroup(args), mvcType, mvcId, new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, injectParentGroup(args), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, mvcId, injectParentGroup(args), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, injectParentGroup(), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, mvcId, injectParentGroup(), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(injectParentGroup(args), mvcType, new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(injectParentGroup(args), mvcType, mvcId, new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, injectParentGroup(args), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, mvcId, injectParentGroup(args), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, injectParentGroup(), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup, M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> void withMVC(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull MVCFunction<M, V, C> handler) {
        super.withMVC(mvcType, mvcId, injectParentGroup(), new MVCFunctionDecorator<>(handler));
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        super.withMVCGroup(injectParentGroup(args), mvcType, new MVCGroupFunctionDecorator(handler));
    }

    @Override
    public void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull String mvcType, @Nonnull String mvcId, @Nonnull MVCGroupFunction handler) {
        super.withMVCGroup(injectParentGroup(args), mvcType, mvcId, new MVCGroupFunctionDecorator(handler));
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        super.withMVCGroup(mvcType, injectParentGroup(args), new MVCGroupFunctionDecorator(handler));
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull MVCGroupFunction handler) {
        super.withMVCGroup(mvcType, mvcId, injectParentGroup(args), new MVCGroupFunctionDecorator(handler));
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull MVCGroupFunction handler) {
        super.withMVCGroup(mvcType, injectParentGroup(), new MVCGroupFunctionDecorator(handler));
    }

    @Override
    public void withMVCGroup(@Nonnull String mvcType, @Nonnull String mvcId, @Nonnull final MVCGroupFunction handler) {
        super.withMVCGroup(mvcType, mvcId, injectParentGroup(), new MVCGroupFunctionDecorator(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        super.withMVCGroup(injectParentGroup(args), mvcType, new TypedMVCGroupFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Map<String, Object> args, @Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        super.withMVCGroup(injectParentGroup(args), mvcType, mvcId, new TypedMVCGroupFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull Map<String, Object> args, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        super.withMVCGroup(mvcType, injectParentGroup(args), new TypedMVCGroupFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull Map<String, Object> args, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        super.withMVCGroup(mvcType, mvcId, injectParentGroup(args), new TypedMVCGroupFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull TypedMVCGroupFunction<MVC> handler) {
        super.withMVCGroup(mvcType, injectParentGroup(), new TypedMVCGroupFunctionDecorator<>(handler));
    }

    @Override
    public <MVC extends TypedMVCGroup> void withMVCGroup(@Nonnull Class<? extends MVC> mvcType, @Nonnull String mvcId, @Nonnull final TypedMVCGroupFunction<MVC> handler) {
        super.withMVCGroup(mvcType, mvcId, injectParentGroup(), new TypedMVCGroupFunctionDecorator<>(handler));
    }

    @Nonnull
    private MVCGroup manageChildGroup(@Nonnull MVCGroup group) {
        children.put(group.getMvcId(), group);
        return group;
    }

    @Nonnull
    private <MVC extends TypedMVCGroup> MVC manageTypedChildGroup(@Nonnull MVC group) {
        children.put(group.getMvcId(), group);
        return group;
    }

    @Nonnull
    private List<? extends BasiliskMvcArtifact> manageChildGroup(@Nonnull List<? extends BasiliskMvcArtifact> artifacts) {
        MVCGroup group = null;
        for (BasiliskMvcArtifact artifact : artifacts) {
            if (artifact != null) {
                group = artifact.getMvcGroup();
                break;
            }
        }
        if (group != null) {
            children.put(group.getMvcId(), group);
        }
        return artifacts;
    }

    @Nonnull
    @Override
    public Map<String, MVCGroup> getChildrenGroups() {
        return unmodifiableMap(children);
    }

    @Nonnull
    private Map<String, Object> injectParentGroup() {
        return injectParentGroup(new LinkedHashMap<String, Object>());
    }

    @Nonnull
    private Map<String, Object> injectParentGroup(@Nonnull Map<String, Object> args) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("parentGroup", this);
        map.putAll(args);
        return map;
    }

    private final class MVCFunctionDecorator<M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> implements MVCFunction<M, V, C> {
        private final MVCFunction<M, V, C> delegate;

        private MVCFunctionDecorator(MVCFunction<M, V, C> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void apply(@Nullable M model, @Nullable V view, @Nullable C controller) {
            MVCGroup group = null;
            if (model != null) { group = model.getMvcGroup(); }
            if (view != null) { group = view.getMvcGroup(); }
            if (controller != null) { group = controller.getMvcGroup(); }

            if (group != null) { children.put(group.getMvcId(), group); }
            delegate.apply(model, view, controller);
            if (group != null) { children.remove(group.getMvcId()); }
        }
    }

    private final class MVCGroupFunctionDecorator implements MVCGroupFunction {
        private final MVCGroupFunction delegate;

        private MVCGroupFunctionDecorator(MVCGroupFunction delegate) {
            this.delegate = delegate;
        }

        @Override
        public void apply(@Nullable MVCGroup group) {
            children.put(group.getMvcId(), group);
            delegate.apply(group);
            children.remove(group.getMvcId());
        }
    }

    private final class TypedMVCGroupFunctionDecorator<MVC extends TypedMVCGroup> implements TypedMVCGroupFunction<MVC> {
        private final TypedMVCGroupFunction<MVC> delegate;

        private TypedMVCGroupFunctionDecorator(TypedMVCGroupFunction<MVC> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void apply(@Nullable MVC group) {
            children.put(group.getMvcId(), group);
            delegate.apply(group);
            children.remove(group.getMvcId());
        }
    }
}
