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
package org.kordamp.basilisk.runtime.core.controller;

import basilisk.core.artifact.BasiliskController;
import basilisk.core.controller.Action;
import basilisk.core.controller.ActionFactory;
import basilisk.core.controller.ActionManager;
import basilisk.core.controller.ActionMetadata;
import basilisk.core.threading.UIThreadManager;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * @author Andres Almiray
 * @since 2.11.0
 */
public class DefaultActionFactory implements ActionFactory {
    @Inject
    private UIThreadManager uiThreadManager;

    @Inject
    private ActionManager actionManager;

    @Nonnull
    @Override
    public Action create(@Nonnull BasiliskController controller, @Nonnull ActionMetadata actionMetadata) {
        return new DefaultAction(uiThreadManager, actionManager, controller, actionMetadata);
    }
}
