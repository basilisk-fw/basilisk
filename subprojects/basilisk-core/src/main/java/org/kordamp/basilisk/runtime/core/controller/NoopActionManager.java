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
package org.kordamp.basilisk.runtime.core.controller;

import basilisk.core.artifact.BasiliskController;
import basilisk.core.controller.Action;
import basilisk.core.controller.ActionHandler;
import basilisk.core.controller.ActionInterceptor;
import basilisk.core.controller.ActionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static basilisk.util.BasiliskNameUtils.uncapitalize;

/**
 * @author Andres Almiray
 */
public class NoopActionManager implements ActionManager {
    @Nonnull
    @Override
    public Map<String, Action> actionsFor(@Nonnull BasiliskController controller) {
        return Collections.emptyMap();
    }

    @Nullable
    @Override
    public Action actionFor(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        return null;
    }

    @Override
    public void createActions(@Nonnull BasiliskController controller) {

    }

    @Nonnull
    @Override
    public String normalizeName(@Nonnull String actionName) {
        requireNonBlank(actionName, "Argument 'actionName' must not be blank");
        if (actionName.endsWith(ACTION)) {
            actionName = actionName.substring(0, actionName.length() - ACTION.length());
        }
        return uncapitalize(actionName);
    }

    @Override
    public void updateActions() {

    }

    @Override
    public void updateActions(@Nonnull BasiliskController controller) {

    }

    @Override
    public void updateAction(@Nonnull Action action) {

    }

    @Override
    public void updateAction(@Nonnull BasiliskController controller, @Nonnull String actionName) {

    }

    @Override
    public void invokeAction(@Nonnull BasiliskController controller, @Nonnull String actionName, Object... args) {

    }

    @Override
    public void invokeAction(@Nonnull Action action, @Nonnull Object... args) {

    }

    @Override
    public void addActionHandler(@Nonnull ActionHandler actionHandler) {

    }

    @Deprecated
    @Override
    public void addActionInterceptor(@Nonnull ActionInterceptor actionInterceptor) {

    }
}
