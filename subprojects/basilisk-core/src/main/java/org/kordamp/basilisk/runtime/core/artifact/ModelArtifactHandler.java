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
import basilisk.core.artifact.BasiliskClass;
import basilisk.core.artifact.BasiliskModel;
import basilisk.core.artifact.BasiliskModelClass;
import basilisk.inject.Typed;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * Handler for 'Model' artifacts.
 *
 * @author Andres Almiray
 */
@Typed(BasiliskModel.class)
public class ModelArtifactHandler extends AbstractArtifactHandler<BasiliskModel> {
    @Inject
    public ModelArtifactHandler(@Nonnull BasiliskApplication application) {
        super(application, BasiliskModel.class, BasiliskModelClass.TYPE, BasiliskModelClass.TRAILING);
    }

    @Nonnull
    public BasiliskClass newBasiliskClassInstance(@Nonnull Class<BasiliskModel> clazz) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        return new DefaultBasiliskModelClass(getApplication(), clazz);
    }
}