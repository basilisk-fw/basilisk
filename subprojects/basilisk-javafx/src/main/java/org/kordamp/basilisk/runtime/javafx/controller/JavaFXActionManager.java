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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.kordamp.basilisk.runtime.core.controller.AbstractActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Locale;

import static basilisk.util.BasiliskApplicationUtils.isMacOSX;
import static basilisk.util.BasiliskNameUtils.getNaturalName;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.TypeUtils.castToBoolean;

/**
 * @author Andres Almiray
 */
public class JavaFXActionManager extends AbstractActionManager {
    private static final Logger LOG = LoggerFactory.getLogger(JavaFXActionManager.class);

    private static final String EMPTY_STRING = "";
    private static final String DOT = ".";
    private static final String EQUALS = " = ";
    private static final String KEY_NAME = "name";
    private static final String KEY_ACCELERATOR = "accelerator";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_ICON = "icon";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SELECTED = "selected";
    private static final String KEY_VISIBLE = "visible";
    private static final String KEY_STYLE_CLASS = "styleclass";
    private static final String KEY_STYLE = "style";
    private static final String KEY_GRAPHIC_STYLE_CLASS = "graphic_styleclass";
    private static final String KEY_GRAPHIC_STYLE = "graphic_style";
    private static final String KEY_CTRL = "ctrl";
    private static final String KEY_META = "meta";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

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
    protected void doConfigureAction(@Nonnull final Action action, @Nonnull final BasiliskController controller, @Nonnull final String normalizeNamed, @Nonnull final String keyPrefix) {
        controller.getApplication().localeProperty().addListener(new ChangeListener<Locale>() {
            @Override
            public void changed(ObservableValue<? extends Locale> observable, Locale oldValue, Locale newValue) {
                configureAction((JavaFXBasiliskControllerAction) action, controller, normalizeNamed, keyPrefix);
            }
        });
        configureAction((JavaFXBasiliskControllerAction) action, controller, normalizeNamed, keyPrefix);
    }

    protected void configureAction(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        resolveName(action, controller, normalizeNamed, keyPrefix);
        resolveAccelerator(action, controller, normalizeNamed, keyPrefix);
        resolveDescription(action, controller, normalizeNamed, keyPrefix);
        resolveIcon(action, controller, normalizeNamed, keyPrefix);
        resolveImage(action, controller, normalizeNamed, keyPrefix);
        resolveEnabled(action, controller, normalizeNamed, keyPrefix);
        resolveSelected(action, controller, normalizeNamed, keyPrefix);
        resolveVisible(action, controller, normalizeNamed, keyPrefix);
        resolveStyleClass(action, controller, normalizeNamed, keyPrefix);
        resolveStyle(action, controller, normalizeNamed, keyPrefix);
        resolveGraphicStyleClass(action, controller, normalizeNamed, keyPrefix);
        resolveGraphicStyle(action, controller, normalizeNamed, keyPrefix);
    }

    protected void resolveName(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsActionName = msg(keyPrefix, normalizeNamed, KEY_NAME, getNaturalName(normalizeNamed));
        if (!isBlank(rsActionName)) {
            trace(keyPrefix + normalizeNamed, KEY_NAME, rsActionName);
            action.setName(rsActionName);
        }
    }

    protected void resolveAccelerator(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsAccelerator = msg(keyPrefix, normalizeNamed, KEY_ACCELERATOR, EMPTY_STRING);
        if (!isBlank(rsAccelerator)) {
            //noinspection ConstantConditions
            if (!isMacOSX && rsAccelerator.contains(KEY_META) && !rsAccelerator.contains(KEY_CTRL)) {
                rsAccelerator = rsAccelerator.replace(KEY_META, KEY_CTRL);
            }
            trace(keyPrefix + normalizeNamed, KEY_ACCELERATOR, rsAccelerator);
            action.setAccelerator(rsAccelerator);
        }
    }

