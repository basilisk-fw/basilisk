/*
 * Copyright 2008-2016 the original author or authors.
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
package org.kordamp.basilisk.runtime.javafx.controller;

import basilisk.core.BasiliskApplication;
import basilisk.core.artifact.BasiliskController;
import basilisk.core.controller.Action;
import org.kordamp.basilisk.runtime.core.controller.AbstractActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static basilisk.util.BasiliskApplicationUtils.isMacOSX;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.TypeUtils.castToBoolean;

/**
 * @author Andres Almiray
 */
public class JavaFXActionManager extends AbstractActionManager {
    private static final Logger LOG = LoggerFactory.getLogger(JavaFXActionManager.class);

    @Inject
    public JavaFXActionManager(@Nonnull BasiliskApplication application) {
        super(application);
    }

    @Nonnull
    @Override
    protected Action createControllerAction(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        return new JavaFXBasiliskControllerAction(getUiThreadManager(), this, controller, actionName);
    }

    @Override
    protected void doConfigureAction(@Nonnull Action action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        JavaFXBasiliskControllerAction javafxAction = (JavaFXBasiliskControllerAction) action;

        String rsAccelerator = msg(keyPrefix, normalizeNamed, "accelerator", "");
        if (!isBlank(rsAccelerator)) {
            //noinspection ConstantConditions
            if (!isMacOSX && rsAccelerator.contains("meta") && !rsAccelerator.contains("ctrl")) {
                rsAccelerator = rsAccelerator.replace("meta", "ctrl");
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".accelerator = " + rsAccelerator);
            }
            javafxAction.setAccelerator(rsAccelerator);
        }

        String rsDescription = msg(keyPrefix, normalizeNamed, "description", "");
        if (!isBlank(rsDescription)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".description = " + rsDescription);
            }
            javafxAction.setDescription(rsDescription);
        }

        String rsIcon = msg(keyPrefix, normalizeNamed, "icon", "");
        if (!isBlank(rsIcon)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".icon = " + rsIcon);
            }
            javafxAction.setIcon(rsIcon);
        }

        String rsImage = msg(keyPrefix, normalizeNamed, "image", "");
        if (!isBlank(rsImage)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".image = " + rsImage);
            }
            javafxAction.setImage(rsImage);
        }

        String rsEnabled = msg(keyPrefix, normalizeNamed, "enabled", "true");
        if (!isBlank(rsEnabled)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".enabled = " + rsEnabled);
            }
            javafxAction.setEnabled(castToBoolean(rsEnabled));
        }

        String rsSelected = msg(keyPrefix, normalizeNamed, "selected", "false");
        if (!isBlank(rsSelected)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".selected = " + rsSelected);
            }
            javafxAction.setSelected(castToBoolean(rsSelected));
        }

        String rsVisible = msg(keyPrefix, normalizeNamed, "visible", "true");
        if (!isBlank(rsVisible)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".visible = " + rsVisible);
            }
            javafxAction.setVisible(castToBoolean(rsVisible));
        }

        String rsStyleClass = msg(keyPrefix, normalizeNamed, "styleclass", "");
        if (!isBlank(rsStyleClass)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".styleclass = " + rsStyleClass);
            }
            javafxAction.setStyleClass(rsStyleClass);
        }

        String rsStyle = msg(keyPrefix, normalizeNamed, "style", "");
        if (!isBlank(rsStyle)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".style = " + rsStyle);
            }
            javafxAction.setStyle(rsStyle);
        }

        String rsGraphicStyleClass = msg(keyPrefix, normalizeNamed, "graphic_styleclass", "");
        if (!isBlank(rsGraphicStyleClass)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".graphic_styleclass = " + rsGraphicStyleClass);
            }
            javafxAction.setGraphicStyleClass(rsGraphicStyleClass);
        }

        String rsGraphicStyle = msg(keyPrefix, normalizeNamed, "graphic_style", "");
        if (!isBlank(rsGraphicStyle)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(keyPrefix + normalizeNamed + ".graphic_style = " + rsGraphicStyle);
            }
            javafxAction.setGraphicStyle(rsGraphicStyle);
        }
    }
}
