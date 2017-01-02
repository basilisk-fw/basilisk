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

import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.artifact.BasiliskViewClass;
import basilisk.core.controller.Action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base implementation of the BasiliskView interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractBasiliskView extends AbstractBasiliskMvcArtifact implements BasiliskView {
    @Nonnull
    @Override
    protected String getArtifactType() {
        return BasiliskViewClass.TYPE;
    }

    @Nullable
    protected Action actionFor(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        return getApplication().getActionManager().actionFor(controller, actionName);
    }
}