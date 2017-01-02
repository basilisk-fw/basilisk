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
import basilisk.exceptions.BasiliskException;

/**
 * Exception thrown when {@code ActionManager} can't find an action
 * for a particular controller.
 *
 * @author Andres Almiray
 */
public class MissingControllerActionException extends BasiliskException {
    private static final long serialVersionUID = -6650690378356878061L;

    private final Class<? extends BasiliskController> controllerClass;
    private final String actionName;

    public MissingControllerActionException(Class<? extends BasiliskController> controllerClass, String actionName) {
        this(null, controllerClass, actionName, null);
    }

    public MissingControllerActionException(String message, Class<? extends BasiliskController> controllerClass, String actionName) {
        this(message, controllerClass, actionName, null);
    }

    public MissingControllerActionException(String message, Class<? extends BasiliskController> controllerClass, String actionName, Throwable cause) {
        super(message, cause);
        this.controllerClass = controllerClass;
        this.actionName = actionName;
    }

    public MissingControllerActionException(Class<? extends BasiliskController> controllerClass, String actionName, Throwable cause) {
        this("", controllerClass, actionName, cause);
    }

    public Class<? extends BasiliskController> getControllerClass() {
        return controllerClass;
    }

    public String getActionName() {
        return actionName;
    }
}
