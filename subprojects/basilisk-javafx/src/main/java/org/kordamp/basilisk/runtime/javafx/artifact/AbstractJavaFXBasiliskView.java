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
package org.kordamp.basilisk.runtime.javafx.artifact;

import basilisk.core.artifact.BasiliskClass;
import basilisk.core.artifact.BasiliskController;
import basilisk.core.controller.Action;
import basilisk.exceptions.BasiliskException;
import basilisk.javafx.support.JavaFXAction;
import basilisk.javafx.support.JavaFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static basilisk.util.ConfigUtils.stripFilenameExtension;

/**
 * JavaFX-friendly implementation of the BasiliskView interface.
 *
 * @author Andres Almiray
 */
public abstract class AbstractJavaFXBasiliskView extends AbstractBasiliskView {
    private static final String FXML_SUFFIX = ".fxml";

    @Nullable
    protected Node loadFromFXML() {
        return loadFromFXML(resolveBasename());
    }

    @Nullable
    protected Node loadFromFXML(@Nonnull String baseName) {
        requireNonBlank(baseName, "Argument 'baseName' must not be blank");
        if (baseName.endsWith(FXML_SUFFIX)) {
            baseName = stripFilenameExtension(baseName);
        }
        baseName = baseName.replace('.', '/');
        String viewName = baseName + FXML_SUFFIX;
        String styleName = baseName + ".css";

        URL viewResource = getResourceAsURL(viewName);
        if (viewResource == null) {
            return null;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(viewResource);
        fxmlLoader.setResources(getApplication().getMessageSource().asResourceBundle());
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory(getApplication().getApplicationClassLoader().get()));
        fxmlLoader.setClassLoader(getApplication().getApplicationClassLoader().get());
        fxmlLoader.setControllerFactory(klass -> getMvcGroup().getView());

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new BasiliskException(e);
        }

        Parent node = fxmlLoader.getRoot();

        URL cssResource = getResourceAsURL(styleName);
        if (cssResource != null) {
            String uriToCss = cssResource.toExternalForm();
            node.getStylesheets().add(uriToCss);
        }

        return node;
    }

    @Nonnull
    protected String resolveBasename() {
        BasiliskClass basiliskClass = getBasiliskClass();
        String packageName = basiliskClass.getPackageName();
        String baseName = basiliskClass.getLogicalPropertyName();
        if (!isBlank(packageName)) {
            baseName = packageName + "." + baseName;
        }
        return baseName;
    }

    protected void connectActions(@Nonnull Object node, @Nonnull BasiliskController controller) {
        JavaFXUtils.connectActions(node, controller);
    }

    @Nullable
    protected JavaFXAction toolkitActionFor(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        Action action = actionFor(controller, actionName);
        return action != null ? (JavaFXAction) action.getToolkitAction() : null;
    }
}