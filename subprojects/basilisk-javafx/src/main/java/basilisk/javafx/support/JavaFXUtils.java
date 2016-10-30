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
package basilisk.javafx.support;

import basilisk.core.artifact.BasiliskController;
import basilisk.core.controller.Action;
import basilisk.core.controller.ActionManager;
import basilisk.core.editors.ValueConversionException;
import basilisk.exceptions.InstanceMethodInvocationException;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.stage.Window;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static basilisk.util.BasiliskClassUtils.getGetterName;
import static basilisk.util.BasiliskClassUtils.getPropertyValue;
import static basilisk.util.BasiliskClassUtils.invokeExactInstanceMethod;
import static basilisk.util.BasiliskClassUtils.invokeInstanceMethod;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public final class JavaFXUtils {
    private static final String ERROR_NODE_NULL = "Argument 'node' must not be null";
    private static final String ERROR_CONTROL_NULL = "Argument 'control' must not be null";
    private static final String ERROR_ACTION_NULL = "Argument 'action' must not be null";
    private static final String ERROR_ICON_BLANK = "Argument 'iconUrl' must not be blank";
    private static final String ERROR_ID_BLANK = "Argument 'id' must not be blank";
    private static final String ERROR_URL_BLANK = "Argument 'url' must not be blank";
    private static final String ERROR_PREDICATE_NULL = "Argument 'predicate' must not be null";
    private static final String ERROR_ROOT_NULL = "Argument 'root' must not be null";
    private static final String ERROR_CONTROLLER_NULL = "Argument 'controller' must not be null";
    private static final String ACTION_TARGET_SUFFIX = "ActionTarget";
    private static final String PROPERTY_SUFFIX = "Property";

    private JavaFXUtils() {

    }

    /**
     * Associates a {@code Action} with a target {@code Node}.
     *
     * @param node     the target node on which the action will be registered.
     * @param actionId the id of the action to be registered.
     *
     * @since 0.2.0
     */
    public static void setGriffonActionId(@Nonnull Node node, @Nonnull String actionId) {
        requireNonNull(node, ERROR_NODE_NULL);
        requireNonBlank(actionId, ERROR_ID_BLANK);
        node.getProperties().put(Action.class.getName(), actionId);
    }

    /**
     * Finds out if an {@code Action} has been registered with the target {@code Node}, returning the aciton id if found.
     *
     * @param node the target node on which the action may have been registered.
     *
     * @return the name of the action registered with the target {@code Node} or {@code null} if not found.
     *
     * @since 0.2.0
     */
    @Nullable
    public static String getGriffonActionId(@Nonnull Node node) {
        requireNonNull(node, ERROR_NODE_NULL);
        return (String) node.getProperties().get(Action.class.getName());
    }

    /**
     * Associates a {@code Action} with a target {@code MenuItem}.
     *
     * @param menuItem the target menuItem on which the action will be registered.
     * @param actionId the id of the action to be registered.
     *
     * @since 0.2.0
     */
    public static void setGriffonActionId(@Nonnull MenuItem menuItem, @Nonnull String actionId) {
        requireNonNull(menuItem, ERROR_NODE_NULL);
        requireNonBlank(actionId, ERROR_ID_BLANK);
        menuItem.getProperties().put(Action.class.getName(), actionId);
    }

    /**
     * Finds out if an {@code Action} has been registered with the target {@code MenuItem}, returning the aciton id if found.
     *
     * @param menuItem the target menuItem on which the action may have been registered.
     *
     * @return the name of the action registered with the target {@code MenuItem} or {@code null} if not found.
     *
     * @since 0.2.0
     */
    @Nullable
    public static String getGriffonActionId(@Nonnull MenuItem menuItem) {
        requireNonNull(menuItem, ERROR_NODE_NULL);
        return (String) menuItem.getProperties().get(Action.class.getName());
    }

    /**
     * Wraps an <tt>ObservableList</tt>, publishing updates inside the UI thread.
     *
     * @param source the <tt>ObservableList</tt> to be wrapped
     * @param <E>    the list's paramter type.
     *
     * @return a new  <tt>ObservableList</tt>
     *
     * @since 0.1.0
     */
    @Nonnull
    public static <E> ObservableList<E> createJavaFXThreadProxyList(@Nonnull ObservableList<E> source) {
        requireNonNull(source, "Argument 'source' must not be null");
        return new JavaFXThreadProxyObservableList<>(source);
    }

    private static class JavaFXThreadProxyObservableList<E> extends DelegatingObservableList<E> {
        protected JavaFXThreadProxyObservableList(ObservableList<E> delegate) {
            super(delegate);
        }

        @Override
        protected void sourceChanged(@Nonnull final ListChangeListener.Change<? extends E> c) {
            if (Platform.isFxApplicationThread()) {
                fireChange(c);
            } else {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        fireChange(c);
                    }
                });
            }
        }
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <B> Property<?> extractProperty(@Nonnull B bean, @Nonnull String propertyName) {
        requireNonNull(bean, "Argument 'bean' must not be null");
        requireNonBlank(propertyName, "Argument 'propertyName' must not be null");

        if (!propertyName.endsWith(PROPERTY_SUFFIX)) {
            propertyName += PROPERTY_SUFFIX;
        }

        InstanceMethodInvocationException imie;
        try {
            // 1. try <columnName>Property() first
            return (Property<?>) invokeExactInstanceMethod(bean, propertyName);
        } catch (InstanceMethodInvocationException e) {
            imie = e;
        }

        // 2. fallback to get<columnName>Property()
        try {
            return (Property<?>) invokeExactInstanceMethod(bean, getGetterName(propertyName));
        } catch (InstanceMethodInvocationException e) {
            throw imie;
        }
    }

    public static void connectActions(@Nonnull Object node, @Nonnull BasiliskController controller) {
        requireNonNull(node, ERROR_NODE_NULL);
        requireNonNull(controller, ERROR_CONTROLLER_NULL);
        ActionManager actionManager = controller.getApplication().getActionManager();
        for (Map.Entry<String, Action> e : actionManager.actionsFor(controller).entrySet()) {
            final String actionName = actionManager.normalizeName(e.getKey());
            final String actionTargetName = actionName + ACTION_TARGET_SUFFIX;
            JavaFXAction action = (JavaFXAction) e.getValue().getToolkitAction();

            Collection<Object> controls = findElements(node, new Predicate<Object>() {
                @Override
                public boolean test(@Nonnull Object arg) {
                    if (arg instanceof Node) {
                        return actionName.equals(getGriffonActionId((Node) arg));
                    } else if (arg instanceof MenuItem) {
                        return actionName.equals(getGriffonActionId((MenuItem) arg));
                    }
                    return false;
                }
            });

            for (Object control : controls) {
                configureControl(control, action);
            }

            Object control = findElement(node, actionTargetName);
            if (control == null || controls.contains(control)) { continue; }
            configureControl(control, action);
        }
    }

    private static void configureControl(@Nonnull Object control, @Nonnull JavaFXAction action) {
        if (control instanceof ButtonBase) {
            configure(((ButtonBase) control), action);
        } else if (control instanceof MenuItem) {
            JavaFXUtils.configure(((MenuItem) control), action);
        } else if (control instanceof Node) {
            ((Node) control).addEventHandler(ActionEvent.ACTION, action.getOnAction());
        } else {
            // does it support the onAction property?
            try {
                invokeInstanceMethod(control, "setOnAction", action.getOnAction());
            } catch (InstanceMethodInvocationException imie) {
                // ignore
            }
        }
    }

    private static void runInsideUIThread(@Nonnull Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static String normalizeStyle(@Nonnull String style, @Nonnull String key, @Nonnull String value) {
        requireNonBlank(style, "Argument 'style' must not be blank");
        requireNonBlank(key, "Argument 'key' must not be blank");
        requireNonBlank(value, "Argument 'value' must not be blank");

        int start = style.indexOf(key);
        if (start != -1) {
            int end = style.indexOf(";", start);
            end = end >= start ? end : style.length() - 1;
            style = style.substring(0, start) + style.substring(end + 1);
        }
        return style + key + ": " + value + ";";
    }

    public static void configure(final @Nonnull ToggleButton control, final @Nonnull JavaFXAction action) {
        configure((ButtonBase) control, action);

        action.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setSelected(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setSelected(action.isSelected());
            }
        });
    }

    public static void configure(final @Nonnull CheckBox control, final @Nonnull JavaFXAction action) {
        configure((ButtonBase) control, action);

        action.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setSelected(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setSelected(action.isSelected());
            }
        });
    }

    public static void configure(final @Nonnull RadioButton control, final @Nonnull JavaFXAction action) {
        configure((ButtonBase) control, action);

        action.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setSelected(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setSelected(action.isSelected());
            }
        });
    }

    public static void configure(final @Nonnull ButtonBase control, final @Nonnull JavaFXAction action) {
        requireNonNull(control, ERROR_CONTROL_NULL);
        requireNonNull(action, ERROR_ACTION_NULL);

        action.onActionProperty().addListener(new ChangeListener<EventHandler<ActionEvent>>() {
            @Override
            public void changed(ObservableValue<? extends EventHandler<ActionEvent>> observableValue, EventHandler<ActionEvent> oldValue, EventHandler<ActionEvent> newValue) {
                control.setOnAction(newValue);
            }
        });
        control.setOnAction(action.getOnAction());

        action.nameProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, final String newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setText(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setText(action.getName());
            }
        });

        action.descriptionProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, final String newValue) {
                setTooltip(control, newValue);
            }
        });
        setTooltip(control, action.getDescription());

        action.iconProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, final String newValue) {
                setIcon(control, newValue);
            }
        });
        if (!isBlank(action.getIcon())) {
            setIcon(control, action.getIcon());
        }

        action.imageProperty().addListener(new ChangeListener<Image>() {
            @Override
            public void changed(ObservableValue<? extends Image> observableValue, Image oldValue, final Image newValue) {
                setGraphic(control, newValue);
            }
        });
        if (null != action.getImage()) {
            setGraphic(control, action.getImage());
        }

        action.graphicProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> observableValue, Node oldValue, final Node newValue) {
                setGraphic(control, newValue);
            }
        });
        if (null != action.getGraphic()) {
            setGraphic(control, action.getGraphic());
        }

        action.enabledProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setDisable(!newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setDisable(!action.isEnabled());
            }
        });

        action.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setVisible(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setVisible(action.isVisible());
            }
        });

        action.styleClassProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, final String newValue) {
                setStyleClass(control, oldValue, true);
                setStyleClass(control, newValue);
            }
        });
        setStyleClass(control, action.getStyleClass());

        action.styleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, String n) {
                setStyle(control, n);
            }
        });
        setStyle(control, action.getStyle());

        action.graphicStyleClassProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, String n) {
                setGraphicStyleClass(control, o, true);
                setGraphicStyleClass(control, n);
            }
        });
        setGraphicStyleClass(control, action.getGraphicStyleClass());

        action.graphicStyleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, String n) {
                setGraphicStyle(control, n);
            }
        });
        setGraphicStyle(control, action.getGraphicStyle());
    }

    public static void configure(final @Nonnull CheckMenuItem control, final @Nonnull JavaFXAction action) {
        configure((MenuItem) control, action);

        action.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setSelected(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setSelected(action.isSelected());
            }
        });
    }

    public static void configure(final @Nonnull RadioMenuItem control, final @Nonnull JavaFXAction action) {
        configure((MenuItem) control, action);

        action.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setSelected(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setSelected(action.isSelected());
            }
        });
    }

    public static void configure(final @Nonnull MenuItem control, final @Nonnull JavaFXAction action) {
        requireNonNull(control, ERROR_CONTROL_NULL);
        requireNonNull(action, ERROR_ACTION_NULL);

        action.onActionProperty().addListener(new ChangeListener<EventHandler<ActionEvent>>() {
            @Override
            public void changed(ObservableValue<? extends EventHandler<ActionEvent>> observableValue, EventHandler<ActionEvent> oldValue, EventHandler<ActionEvent> newValue) {
                control.setOnAction(newValue);
            }
        });
        control.setOnAction(action.getOnAction());

        action.nameProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, final String newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setText(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setText(action.getName());
            }
        });

        action.iconProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, final String newValue) {
                setIcon(control, newValue);
            }
        });
        if (!isBlank(action.getIcon())) {
            setIcon(control, action.getIcon());
        }

        action.imageProperty().addListener(new ChangeListener<Image>() {
            @Override
            public void changed(ObservableValue<? extends Image> observableValue, Image oldValue, final Image newValue) {
                setGraphic(control, newValue);
            }
        });
        if (null != action.getImage()) {
            setGraphic(control, action.getImage());
        }

        action.graphicProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> observableValue, Node oldValue, final Node newValue) {
                setGraphic(control, newValue);
            }
        });
        if (null != action.getGraphic()) {
            setGraphic(control, action.getGraphic());
        }

        action.enabledProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setDisable(!newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setDisable(!action.getEnabled());
            }
        });

        action.acceleratorProperty().addListener(new ChangeListener<KeyCombination>() {
            @Override
            public void changed(ObservableValue<? extends KeyCombination> observableValue, KeyCombination oldValue, final KeyCombination newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setAccelerator(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setAccelerator(action.getAccelerator());
            }
        });

        action.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, final Boolean newValue) {
                runInsideUIThread(new Runnable() {
                    @Override
                    public void run() {
                        control.setVisible(newValue);
                    }
                });
            }
        });
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                control.setVisible(action.isVisible());
            }
        });

        action.styleClassProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, final String newValue) {
                setStyleClass(control, oldValue, true);
                setStyleClass(control, newValue);
            }
        });
        setStyleClass(control, action.getStyleClass());

        action.styleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, String n) {
                setStyle(control, n);
            }
        });
        setStyle(control, action.getStyle());

        action.graphicStyleClassProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, String n) {
                setGraphicStyleClass(control, o, true);
                setGraphicStyleClass(control, n);
            }
        });
        setGraphicStyleClass(control, action.getGraphicStyleClass());

        action.graphicStyleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, String n) {
                setGraphicStyle(control, n);
            }
        });
        setGraphicStyle(control, action.getGraphicStyle());
    }

    public static void setStyle(@Nonnull Node node, @Nonnull String style) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isNull(style)) { return; }
        if (style.startsWith("&")) {
            // append style
            String nodeStyle = node.getStyle();
            node.setStyle(nodeStyle + (nodeStyle.endsWith(";") ? "" : ";") + style.substring(1));
        } else {
            node.setStyle(style);
        }
    }

    public static void setStyle(@Nonnull MenuItem node, @Nonnull String style) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isNull(style)) { return; }
        if (style.startsWith("&")) {
            // append style
            String nodeStyle = node.getStyle();
            node.setStyle(nodeStyle + (nodeStyle.endsWith(";") ? "" : ";") + style.substring(1));
        } else {
            node.setStyle(style);
        }
    }

    public static void setGraphicStyle(@Nonnull ButtonBase node, @Nonnull String graphicStyle) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isNull(graphicStyle)) { return; }
        if (node.getGraphic() != null) {
            setStyle(node.getGraphic(), graphicStyle);
        }
    }

    public static void setGraphicStyle(@Nonnull MenuItem node, @Nonnull String graphicStyle) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isNull(graphicStyle)) { return; }
        if (node.getGraphic() != null) {
            setStyle(node.getGraphic(), graphicStyle);
        }
    }

    public static void setStyleClass(@Nonnull Node node, @Nonnull String styleClass) {
        setStyleClass(node, styleClass, false);
    }

    public static void setStyleClass(@Nonnull Node node, @Nonnull String styleClass, boolean remove) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isBlank(styleClass)) { return; }

        ObservableList<String> styleClasses = node.getStyleClass();
        applyStyleClass(styleClass, styleClasses, remove);
    }

    public static void setStyleClass(@Nonnull MenuItem node, @Nonnull String styleClass) {
        setStyleClass(node, styleClass, false);
    }

    public static void setStyleClass(@Nonnull MenuItem node, @Nonnull String styleClass, boolean remove) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isBlank(styleClass)) { return; }
        ObservableList<String> styleClasses = node.getStyleClass();
        applyStyleClass(styleClass, styleClasses, remove);
    }

    public static void setGraphicStyleClass(@Nonnull ButtonBase node, @Nonnull String graphicStyleClass) {
        setGraphicStyleClass(node, graphicStyleClass, false);
    }

    public static void setGraphicStyleClass(@Nonnull ButtonBase node, @Nonnull String graphicStyleClass, boolean remove) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isBlank(graphicStyleClass) || node.getGraphic() == null) { return; }

        ObservableList<String> graphicStyleClasses = node.getGraphic().getStyleClass();
        applyStyleClass(graphicStyleClass, graphicStyleClasses, remove);
    }

    public static void setGraphicStyleClass(@Nonnull MenuItem node, @Nonnull String graphicStyleClass) {
        setGraphicStyleClass(node, graphicStyleClass, false);
    }

    public static void setGraphicStyleClass(@Nonnull MenuItem node, @Nonnull String graphicStyleClass, boolean remove) {
        requireNonNull(node, ERROR_CONTROL_NULL);
        if (isBlank(graphicStyleClass) || node.getGraphic() == null) { return; }

        ObservableList<String> graphicStyleClasses = node.getGraphic().getStyleClass();
        applyStyleClass(graphicStyleClass, graphicStyleClasses, remove);
    }

    private static void applyStyleClass(final String styleClass, final ObservableList<String> styleClasses, final boolean remove) {
        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                String[] strings = styleClass.split("[,\\ ]");
                if (remove) {
                    styleClasses.removeAll(strings);
                } else {
                    Set<String> classes = new LinkedHashSet<>(styleClasses);
                    for (String s : strings) {
                        if (isBlank(s)) { continue; }
                        classes.add(s.trim());
                    }
                    styleClasses.setAll(classes);
                }
            }
        });
    }

    public static void setTooltip(final @Nonnull Control control, final @Nullable String text) {
        if (isBlank(text)) {
            return;
        }
        requireNonNull(control, ERROR_CONTROL_NULL);

        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                Tooltip tooltip = control.tooltipProperty().get();
                if (tooltip == null) {
                    tooltip = new Tooltip();
                    control.tooltipProperty().set(tooltip);
                }
                tooltip.setText(text);
            }
        });
    }

    public static void setIcon(final @Nonnull Labeled control, @Nonnull String iconUrl) {
        requireNonNull(control, ERROR_CONTROL_NULL);
        requireNonBlank(iconUrl, ERROR_ICON_BLANK);

        final Node graphicNode = resolveIcon(iconUrl);
        if (graphicNode != null) {
            runInsideUIThread(new Runnable() {
                @Override
                public void run() {
                    control.graphicProperty().set(graphicNode);
                }
            });
        }
    }

    public static void setIcon(final @Nonnull MenuItem control, @Nonnull String iconUrl) {
        requireNonNull(control, ERROR_CONTROL_NULL);
        requireNonBlank(iconUrl, ERROR_ICON_BLANK);

        final Node graphicNode = resolveIcon(iconUrl);
        if (graphicNode != null) {
            runInsideUIThread(new Runnable() {
                @Override
                public void run() {
                    control.graphicProperty().set(graphicNode);
                }
            });
        }
    }

    public static void setGraphic(final @Nonnull Labeled control, final @Nullable Image graphic) {
        requireNonNull(control, ERROR_CONTROL_NULL);

        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                if (graphic != null) {
                    Node graphicNode = new ImageView(graphic);
                    control.graphicProperty().set(graphicNode);
                } else {
                    control.graphicProperty().set(null);
                }
            }
        });
    }

    public static void setGraphic(final @Nonnull MenuItem control, final @Nullable Image graphic) {
        requireNonNull(control, ERROR_CONTROL_NULL);

        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                if (graphic != null) {
                    Node graphicNode = new ImageView(graphic);
                    control.graphicProperty().set(graphicNode);
                } else {
                    control.graphicProperty().set(null);
                }
            }
        });
    }

    public static void setGraphic(final @Nonnull Labeled control, final @Nullable Node graphic) {
        requireNonNull(control, ERROR_CONTROL_NULL);

        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                if (graphic != null) {
                    control.graphicProperty().set(graphic);
                } else {
                    control.graphicProperty().set(null);
                }
            }
        });
    }

    public static void setGraphic(final @Nonnull MenuItem control, final @Nullable Node graphic) {
        requireNonNull(control, ERROR_CONTROL_NULL);

        runInsideUIThread(new Runnable() {
            @Override
            public void run() {
                if (graphic != null) {
                    control.graphicProperty().set(graphic);
                } else {
                    control.graphicProperty().set(null);
                }
            }
        });
    }

    @Nullable
    public static Node resolveIcon(@Nonnull String iconUrl) {
        requireNonBlank(iconUrl, ERROR_URL_BLANK);

        if (iconUrl.contains("|")) {
            // assume classname|arg format
            return handleAsClassWithArg(iconUrl);
        } else {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(iconUrl);
            if (resource != null) {
                return new ImageView(new Image(resource.toString()));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Node handleAsClassWithArg(String str) {
        String[] args = str.split("\\|");
        if (args.length == 2) {
            Class<?> iconClass = null;
            try {
                iconClass = (Class<?>) JavaFXUtils.class.getClassLoader().loadClass(args[0]);
            } catch (ClassNotFoundException e) {
                throw illegalValue(str, Node.class, e);
            }

            Constructor<?> constructor = null;
            try {
                constructor = iconClass.getConstructor(String.class);
            } catch (NoSuchMethodException e) {
                throw illegalValue(str, Node.class, e);
            }

            try {
                Object o = constructor.newInstance(args[1]);
                if (o instanceof Node) {
                    return (Node) o;
                } else if (o instanceof Image) {
                    return new ImageView((Image) o);
                } else {
                    throw illegalValue(str, Node.class);
                }
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw illegalValue(str, Node.class, e);
            }
        } else {
            throw illegalValue(str, Node.class);
        }
    }

    @Nullable
    public static Node findNode(@Nonnull Node root, @Nonnull String id) {
        requireNonNull(root, ERROR_ROOT_NULL);
        requireNonBlank(id, ERROR_ID_BLANK);

        if (id.equals(root.getId())) { return root; }

        if (root instanceof TabPane) {
            TabPane parent = (TabPane) root;
            for (Tab child : parent.getTabs()) {
                if (child.getContent() != null) {
                    Node found = findNode(child.getContent(), id);
                    if (found != null) { return found; }
                }
            }
        } else if (root instanceof TitledPane) {
            TitledPane parent = (TitledPane) root;
            if (parent.getContent() != null) {
                Node found = findNode(parent.getContent(), id);
                if (found != null) { return found; }
            }
        } else if (root instanceof Accordion) {
            Accordion parent = (Accordion) root;
            for (TitledPane child : parent.getPanes()) {
                Node found = findNode(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof SplitPane) {
            SplitPane parent = (SplitPane) root;
            for (Node child : parent.getItems()) {
                Node found = findNode(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) root;
            if (scrollPane.getContent() != null) {
                Node found = findNode(scrollPane.getContent(), id);
                if (found != null) { return found; }
            }
        } else if (root instanceof ToolBar) {
            ToolBar toolBar = (ToolBar) root;
            for (Node child : toolBar.getItems()) {
                Node found = findNode(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof Parent) {
            Parent parent = (Parent) root;
            for (Node child : parent.getChildrenUnmodifiable()) {
                Node found = findNode(child, id);
                if (found != null) { return found; }
            }
        }

        return null;
    }

    @Nullable
    public static Object findElement(@Nonnull Object root, @Nonnull String id) {
        requireNonNull(root, ERROR_ROOT_NULL);
        requireNonBlank(id, ERROR_ID_BLANK);

        if (id.equals(getPropertyValue(root, "id"))) { return root; }

        if (root instanceof ButtonBar) {
            ButtonBar buttonBar = (ButtonBar) root;
            for (Node child : buttonBar.getButtons()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof MenuBar) {
            MenuBar menuBar = (MenuBar) root;
            for (Menu child : menuBar.getMenus()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof ContextMenu) {
            ContextMenu contextMenu = (ContextMenu) root;
            for (MenuItem child : contextMenu.getItems()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof Menu) {
            Menu menu = (Menu) root;
            for (MenuItem child : menu.getItems()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof TabPane) {
            TabPane tabPane = (TabPane) root;
            for (Tab child : tabPane.getTabs()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof Tab) {
            Tab tab = (Tab) root;
            if (tab.getContent() != null) {
                Object found = findElement(tab.getContent(), id);
                if (found != null) { return found; }
            }
        } else if (root instanceof TitledPane) {
            TitledPane parent = (TitledPane) root;
            if (parent.getContent() != null) {
                Object found = findElement(parent.getContent(), id);
                if (found != null) { return found; }
            }
        } else if (root instanceof Accordion) {
            Accordion parent = (Accordion) root;
            for (TitledPane child : parent.getPanes()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof SplitPane) {
            SplitPane parent = (SplitPane) root;
            for (Node child : parent.getItems()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) root;
            if (scrollPane.getContent() != null) {
                Object found = findElement(scrollPane.getContent(), id);
                if (found != null) { return found; }
            }
        } else if (root instanceof ToolBar) {
            ToolBar toolBar = (ToolBar) root;
            for (Node child : toolBar.getItems()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        } else if (root instanceof Parent) {
            Parent parent = (Parent) root;
            for (Node child : parent.getChildrenUnmodifiable()) {
                Object found = findElement(child, id);
                if (found != null) { return found; }
            }
        }

        return null;
    }

    @Nullable
    public static Object findElement(@Nonnull Object root, @Nonnull Predicate<Object> predicate) {
        requireNonNull(root, ERROR_ROOT_NULL);
        requireNonNull(predicate, ERROR_PREDICATE_NULL);

        if (predicate.test(root)) {
            return root;
        }

        if (root instanceof ButtonBar) {
            ButtonBar buttonBar = (ButtonBar) root;
            for (Node child : buttonBar.getButtons()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof MenuBar) {
            MenuBar menuBar = (MenuBar) root;
            for (Menu child : menuBar.getMenus()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof ContextMenu) {
            ContextMenu contextMenu = (ContextMenu) root;
            for (MenuItem child : contextMenu.getItems()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof Menu) {
            Menu menu = (Menu) root;
            for (MenuItem child : menu.getItems()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof TabPane) {
            TabPane tabPane = (TabPane) root;
            for (Tab child : tabPane.getTabs()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof Tab) {
            Tab tab = (Tab) root;
            if (tab.getContent() != null) {
                Object found = findElement(tab.getContent(), predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof TitledPane) {
            TitledPane parent = (TitledPane) root;
            if (parent.getContent() != null) {
                Object found = findElement(parent.getContent(), predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof Accordion) {
            Accordion parent = (Accordion) root;
            for (TitledPane child : parent.getPanes()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof SplitPane) {
            SplitPane parent = (SplitPane) root;
            for (Node child : parent.getItems()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) root;
            if (scrollPane.getContent() != null) {
                Object found = findElement(scrollPane.getContent(), predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof ToolBar) {
            ToolBar toolBar = (ToolBar) root;
            for (Node child : toolBar.getItems()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        } else if (root instanceof Parent) {
            Parent parent = (Parent) root;
            for (Node child : parent.getChildrenUnmodifiable()) {
                Object found = findElement(child, predicate);
                if (found != null) { return found; }
            }
        }

        return null;
    }

    @Nonnull
    public static Collection<Object> findElements(@Nonnull Object root, @Nonnull Predicate<Object> predicate) {
        Set<Object> accumulator = new LinkedHashSet<>();
        findElements(root, predicate, accumulator);
        return accumulator;
    }

    private static void findElements(@Nonnull Object root, @Nonnull Predicate<Object> predicate, @Nonnull Collection<Object> accumulator) {
        requireNonNull(root, ERROR_ROOT_NULL);
        requireNonNull(predicate, ERROR_PREDICATE_NULL);

        if (predicate.test(root)) {
            accumulator.add(root);
        }

        if (root instanceof ButtonBar) {
            ButtonBar buttonBar = (ButtonBar) root;
            for (Node child : buttonBar.getButtons()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof MenuBar) {
            MenuBar menuBar = (MenuBar) root;
            for (Menu child : menuBar.getMenus()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof ContextMenu) {
            ContextMenu contextMenu = (ContextMenu) root;
            for (MenuItem child : contextMenu.getItems()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof Menu) {
            Menu menu = (Menu) root;
            for (MenuItem child : menu.getItems()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof TabPane) {
            TabPane tabPane = (TabPane) root;
            for (Tab child : tabPane.getTabs()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof Tab) {
            Tab tab = (Tab) root;
            if (tab.getContent() != null) {
                findElements(tab.getContent(), predicate, accumulator);
            }
        } else if (root instanceof TitledPane) {
            TitledPane parent = (TitledPane) root;
            if (parent.getContent() != null) {
                findElements(parent.getContent(), predicate, accumulator);
            }
        } else if (root instanceof Accordion) {
            Accordion parent = (Accordion) root;
            for (TitledPane child : parent.getPanes()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof SplitPane) {
            SplitPane parent = (SplitPane) root;
            for (Node child : parent.getItems()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) root;
            if (scrollPane.getContent() != null) {
                findElements(scrollPane.getContent(), predicate, accumulator);
            }
        } else if (root instanceof ToolBar) {
            ToolBar toolBar = (ToolBar) root;
            for (Node child : toolBar.getItems()) {
                findElements(child, predicate, accumulator);
            }
        } else if (root instanceof Parent) {
            Parent parent = (Parent) root;
            for (Node child : parent.getChildrenUnmodifiable()) {
                findElements(child, predicate, accumulator);
            }
        }
    }

    @Nullable
    public static Window getWindowAncestor(@Nonnull Object node) {
        requireNonNull(node, ERROR_NODE_NULL);

        if (node instanceof Window) {
            return (Window) node;
        } else if (node instanceof Scene) {
            return ((Scene) node).getWindow();
        } else if (node instanceof Node) {
            Scene scene = ((Node) node).getScene();
            if (scene != null) {
                return scene.getWindow();
            }
        } else if (node instanceof Tab) {
            TabPane tabPane = ((Tab) node).getTabPane();
            if (tabPane != null) {
                return getWindowAncestor(tabPane);
            }
        }

        return null;
    }

    private static ValueConversionException illegalValue(Object value, Class<?> klass) {
        throw new ValueConversionException(value, klass);
    }

    private static ValueConversionException illegalValue(Object value, Class<?> klass, Exception e) {
        throw new ValueConversionException(value, klass, e);
    }

    /**
     * An unary function that evaluates its input.
     *
     * @param <T> the type of the input to the predicate
     *
     * @author Andres Almiray
     * @since 0.2.0
     */
    public interface Predicate<T> {
        /**
         * Evaluates this predicate on the given argument.
         *
         * @param arg the input argument
         *
         * @return {@code true} if the input argument matches the predicate,
         * {@code false} otherwise.
         */
        boolean test(@Nonnull T arg);
    }
}
