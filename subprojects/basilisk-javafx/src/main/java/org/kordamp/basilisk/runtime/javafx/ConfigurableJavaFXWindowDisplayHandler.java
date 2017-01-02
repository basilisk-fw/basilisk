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
package org.kordamp.basilisk.runtime.javafx;

import basilisk.core.BasiliskApplication;
import basilisk.exceptions.InstanceNotFoundException;
import basilisk.javafx.JavaFXWindowDisplayHandler;
import javafx.stage.Window;
import org.kordamp.basilisk.runtime.core.view.ConfigurableWindowDisplayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import static basilisk.util.AnnotationUtils.named;

/**
 * @author Andres Almiray
 */
public class ConfigurableJavaFXWindowDisplayHandler extends ConfigurableWindowDisplayHandler<Window> implements JavaFXWindowDisplayHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurableJavaFXWindowDisplayHandler.class);

    @Inject
    public ConfigurableJavaFXWindowDisplayHandler(@Nonnull BasiliskApplication application, @Nonnull @Named("defaultWindowDisplayHandler") JavaFXWindowDisplayHandler delegateWindowsDisplayHandler) {
        super(application, delegateWindowsDisplayHandler);
    }

    @Nonnull
    protected JavaFXWindowDisplayHandler fetchDefaultWindowDisplayHandler() {
        Object handler = windowManagerBlock().get("defaultHandler");
        return (JavaFXWindowDisplayHandler) (handler instanceof JavaFXWindowDisplayHandler ? handler : getDelegateWindowsDisplayHandler());
    }

    @Override
    protected boolean handleShowByInjectedHandler(@Nonnull String name, @Nonnull Window window) {
        try {
            JavaFXWindowDisplayHandler handler = getApplication().getInjector()
                .getInstance(JavaFXWindowDisplayHandler.class, named(name));
            LOG.trace("Showing {} with injected handler", name);
            handler.show(name, window);
            return true;
        } catch (InstanceNotFoundException infe) {
            return super.handleShowByInjectedHandler(name, window);
        }
    }

    @Override
    protected boolean handleHideByInjectedHandler(@Nonnull String name, @Nonnull Window window) {
        try {
            JavaFXWindowDisplayHandler handler = getApplication().getInjector()
                .getInstance(JavaFXWindowDisplayHandler.class, named(name));
            LOG.trace("Hiding {} with injected handler", name);
            handler.hide(name, window);
            return true;
        } catch (InstanceNotFoundException infe) {
            return super.handleHideByInjectedHandler(name, window);
        }
    }
}
