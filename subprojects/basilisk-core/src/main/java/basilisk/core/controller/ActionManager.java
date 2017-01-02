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
package basilisk.core.controller;

import basilisk.core.artifact.BasiliskController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public interface ActionManager {
    String ACTION = "Action";

    @Nonnull
    Map<String, Action> actionsFor(@Nonnull BasiliskController controller);

    @Nullable
    Action actionFor(@Nonnull BasiliskController controller, @Nonnull String actionName);

    void createActions(@Nonnull BasiliskController controller);

    @Nonnull
    String normalizeName(@Nonnull String actionName);

    /**
     * Updates all actions currently configured.
     */
    void updateActions();

    /**
     * Updates all actions belonging to the supplied controller.
     *
     * @param controller the controller that owns the actions to be updated.
     */
    void updateActions(@Nonnull BasiliskController controller);

    /**
     * Update the action's properties using registered {@code ActionHandler}s.
     *
     * @param action the action to be updated
     */
    void updateAction(@Nonnull Action action);

    /**
     * Update the action's properties using registered {@code ActionHandler}s.
     *
     * @param controller the controller that owns the action
     * @param actionName the action's name
     */
    void updateAction(@Nonnull BasiliskController controller, @Nonnull String actionName);

    /**
     * Execute the action using registered {@code ActionHandler}s.
     *
     * @param controller the controller that owns the action
     * @param actionName the action's name
     * @param args       additional arguments to be sent to the action
     */
    void invokeAction(@Nonnull BasiliskController controller, @Nonnull String actionName, Object... args);

    /**
     * Execute the action using registered {@code ActionHandler}s.
     *
     * @param action the action to be invoked
     * @param args   additional arguments to be sent to the action
     */
    void invokeAction(@Nonnull Action action, @Nonnull Object... args);

    /**
     * Register an {@code ActionHandler} with this instance.
     *
     * @param actionHandler the handler to be added to this ActionManager
     */
    void addActionHandler(@Nonnull ActionHandler actionHandler);

    /**
     * Register an {@code ActionInterceptor} with this instance.
     *
     * @param actionInterceptor the interceptor to be added to this ActionManager
     * @deprecated use {@code addActionHandler} instead.
     */
    @Deprecated
    void addActionInterceptor(@Nonnull ActionInterceptor actionInterceptor);
}
