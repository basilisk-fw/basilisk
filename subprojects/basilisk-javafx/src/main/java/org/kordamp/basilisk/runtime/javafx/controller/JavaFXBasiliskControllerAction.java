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

import basilisk.core.artifact.BasiliskController;
import basilisk.core.controller.ActionManager;
import basilisk.core.threading.UIThreadManager;
import basilisk.javafx.support.JavaFXAction;
import com.googlecode.openbeans.PropertyEditor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.kordamp.basilisk.runtime.core.controller.AbstractAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static basilisk.core.editors.PropertyEditorResolver.findEditor;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class JavaFXBasiliskControllerAction extends AbstractAction {
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_ICON = "icon";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_GRAPHIC = "graphic";
    public static final String KEY_SELECTED = "selected";
    public static final String KEY_VISIBLE = "visible";
    public static final String KEY_ACCELERATOR = "accelerator";
    public static final String KEY_STYLECLASS = "styleClass";
    private final JavaFXAction toolkitAction;

    private StringProperty description;
    private StringProperty icon;
    private StringProperty image;
    private ObjectProperty<Node> graphic;
    private StringProperty accelerator;
    private StringProperty styleClass;
    private BooleanProperty selected;
    private BooleanProperty visible;

    public JavaFXBasiliskControllerAction(final @Nonnull UIThreadManager uiThreadManager, @Nonnull final ActionManager actionManager, @Nonnull final BasiliskController controller, @Nonnull final String actionName) {
        super(actionManager, controller, actionName);
        requireNonNull(uiThreadManager, "Argument 'uiThreadManager' must not be null");

        toolkitAction = createAction(actionManager, controller, actionName);
        toolkitAction.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                actionManager.invokeAction(controller, actionName, actionEvent);
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
        enabledProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> v, Boolean o, final Boolean n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setEnabled(n);
                    }
                });
            }
        });
        descriptionProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, final String n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setDescription(n);
                    }
                });
            }
        });
        iconProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, final String n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setIcon(n);
                    }
                });
            }
        });
        imageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, final String n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setImage(JavaFXBasiliskControllerAction.this.convertImage(n));
                    }
                });
            }
        });
        graphicProperty().addListener(new ChangeListener<Node>() {
            @Override
            public void changed(ObservableValue<? extends Node> v, Node o, final Node n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setGraphic(n);
                    }
                });
            }
        });
        acceleratorProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, final String n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setAccelerator(n);
                    }
                });
            }
        });
        styleClassProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> v, String o, final String n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setStyleClass(n);
                    }
                });
            }
        });
        selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> v, Boolean o, final Boolean n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setSelected(n);
                    }
                });
            }
        });
        visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> v, Boolean o, final Boolean n) {
                uiThreadManager.runInsideUIAsync(new Runnable() {
                    @Override
                    public void run() {
                        toolkitAction.setVisible(n);
                    }
                });
            }
        });
    }

    protected JavaFXAction createAction(final @Nonnull ActionManager actionManager, final @Nonnull BasiliskController controller, final @Nonnull String actionName) {
        return new JavaFXAction();
    }

    @Nonnull
    public StringProperty descriptionProperty() {
        if (description == null) {
            description = new SimpleStringProperty(this, "description");
        }
        return description;
    }

    @Nonnull
    public StringProperty iconProperty() {
        if (icon == null) {
            icon = new SimpleStringProperty(this, "icon");
        }
        return icon;
    }

    @Nonnull
    public StringProperty imageProperty() {
        if (image == null) {
            image = new SimpleStringProperty(this, "image");
        }
        return image;
    }

    @Nonnull
    public ObjectProperty<Node> graphicProperty() {
        if (graphic == null) {
            graphic = new SimpleObjectProperty<>(this, "graphic");
        }
        return graphic;
    }

    @Nonnull
    public StringProperty acceleratorProperty() {
        if (accelerator == null) {
            accelerator = new SimpleStringProperty(this, "accelerator");
        }
        return accelerator;
    }

    @Nonnull
    public StringProperty styleClassProperty() {
        if (styleClass == null) {
            styleClass = new SimpleStringProperty(this, "styleClass");
        }
        return styleClass;
    }

    @Nonnull
    public BooleanProperty selectedProperty() {
        if (selected == null) {
            selected = new SimpleBooleanProperty(this, "selected");
        }
        return selected;
    }

    @Nonnull
    public BooleanProperty visibleProperty() {
        if (visible == null) {
            visible = new SimpleBooleanProperty(this, "visible", true);
        }
        return visible;
    }

    @Nullable
    public String getDescription() {
        return descriptionProperty().get();
    }

    public void setDescription(@Nullable String description) {
        descriptionProperty().set(description);
    }

    @Nullable
    public String getIcon() {
        return iconProperty().get();
    }

    public void setIcon(@Nullable String icon) {
        iconProperty().set(icon);
    }

    @Nullable
    public String getImage() {
        return imageProperty().get();
    }

    @Nullable
    protected Image convertImage(@Nullable String image) {
        PropertyEditor editor = findEditor(Image.class);
        editor.setValue(image);
        return (Image) editor.getValue();
    }

    public void setImage(@Nullable String image) {
        imageProperty().set(image);
    }

    @Nullable
    public Node getGraphic() {
        return graphicProperty().get();
    }

    public void setGraphic(@Nullable Node graphic) {
        graphicProperty().set(graphic);
    }

    @Nullable
    public String getAccelerator() {
        return acceleratorProperty().get();
    }

    public void setAccelerator(@Nullable String accelerator) {
        acceleratorProperty().set(accelerator);
    }

    @Nullable
    public String getStyleClass() {
        return styleClassProperty().get();
    }

    public void setStyleClass(@Nullable String styleClass) {
        styleClassProperty().set(styleClass);
    }

    public boolean isSelected() {
        return selectedProperty().get();
    }

    public void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    public boolean isVisible() {
        return visibleProperty().get();
    }

    public void setVisible(boolean visible) {
        visibleProperty().set(visible);
    }

    @Nonnull
    public Object getToolkitAction() {
        return toolkitAction;
    }

    protected void doExecute(Object... args) {
        ActionEvent event = null;
        if (args != null && args.length == 1 && args[0] instanceof ActionEvent) {
            event = (ActionEvent) args[0];
        }
        toolkitAction.onActionProperty().get().handle(event);
    }

    @Override
    protected void doInitialize() {
        toolkitAction.setName(getName());
        toolkitAction.setDescription(getDescription());
        toolkitAction.setEnabled(isEnabled());
        toolkitAction.setSelected(isSelected());
        toolkitAction.setVisible(isVisible());
        String accelerator = getAccelerator();
        if (!isBlank(accelerator)) toolkitAction.setAccelerator(accelerator);
        if (!isBlank(getStyleClass())) toolkitAction.setStyleClass(getStyleClass());
        String icon = getIcon();
        if (!isBlank(icon)) toolkitAction.setIcon(icon);
        if (null != getImage()) toolkitAction.setImage(convertImage(getImage()));
        if (null != getGraphic()) toolkitAction.setGraphic(getGraphic());
    }
}
