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
import basilisk.core.artifact.BasiliskService;
import basilisk.core.artifact.BasiliskServiceClass;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Base implementation of the BasiliskService interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractBasiliskService extends AbstractBasiliskArtifact implements BasiliskService {
    public AbstractBasiliskService() {

    }

    /**
     * Creates a new instance of this class.
     *
     * @param application the BasiliskApplication that holds this artifact.
     * @deprecated Basilisk prefers field injection over constructor injector for artifacts as of 2.1.0
     */
    @Inject
    @Deprecated
    public AbstractBasiliskService(@Nonnull BasiliskApplication application) {
        super(application);
    }

    @Nonnull
    @Override
    protected String getArtifactType() {
        return BasiliskServiceClass.TYPE;
    }
}
