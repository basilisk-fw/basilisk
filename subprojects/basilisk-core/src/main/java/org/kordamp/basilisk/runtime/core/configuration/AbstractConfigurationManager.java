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
package org.kordamp.basilisk.runtime.core.configuration;

import basilisk.core.ApplicationEvent;
import basilisk.core.BasiliskApplication;
import basilisk.core.RunnableWithArgs;
import basilisk.core.configuration.Configuration;
import basilisk.core.configuration.ConfigurationManager;
import basilisk.core.configuration.Configured;
import basilisk.core.editors.ExtendedPropertyEditor;
import basilisk.core.editors.PropertyEditorResolver;
import basilisk.exceptions.BasiliskException;
import basilisk.util.BasiliskClassUtils;
import com.googlecode.openbeans.PropertyDescriptor;
import com.googlecode.openbeans.PropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static basilisk.core.editors.PropertyEditorResolver.findEditor;
import static basilisk.util.BasiliskNameUtils.isNotBlank;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public abstract class AbstractConfigurationManager implements ConfigurationManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurationManager.class);

    private static final String ERROR_INSTANCE_NULL = "Argument 'instance' must not be null";
    private static final String ERROR_TYPE_NULL = "Argument 'type' must not be null";
    private static final String ERROR_VALUE_NULL = "Argument 'value' must not be null";

    @Inject
    protected BasiliskApplication application;

    @PostConstruct
    private void initialize() {
        this.application = requireNonNull(application, "Argument 'application' cannot ne null");

        application.getEventRouter().addEventListener(ApplicationEvent.NEW_INSTANCE.getName(), new RunnableWithArgs() {
            @Override
            public void run(@Nullable Object... args) {
                Object instance = args[1];
                injectConfiguration(instance);
            }
        });
    }

    @Override
    public void injectConfiguration(@Nonnull Object instance) {
        requireNonNull(instance, ERROR_INSTANCE_NULL);

        Map<String, ConfigurationDescriptor> descriptors = new LinkedHashMap<>();
        Class<?> klass = instance.getClass();
        do {
            harvestDescriptors(instance.getClass(), klass, instance, descriptors);
            klass = klass.getSuperclass();
        } while (null != klass);

        doConfigurationInjection(instance, descriptors);
    }

    protected void harvestDescriptors(@Nonnull Class<?> instanceClass, @Nonnull Class<?> currentClass, @Nonnull Object instance, @Nonnull Map<String, ConfigurationDescriptor> descriptors) {
        PropertyDescriptor[] propertyDescriptors = BasiliskClassUtils.getPropertyDescriptors(currentClass);
        for (PropertyDescriptor pd : propertyDescriptors) {
            Method writeMethod = pd.getWriteMethod();
            if (null == writeMethod) { continue; }
            if (isStatic(writeMethod.getModifiers())) {
                continue;
            }

            Configured annotation = writeMethod.getAnnotation(Configured.class);
            if (null == annotation) { continue; }

            String propertyName = pd.getName();
            String configuration = annotation.configuration().trim();
            String key = annotation.value();
            String defaultValue = annotation.defaultValue();
            defaultValue = Configured.NO_VALUE.equals(defaultValue) ? null : defaultValue;
            String format = annotation.format();
            Class<? extends PropertyEditor> editor = annotation.editor();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Property " + propertyName +
                    " of instance " + instance +
                    " [configuration='" + configuration +
                    "', key='" + key +
                    "', defaultValue='" + defaultValue +
                    "', format='" + format +
                    "'] is marked for configuration injection.");
            }
            descriptors.put(propertyName, new MethodConfigurationDescriptor(writeMethod, configuration, key, defaultValue, format, editor));
        }

        for (Field field : currentClass.getDeclaredFields()) {
            if (field.isSynthetic() || isStatic(field.getModifiers()) || descriptors.containsKey(field.getName())) {
                continue;
            }
            final Configured annotation = field.getAnnotation(Configured.class);
            if (null == annotation) { continue; }

            Class<?> resolvedClass = field.getDeclaringClass();
            String fqFieldName = resolvedClass.getName().replace('$', '.') + "." + field.getName();
            String configuration = annotation.configuration().trim();
            String key = annotation.value();
            String defaultValue = annotation.defaultValue();
            defaultValue = Configured.NO_VALUE.equals(defaultValue) ? null : defaultValue;
            String format = annotation.format();
            Class<? extends PropertyEditor> editor = annotation.editor();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Field " + fqFieldName +
                    " of instance " + instance +
                    " [configuration='" + configuration +
                    "', key='" + key +
                    "', defaultValue='" + defaultValue +
                    "', format='" + format +
                    "'] is marked for configuration injection.");
            }

            descriptors.put(field.getName(), new FieldConfigurationDescriptor(field, configuration, key, defaultValue, format, editor));
        }
    }

    protected void doConfigurationInjection(@Nonnull Object instance, @Nonnull Map<String, ConfigurationDescriptor> descriptors) {
        for (ConfigurationDescriptor descriptor : descriptors.values()) {
            Object value = resolveConfiguration(descriptor.getConfiguration(), descriptor.getKey(), descriptor.getDefaultValue());

            if (value != null) {
                InjectionPoint injectionPoint = descriptor.asInjectionPoint();
                if (!isNoopPropertyEditor(descriptor.getEditor()) || !injectionPoint.getType().isAssignableFrom(value.getClass())) {
                    value = convertValue(injectionPoint.getType(), value, descriptor.getFormat(), descriptor.getEditor());
                }
                injectionPoint.setValue(instance, value);
            }
        }
    }

    @Nonnull
    protected Object resolveConfiguration(@Nonnull String name, @Nonnull String key, @Nonnull String defaultValue) {
        Configuration configuration = getConfiguration();
        if (isNotBlank(name)) {
            configuration = getConfiguration(name);
        }

        if (configuration.containsKey(key)) {
            return configuration.get(key);
        } else {
            return defaultValue;
        }
    }

    @Nonnull
    protected Object convertValue(@Nonnull Class<?> type, @Nonnull Object value, @Nullable String format, @Nonnull Class<? extends PropertyEditor> editor) {
        requireNonNull(type, ERROR_TYPE_NULL);
        requireNonNull(value, ERROR_VALUE_NULL);

        PropertyEditor propertyEditor = resolvePropertyEditor(type, format, editor);
        if (isNoopPropertyEditor(propertyEditor.getClass())) { return value; }
        if (value instanceof CharSequence) {
            propertyEditor.setAsText(String.valueOf(value));
        } else {
            propertyEditor.setValue(value);
        }
        return propertyEditor.getValue();
    }

    @Nonnull
    protected PropertyEditor resolvePropertyEditor(@Nonnull Class<?> type, @Nullable String format, @Nonnull Class<? extends PropertyEditor> editor) {
        requireNonNull(type, ERROR_TYPE_NULL);

        PropertyEditor propertyEditor = null;
        if (isNoopPropertyEditor(editor)) {
            propertyEditor = findEditor(type);
        } else {
            try {
                propertyEditor = editor.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new BasiliskException("Could not instantiate editor with " + editor, e);
            }
        }

        if (propertyEditor instanceof ExtendedPropertyEditor) {
            ((ExtendedPropertyEditor) propertyEditor).setFormat(format);
        }
        return propertyEditor;
    }

    protected boolean isNoopPropertyEditor(@Nonnull Class<? extends PropertyEditor> editor) {
        return PropertyEditorResolver.NoopPropertyEditor.class.isAssignableFrom(editor);
    }
}
