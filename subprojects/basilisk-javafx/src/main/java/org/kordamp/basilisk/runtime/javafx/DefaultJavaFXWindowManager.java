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

import basilisk.core.ApplicationEvent;
import basilisk.core.BasiliskApplication;
import basilisk.core.env.ApplicationPhase;
import basilisk.javafx.JavaFXWindowDisplayHandler;
import basilisk.javafx.JavaFXWindowManager;
import javafx.event.EventHandler;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.kordamp.basilisk.runtime.core.view.AbstractWindowManager;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultJavaFXWindowManager extends AbstractWindowManager<Window> implements JavaFXWindowManager {
    private final OnWindowHidingHelper onWindowHiding = new OnWindowHidingHelper();
    private final OnWindowShownHelper onWindowShown = new OnWindowShownHelper();
    private final OnWindowHiddenHelper onWindowHidden = new OnWindowHiddenHelper();

    @Inject
    @Nonnull
    public DefaultJavaFXWindowManager(@Nonnull BasiliskApplication application, @Nonnull @Named("windowDisplayHandler") JavaFXWindowDisplayHandler windowDisplayHandler) {
        super(application, windowDisplayHandler);
        requireNonNull(application.getEventRouter(), "Argument 'application.eventRouter' must not be null");
    }

    @Override
    protected void doAttach(@Nonnull Window window) {
        requireNonNull(window, ERROR_WINDOW_NULL);
        window.setOnHiding(onWindowHiding);
        window.setOnShown(onWindowShown);
        window.setOnHidden(onWindowHidden);
    }

    @Override
    protected void doDetach(@Nonnull Window window) {
        requireNonNull(window, ERROR_WINDOW_NULL);
        window.setOnHiding(null);
        window.setOnShown(null);
        window.setOnHidden(null);
    }

    @Override
    protected boolean isWindowVisible(@Nonnull Window window) {
        requireNonNull(window, ERROR_WINDOW_NULL);
        return window.isShowing();
    }

    public void handleClose(@Nonnull Window widget) {
        if (getApplication().getPhase() == ApplicationPhase.SHUTDOWN) {
            return;
        }

        List<Window> visibleWindows = new ArrayList<>();
        for (Window window : getWindows()) {
            if (window.isShowing()) {
                visibleWindows.add(window);
            }
        }

        if (isAutoShutdown() && visibleWindows.size() <= 1 && visibleWindows.contains(widget)) {
            if (!getApplication().shutdown()) show(widget);
        }
    }

    /**
     * WindowAdapter that invokes hide() when the window is about to be closed.
     *
     * @author Andres Almiray
     */
    private class OnWindowHidingHelper implements EventHandler<WindowEvent> {
        public void handle(WindowEvent event) {
            hide((Window) event.getSource());
            handleClose((Window) event.getSource());
        }
    }

    /**
     * Listener that triggers application events when a window is shown.
     *
     * @author Andres Almiray
     */
    private class OnWindowShownHelper implements EventHandler<WindowEvent> {
        /**
         * Triggers a <tt>WindowShown</tt> event with the window as sole argument
         */
        public void handle(WindowEvent windowEvent) {
            Window window = (Window) windowEvent.getSource();
            event(ApplicationEvent.WINDOW_SHOWN, asList(findWindowName(window), window));
        }
    }

    /**
     * Listener that triggers application events when a window is hidden.
     *
     * @author Andres Almiray
     */
    private class OnWindowHiddenHelper implements EventHandler<WindowEvent> {
        /**
         * Triggers a <tt>WindowHidden</tt> event with the window as sole argument
         */
        public void handle(WindowEvent windowEvent) {
            Window window = (Window) windowEvent.getSource();
            event(ApplicationEvent.WINDOW_HIDDEN, asList(findWindowName(window), window));
        }
    }
}