    protected void resolveDescription(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsDescription = msg(keyPrefix, normalizeNamed, KEY_DESCRIPTION, EMPTY_STRING);
        if (!isBlank(rsDescription)) {
            trace(keyPrefix + normalizeNamed, KEY_DESCRIPTION, rsDescription);
            action.setDescription(rsDescription);
        }
    }

    protected void resolveIcon(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsIcon = msg(keyPrefix, normalizeNamed, KEY_ICON, EMPTY_STRING);
        if (!isBlank(rsIcon)) {
            trace(keyPrefix + normalizeNamed, KEY_ICON, rsIcon);
            action.setIcon(rsIcon);
        }
    }

    protected void resolveImage(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsImage = msg(keyPrefix, normalizeNamed, KEY_IMAGE, EMPTY_STRING);
        if (!isBlank(rsImage)) {
            trace(keyPrefix + normalizeNamed, KEY_IMAGE, rsImage);
            action.setImage(rsImage);
        }
    }

    protected void resolveEnabled(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsEnabled = msg(keyPrefix, normalizeNamed, KEY_ENABLED, TRUE);
        if (!isBlank(rsEnabled)) {
            trace(keyPrefix + normalizeNamed, KEY_ENABLED, rsEnabled);
            action.setEnabled(castToBoolean(rsEnabled));
        }
    }

    protected void resolveSelected(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsSelected = msg(keyPrefix, normalizeNamed, KEY_SELECTED, FALSE);
        if (!isBlank(rsSelected)) {
            trace(keyPrefix + normalizeNamed, KEY_SELECTED, rsSelected);
            action.setSelected(castToBoolean(rsSelected));
        }
    }

    protected void resolveVisible(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsVisible = msg(keyPrefix, normalizeNamed, KEY_VISIBLE, TRUE);
        if (!isBlank(rsVisible)) {
            trace(keyPrefix + normalizeNamed, KEY_VISIBLE, rsVisible);
            action.setVisible(castToBoolean(rsVisible));
        }
    }

    protected void resolveStyleClass(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsStyleClass = msg(keyPrefix, normalizeNamed, KEY_STYLE_CLASS, EMPTY_STRING);
        if (!isBlank(rsStyleClass)) {
            trace(keyPrefix + normalizeNamed, KEY_STYLE_CLASS, rsStyleClass);
            action.setStyleClass(rsStyleClass);
        }
    }

    protected void resolveStyle(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsStyle = msg(keyPrefix, normalizeNamed, KEY_STYLE, EMPTY_STRING);
        if (!isBlank(rsStyle)) {
            trace(keyPrefix + normalizeNamed, KEY_STYLE, rsStyle);
            action.setStyle(rsStyle);
        }
    }

    protected void resolveGraphicStyleClass(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsGraphicStyleClass = msg(keyPrefix, normalizeNamed, KEY_GRAPHIC_STYLE_CLASS, EMPTY_STRING);
        if (!isBlank(rsGraphicStyleClass)) {
            trace(keyPrefix + normalizeNamed, KEY_GRAPHIC_STYLE_CLASS, rsGraphicStyleClass);
            action.setGraphicStyleClass(rsGraphicStyleClass);
        }
    }

    protected void resolveGraphicStyle(@Nonnull JavaFXBasiliskControllerAction action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix) {
        String rsGraphicStyle = msg(keyPrefix, normalizeNamed, KEY_GRAPHIC_STYLE, EMPTY_STRING);
        if (!isBlank(rsGraphicStyle)) {
            trace(keyPrefix + normalizeNamed, KEY_GRAPHIC_STYLE, rsGraphicStyle);
            action.setGraphicStyle(rsGraphicStyle);
        }
    }

    protected void trace(@Nonnull String actionKey, @Nonnull String key, @Nonnull String value) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(actionKey + DOT + key + EQUALS + value);
        }
    }
}
