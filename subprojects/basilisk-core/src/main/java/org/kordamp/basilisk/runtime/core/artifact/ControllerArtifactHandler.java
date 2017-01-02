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
import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskControllerClass;
import basilisk.inject.Typed;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * Handler for 'Controller' artifacts.
 *
 * @author Andres Almiray
 */
@Typed(BasiliskController.class)
public class ControllerArtifactHandler extends AbstractArtifactHandler<BasiliskController> {
    @Inject
    public ControllerArtifactHandler(@Nonnull BasiliskApplication application) {
        super(application, BasiliskController.class, BasiliskControllerClass.TYPE, BasiliskControllerClass.TRAILING);
    }

    @Nonnull
    public BasiliskClass newBasiliskClassInstance(@Nonnull Class<BasiliskController> clazz) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        return new DefaultBasiliskControllerClass(getApplication(), clazz);
    }
}