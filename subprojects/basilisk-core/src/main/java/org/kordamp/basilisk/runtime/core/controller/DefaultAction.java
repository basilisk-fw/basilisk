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
import basilisk.core.controller.ActionManager;
import basilisk.core.controller.ActionMetadata;
import basilisk.core.threading.UIThreadManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultAction extends AbstractAction {
    private final DefaultToolkitAction toolkitAction;

    public DefaultAction(@Nonnull final UIThreadManager uiThreadManager, @Nonnull final ActionManager actionManager, @Nonnull final BasiliskController controller, @Nonnull final ActionMetadata actionMetadata) {
        super(actionManager, controller, actionMetadata);
        requireNonNull(uiThreadManager, "Argument 'uiThreadManager' must not be null");

        toolkitAction = new DefaultToolkitAction(new Runnable() {
            @Override
            public void run() {
                actionManager.invokeAction(controller, actionMetadata.getActionName());
            }
        });
        nameProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, final String n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setName(n);
                    }
                });
            }
        });
    }

    @Nonnull
    public Object getToolkitAction() {
        return toolkitAction;
    }

    protected void doExecute(Object... args) {
        toolkitAction.execute();
    }

    @Override
    protected void doInitialize() {
        toolkitAction.setName(getName());
    }
}