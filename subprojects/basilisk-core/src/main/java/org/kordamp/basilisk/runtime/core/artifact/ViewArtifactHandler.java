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
import basilisk.core.artifact.BasiliskClass;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.artifact.BasiliskViewClass;
import basilisk.inject.Typed;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * Handler for 'View' artifacts.
 *
 * @author Andres Almiray
 */
@Typed(BasiliskView.class)
public class ViewArtifactHandler extends AbstractArtifactHandler<BasiliskView> {
    @Inject
    public ViewArtifactHandler(@Nonnull BasiliskApplication application) {
        super(application, BasiliskView.class, BasiliskViewClass.TYPE, BasiliskViewClass.TRAILING);
    }

    @Nonnull
    public BasiliskClass newBasiliskClassInstance(@Nonnull Class<BasiliskView> clazz) {
        requireNonNull(clazz, ERROR_CLASS_NULL);
        return new DefaultBasiliskViewClass(getApplication(), clazz);
    }
}