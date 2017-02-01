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
package basilisk.javafx.support;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Predicate;

import static basilisk.javafx.support.JavaFXUtils.configureControl;
import static basilisk.javafx.support.JavaFXUtils.findElement;
import static basilisk.javafx.support.JavaFXUtils.findElements;
import static basilisk.javafx.support.JavaFXUtils.getBasiliskActionId;

/**
 * Strategy for matching controller actions to widgets defined in FXML.
 *
 * @author Andres Almiray
 * @since 1.0.0
 */
public interface ActionMatcher {
    /**
     * Matches a widget whose action id has been defined by using the default convention.
     * Either the action id was defined using {@code JavaFXUtils.basiliskActionId} or it
     * has "ActionTarget" as a suffix. Examples:
     * <p>
     * <pre>
     * <Button JavaFXUtils.basiliskActionId="copy"/>
     * <Button fx:id="copyActionTarget"/>
     * </pre>
     */
    ActionMatcher DEFAULT = new ActionMatcher() {
        @Override
        public void match(@Nonnull Object node, @Nonnull final String actionName, @Nonnull JavaFXAction action) {
            Collection<Object> controls = findElements(node, new Predicate<Object>() {
                @Override
                public boolean test(Object arg) {
                    if (arg instanceof Node) {
                        return actionName.equals(getBasiliskActionId((Node) arg));
                    } else if (arg instanceof MenuItem) {
                        return actionName.equals(getBasiliskActionId((MenuItem) arg));
                    }
                    return false;
                }
            });

            for (Object control : controls) {
                configureControl(control, action);
            }

            Object control = findElement(node, actionName + "ActionTarget");
            if (control != null && !controls.contains(control)) {
                configureControl(control, action);
            }
        }
    };

    void match(@Nonnull Object node, @Nonnull String actionName, @Nonnull JavaFXAction action);
}
