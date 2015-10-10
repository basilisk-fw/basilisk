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
package org.kordamp.basilisk.runtime.javafx;

import basilisk.core.controller.ActionManager;
import basilisk.core.injection.Module;
import basilisk.core.threading.UIThreadManager;
import basilisk.core.view.WindowManager;
import basilisk.javafx.JavaFXWindowDisplayHandler;
import org.kordamp.basilisk.runtime.core.injection.AbstractModule;
import org.kordamp.basilisk.runtime.javafx.controller.JavaFXActionManager;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;

import static basilisk.util.AnnotationUtils.named;

/**
 * @author Andres Almiray
 */
@Named("javafx")
@ServiceProviderFor(Module.class)
public class JavaFXModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(JavaFXWindowDisplayHandler.class)
            .withClassifier(named("defaultWindowDisplayHandler"))
            .to(DefaultJavaFXWindowDisplayHandler.class)
            .asSingleton();

        bind(JavaFXWindowDisplayHandler.class)
            .withClassifier(named("windowDisplayHandler"))
            .to(ConfigurableJavaFXWindowDisplayHandler.class)
            .asSingleton();

        bind(WindowManager.class)
            .to(DefaultJavaFXWindowManager.class)
            .asSingleton();

        bind(UIThreadManager.class)
            .to(JavaFXUIThreadManager.class)
            .asSingleton();

        bind(ActionManager.class)
            .to(JavaFXActionManager.class)
            .asSingleton();
        // end::bindings[]
    }
}
