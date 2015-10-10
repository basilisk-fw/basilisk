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
import basilisk.core.controller.AbortActionExecution;
import basilisk.core.controller.ActionExecutionStatus;
import basilisk.core.controller.ActionInterceptor;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @deprecated use {@code ActionHandler} instead.
 */
@Deprecated
public class AbstractActionInterceptor implements ActionInterceptor {
    private static final String ERROR_CONTROLLER_NULL = "Argument 'controller' must not be null";
    private static final String ERROR_ACTION_NAME_BLANK = "Argument 'actionName' must not be blank";

    @Override
    public void configure(@Nonnull BasiliskController controller, @Nonnull String actionName, @Nonnull Method method) {
    }

    @Nonnull
    @Override
    public Object[] before(@Nonnull BasiliskController controller, @Nonnull String actionName, @Nonnull Object[] args) {
        return new Object[0];
    }

    @Override
    public void after(@Nonnull ActionExecutionStatus status, @Nonnull BasiliskController controller, @Nonnull String actionName, @Nonnull Object[] args) {
    }

    @Override
    public boolean exception(@Nonnull Exception exception, @Nonnull BasiliskController controller, @Nonnull String actionName, @Nonnull Object[] args) {
        return false;
    }

    @Nonnull
    protected AbortActionExecution abortActionExecution() throws AbortActionExecution {
        throw new AbortActionExecution();
    }

    @Nonnull
    protected String qualifyActionName(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        requireNonNull(controller, ERROR_CONTROLLER_NULL);
        requireNonBlank(actionName, ERROR_ACTION_NAME_BLANK);
        return controller.getClass().getName() + "." + actionName;
    }
}
