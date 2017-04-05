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

import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.mvc.TypedMVCGroup;

import javax.annotation.Nonnull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractTypedMVCGroup<M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController> extends DelegatingMVCGroup implements TypedMVCGroup<M, V, C> {
    public AbstractTypedMVCGroup(@Nonnull MVCGroup delegate) {
        super(delegate);
    }

    @Nonnull
    @Override
    public M model() {
        return (M) delegate().getModel();
    }

    @Nonnull
    @Override
    public V view() {
        return (V) delegate().getView();
    }

    @Nonnull
    @Override
    public C controller() {
        return (C) delegate().getController();
    }
}
