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
package org.kordamp.basilisk.runtime.core.mvc;

import basilisk.core.ApplicationClassLoader;
import basilisk.core.ApplicationEvent;
import basilisk.core.BasiliskApplication;
import basilisk.core.artifact.ArtifactManager;
import basilisk.core.artifact.BasiliskArtifact;
import basilisk.core.artifact.BasiliskClass;
import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskMvcArtifact;
import basilisk.core.artifact.BasiliskView;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.mvc.MVCGroupConfiguration;
import basilisk.exceptions.BasiliskException;
import basilisk.exceptions.BasiliskViewInitializationException;
import basilisk.exceptions.FieldException;
import basilisk.exceptions.MVCGroupInstantiationException;
import basilisk.exceptions.NewInstanceException;
import basilisk.inject.Contextual;
import basilisk.inject.MVCMember;
import basilisk.util.CollectionUtils;
import basilisk.util.Instantiator;
import com.googlecode.openbeans.PropertyDescriptor;
import org.kordamp.basilisk.runtime.core.injection.InjectionUnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static basilisk.core.BasiliskExceptionHandler.sanitize;
import static basilisk.util.AnnotationUtils.annotationsOfMethodParameter;
import static basilisk.util.AnnotationUtils.findAnnotation;
import static basilisk.util.AnnotationUtils.namesFor;
import static basilisk.util.BasiliskClassUtils.getAllDeclaredFields;
import static basilisk.util.BasiliskClassUtils.getPropertyDescriptors;
import static basilisk.util.BasiliskClassUtils.setFieldValue;
import static basilisk.util.BasiliskClassUtils.setPropertiesOrFieldsNoException;
import static basilisk.util.BasiliskClassUtils.setPropertyOrFieldValueNoException;
import static basilisk.util.BasiliskNameUtils.capitalize;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.ConfigUtils.getConfigValueAsBoolean;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@code MVCGroupManager} interface.
 *
 * @author Andres Almiray
 */
public class DefaultMVCGroupManager extends AbstractMVCGroupManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMVCGroupManager.class);
    private static final String CONFIG_KEY_COMPONENT = "component";
    private static final String CONFIG_KEY_EVENTS_LIFECYCLE = "events.lifecycle";
    private static final String CONFIG_KEY_EVENTS_LISTENER = "events.listener";
    private static final String KEY_PARENT_GROUP = "parentGroup";

    protected final ApplicationClassLoader applicationClassLoader;
    protected final Instantiator instantiator;

    @Inject
    public DefaultMVCGroupManager(@Nonnull BasiliskApplication application, @Nonnull ApplicationClassLoader applicationClassLoader, @Nonnull Instantiator instantiator) {
        super(application);
        this.applicationClassLoader = requireNonNull(applicationClassLoader, "Argument 'applicationClassLoader' must not be null");
        this.instantiator = requireNonNull(instantiator, "Argument 'instantiator' must not be null");
    }

    protected void doInitialize(@Nonnull Map<String, MVCGroupConfiguration> configurations) {
        requireNonNull(configurations, "Argument 'configurations' must not be null");
        for (MVCGroupConfiguration configuration : configurations.values()) {
            addConfiguration(configuration);
        }
    }

    @Nonnull
    protected MVCGroup createMVCGroup(@Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId, @Nonnull Map<String, Object> args) {
        requireNonNull(configuration, ERROR_CONFIGURATION_NULL);
        requireNonNull(args, ERROR_ARGS_NULL);

        mvcId = resolveMvcId(configuration, mvcId);
        checkIdIsUnique(mvcId, configuration);

        LOG.debug("Building MVC group '{}' with name '{}'", configuration.getMvcType(), mvcId);
        Map<String, Object> argsCopy = copyAndConfigureArguments(args, configuration, mvcId);

        // figure out what the classes are
        Map<String, ClassHolder> classMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> memberEntry : configuration.getMembers().entrySet()) {
            String memberType = memberEntry.getKey();
            String memberClassName = memberEntry.getValue();
            selectClassesPerMember(memberType, memberClassName, classMap);
        }

        Map<String, Object> instances = new LinkedHashMap<>();
        List<Object> injectedInstances = new ArrayList<>();

        try {
            InjectionUnitOfWork.start();
        } catch (IllegalStateException ise) {
            throw new MVCGroupInstantiationException("Can not instantiate MVC group '" + configuration.getMvcType() + "' with id '" + mvcId + "'", configuration.getMvcType(), mvcId, ise);
        }

        try {
            instances.putAll(instantiateMembers(classMap, argsCopy));
        } finally {
            try {
                injectedInstances.addAll(InjectionUnitOfWork.finish());
            } catch (IllegalStateException ise) {
                throw new MVCGroupInstantiationException("Can not instantiate MVC group '" + configuration.getMvcType() + "' with id '" + mvcId + "'", configuration.getMvcType(), mvcId, ise);
            }
        }

        MVCGroup group = newMVCGroup(configuration, mvcId, instances, (MVCGroup) args.get(KEY_PARENT_GROUP));
        adjustMvcArguments(group, argsCopy);

        boolean fireEvents = isConfigFlagEnabled(configuration, CONFIG_KEY_EVENTS_LIFECYCLE);
        if (fireEvents) {
            getApplication().getEventRouter().publishEvent(ApplicationEvent.INITIALIZE_MVC_GROUP.getName(), asList(configuration, group));
        }

        // special case -- controllers are added as application listeners
        if (isConfigFlagEnabled(group.getConfiguration(), CONFIG_KEY_EVENTS_LISTENER)) {
            BasiliskController controller = group.getController();
            if (controller != null) {
                getApplication().getEventRouter().addEventListener(controller);
            }
        }

        // mutually set each other to the available fields and inject args
        fillReferencedProperties(group, argsCopy);

        doAddGroup(group);

        initializeMembers(group, argsCopy);
        if (group instanceof AbstractMVCGroup) {
            ((AbstractMVCGroup) group).getInjectedInstances().addAll(injectedInstances);
        }

        if (fireEvents) {
            getApplication().getEventRouter().publishEvent(ApplicationEvent.CREATE_MVC_GROUP.getName(), asList(group));
        }

        return group;
    }

    protected void adjustMvcArguments(@Nonnull MVCGroup group, @Nonnull Map<String, Object> args) {
        // must set it again because mvcId might have been initialized internally
        args.put("mvcId", group.getMvcId());
        args.put("mvcGroup", group);
        args.put("application", getApplication());
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    protected String resolveMvcId(@Nonnull MVCGroupConfiguration configuration, @Nullable String mvcId) {
        boolean component = getConfigValueAsBoolean(configuration.getConfig(), CONFIG_KEY_COMPONENT, false);

        if (isBlank(mvcId)) {
            if (component) {
                mvcId = configuration.getMvcType() + "-" + System.nanoTime();
            } else {
                mvcId = configuration.getMvcType();
            }
        }
        return mvcId;
    }

    @SuppressWarnings("unchecked")
    protected void selectClassesPerMember(@Nonnull String memberType, @Nonnull String memberClassName, @Nonnull Map<String, ClassHolder> classMap) {
        BasiliskClass basiliskClass = getApplication().getArtifactManager().findBasiliskClass(memberClassName);
        ClassHolder classHolder = new ClassHolder();
        if (basiliskClass != null) {
            classHolder.artifactClass = (Class<? extends BasiliskArtifact>) basiliskClass.getClazz();
        } else {
            classHolder.regularClass = loadClass(memberClassName);
        }
        classMap.put(memberType, classHolder);
    }

    @Nonnull
    protected Map<String, Object> copyAndConfigureArguments(@Nonnull Map<String, Object> args, @Nonnull MVCGroupConfiguration configuration, @Nonnull String mvcId) {
        Map<String, Object> argsCopy = CollectionUtils.<String, Object>map()
            .e("application", getApplication())
            .e("mvcType", configuration.getMvcType())
            .e("mvcId", mvcId)
            .e("configuration", configuration);

        if (args.containsKey(KEY_PARENT_GROUP)) {
            if (args.get(KEY_PARENT_GROUP) instanceof MVCGroup) {
                MVCGroup parentGroup = (MVCGroup) args.get(KEY_PARENT_GROUP);
                for (Map.Entry<String, Object> e : parentGroup.getMembers().entrySet()) {
                    args.put("parent" + capitalize(e.getKey()), e.getValue());
                }
            }
        }

        argsCopy.putAll(args);
        return argsCopy;
    }

    protected void checkIdIsUnique(@Nonnull String mvcId, @Nonnull MVCGroupConfiguration configuration) {
        if (findGroup(mvcId) != null) {
            String action = getApplication().getConfiguration().getAsString("basilisk.mvcid.collision", "exception");
            if ("warning".equalsIgnoreCase(action)) {
                LOG.warn("A previous instance of MVC group '{}' with id '{}' exists. Destroying the old instance first.", configuration.getMvcType(), mvcId);
                destroyMVCGroup(mvcId);
            } else {
                throw new MVCGroupInstantiationException("Can not instantiate MVC group '" + configuration.getMvcType() + "' with id '" + mvcId + "' because a previous instance with that name exists and was not disposed off properly.", configuration.getMvcType(), mvcId);
            }
        }
    }

    @Nonnull
    protected Map<String, Object> instantiateMembers(@Nonnull Map<String, ClassHolder> classMap, @Nonnull Map<String, Object> args) {
        // instantiate the parts
        Map<String, Object> instanceMap = new LinkedHashMap<>();
        for (Map.Entry<String, ClassHolder> classEntry : classMap.entrySet()) {
            String memberType = classEntry.getKey();
            if (args.containsKey(memberType)) {
                // use provided value, even if null
                instanceMap.put(memberType, args.get(memberType));
            } else {
                // otherwise create a new value
                ClassHolder classHolder = classEntry.getValue();
                if (classHolder.artifactClass != null) {
                    Class<? extends BasiliskArtifact> memberClass = classHolder.artifactClass;
                    ArtifactManager artifactManager = getApplication().getArtifactManager();
                    BasiliskClass basiliskClass = artifactManager.findBasiliskClass(memberClass);
                    BasiliskArtifact instance = artifactManager.newInstance(basiliskClass);
                    instanceMap.put(memberType, instance);
                    args.put(memberType, instance);
                } else {
                    Class<?> memberClass = classHolder.regularClass;
                    try {
                        Object instance = instantiator.instantiate(memberClass);
                        instanceMap.put(memberType, instance);
                        args.put(memberType, instance);
                    } catch (RuntimeException e) {
                        LOG.error("Can't create member {} with {}", memberType, memberClass);
                        throw new NewInstanceException(memberClass, e);
                    }
                }
            }
        }
        return instanceMap;
    }

    protected void initializeMembers(@Nonnull MVCGroup group, @Nonnull Map<String, Object> args) {
        LOG.debug("Initializing each MVC member of group '{}'", group.getMvcId());
        for (Map.Entry<String, Object> memberEntry : group.getMembers().entrySet()) {
            String memberType = memberEntry.getKey();
            Object member = memberEntry.getValue();
            if (member instanceof BasiliskArtifact) {
                initializeArtifactMember(group, memberType, (BasiliskArtifact) member, args);
            } else {
                initializeNonArtifactMember(group, memberType, member, args);
            }
        }
    }

    protected void initializeArtifactMember(@Nonnull final MVCGroup group, @Nonnull String type, @Nonnull final BasiliskArtifact member, @Nonnull final Map<String, Object> args) {
        if (member instanceof BasiliskView) {
            getApplication().getUIThreadManager().runInsideUISync(new Runnable() {
                @Override
                public void run() {
                    try {
                        BasiliskView view = (BasiliskView) member;
                        view.initUI();
                    } catch (RuntimeException e) {
                        throw (RuntimeException) sanitize(new BasiliskViewInitializationException(group.getMvcType(), group.getMvcId(), member.getClass().getName(), e));
                    }
                    ((BasiliskMvcArtifact) member).mvcGroupInit(args);
                }
            });
        } else if (member instanceof BasiliskMvcArtifact) {
            ((BasiliskMvcArtifact) member).mvcGroupInit(args);
        }
    }

    protected void initializeNonArtifactMember(@Nonnull MVCGroup group, @Nonnull String type, @Nonnull Object member, @Nonnull Map<String, Object> args) {
        // empty
    }

    protected abstract static class InjectionPoint {
        protected final String name;
        protected final boolean nullable;
        protected final Type type;

        protected InjectionPoint(String name, boolean nullable, Type type) {
            this.name = name;
            this.nullable = nullable;
            this.type = type;
        }

        protected enum Type {
            MEMBER,
            CONTEXTUAL,
            OTHER
        }

        protected abstract void apply(@Nonnull MVCGroup group, @Nonnull String memberType, @Nonnull Object instance, @Nonnull Map<String, Object> args);
    }

    protected static class FieldInjectionPoint extends InjectionPoint {
        protected final Field field;

        protected FieldInjectionPoint(String name, boolean nullable, Type type, Field field) {
            super(name, nullable, type);
            this.field = field;
        }

        @Override
        protected void apply(@Nonnull MVCGroup group, @Nonnull String memberType, @Nonnull Object instance, @Nonnull Map<String, Object> args) {
            String[] keys = namesFor(field);
            Object argValue = args.get(name);

            if (type == Type.CONTEXTUAL) {
                for (String key : keys) {
                    if (group.getContext().containsKey(key)) {
                        argValue = group.getContext().get(key);
                        break;
                    }
                }
            }

            if (argValue == null) {
                if (!nullable) {
                    if (type == Type.CONTEXTUAL) {
                        throw new IllegalStateException("Could not find an instance of type " +
                            field.getType().getName() + " under keys '" + Arrays.toString(keys) +
                            "' in the context of MVCGroup[" + group.getMvcType() + ":" + group.getMvcId() +
                            "] to be injected on field '" + field.getName() +
                            "' in " + type + " (" + resolveMemberClass(instance).getName() + "). Field does not accept null values.");
                    } else if (type == Type.MEMBER) {
                        throw new IllegalStateException("Could not inject argument on field '"
                            + name + "' in " + memberType + " (" + resolveMemberClass(instance).getName() +
                            "). Field does not accept null values.");
                    }
                }
                return;
            }

            try {
                setFieldValue(instance, name, argValue);
                if (type == Type.OTHER) {
                    LOG.warn("Field '" + name + "' in " + memberType + " (" + resolveMemberClass(instance).getName() +
                        ") must be annotated with @" + MVCMember.class.getName() + ".");
                }
            } catch (FieldException e) {
                throw new MVCGroupInstantiationException(group.getMvcType(), group.getMvcId(), e);
            }
        }
    }

    protected static class MethodInjectionPoint extends InjectionPoint {
        protected final Method method;

        protected MethodInjectionPoint(String name, boolean nullable, Type type, Method method) {
            super(name, nullable, type);
            this.method = method;
        }

        @Override
        protected void apply(@Nonnull MVCGroup group, @Nonnull String memberType, @Nonnull Object instance, @Nonnull Map<String, Object> args) {
            if (type == Type.CONTEXTUAL) {
                String[] keys = namesFor(method);
                Object argValue = args.get(name);

                for (String key : keys) {
                    if (group.getContext().containsKey(key)) {
                        argValue = group.getContext().get(key);
                        break;
                    }
                }

                if (argValue == null && !nullable) {
                    throw new IllegalStateException("Could not find an instance of type " +
                        method.getParameterTypes()[0].getName() + " under keys '" + Arrays.toString(keys) +
                        "' in the context of MVCGroup[" + group.getMvcType() + ":" + group.getMvcId() +
                        "] to be injected on property '" + name +
                        "' in " + type + " (" + resolveMemberClass(instance).getName() + "). Property does not accept null values.");
                }

                try {
                    method.invoke(instance, argValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new MVCGroupInstantiationException(group.getMvcType(), group.getMvcId(), e);
                }
            } else {
                Object argValue = args.get(name);
                if (argValue == null) {
                    if (!nullable) {
                        if (type == Type.MEMBER) {
                            throw new IllegalStateException("Could not inject argument on property '" +
                                name + "' in " + memberType + " (" + resolveMemberClass(instance).getName() +
                                "). Property does not accept null values.");
                        }
                    }
                    return;
                }

                try {
                    method.invoke(instance, argValue);
                    if (type == Type.OTHER) {
                        LOG.warn("Property '" + name + "' in " + memberType + " (" + resolveMemberClass(instance).getName() +
                            ") must be annotated with @" + MVCMember.class.getName() + ".");
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new MVCGroupInstantiationException(group.getMvcType(), group.getMvcId(), e);
                }
            }
        }
    }

    protected void fillReferencedProperties(@Nonnull MVCGroup group, @Nonnull Map<String, Object> args) {
        for (Map.Entry<String, Object> memberEntry : group.getMembers().entrySet()) {
            String memberType = memberEntry.getKey();
            Object member = memberEntry.getValue();

            Map<String, Object> argsCopy = new LinkedHashMap<>(args);

            Map<String, Field> fields = new LinkedHashMap<>();
            for (Field field : getAllDeclaredFields(resolveMemberClass(member))) {
                fields.put(field.getName(), field);
            }
            Map<String, InjectionPoint> injectionPoints = new LinkedHashMap<>();
            for (PropertyDescriptor descriptor : getPropertyDescriptors(resolveMemberClass(member))) {
                Method method = descriptor.getWriteMethod();
                if (method == null || isInjectable(method)) { continue; }
                boolean nullable = method.getAnnotation(Nonnull.class) == null && findAnnotation(annotationsOfMethodParameter(method, 0), Nonnull.class) == null;
                InjectionPoint.Type type = resolveType(method);
                Field field = fields.get(descriptor.getName());
                if (field != null && type == InjectionPoint.Type.OTHER) {
                    type = resolveType(field);
                    nullable = field.getAnnotation(Nonnull.class) == null;
                }
                injectionPoints.put(descriptor.getName(), new MethodInjectionPoint(descriptor.getName(), nullable, type, method));
            }

            for (Field field : getAllDeclaredFields(resolveMemberClass(member))) {
                if (Modifier.isStatic(field.getModifiers()) || isInjectable(field)) { continue; }
                if (!injectionPoints.containsKey(field.getName())) {
                    boolean nullable = field.getAnnotation(Nonnull.class) == null;
                    InjectionPoint.Type type = resolveType(field);
                    injectionPoints.put(field.getName(), new FieldInjectionPoint(field.getName(), nullable, type, field));
                }
            }

            for (InjectionPoint ip : injectionPoints.values()) {
                ip.apply(group, memberType, member, args);
                argsCopy.remove(ip.name);
            }

            /*
            for (Map.Entry<String, Object> e : argsCopy.entrySet()) {
                try {
                    setPropertyOrFieldValue(member, e.getKey(), e.getValue());
                    LOG.warn("Property '" + e.getKey() + "' in " + memberType + " (" + resolveMemberClass(member).getName() +
                        ") must be annotated with @" + MVCMember.class.getName() + ".");
                } catch (PropertyException ignored) {
                    // OK
                }
            }
            */
            setPropertiesOrFieldsNoException(member, argsCopy);
        }
    }

    @Nonnull
    protected InjectionPoint.Type resolveType(@Nonnull AnnotatedElement element) {
        if (isContextual(element)) {
            return InjectionPoint.Type.CONTEXTUAL;
        } else if (isMvcMember(element)) {
            return InjectionPoint.Type.MEMBER;
        }
        return InjectionPoint.Type.OTHER;
    }

    protected boolean isContextual(AnnotatedElement element) {
        return element != null && element.getAnnotation(Contextual.class) != null;
    }

    protected boolean isInjectable(AnnotatedElement element) {
        return element != null && element.getAnnotation(Inject.class) != null;
    }

    protected boolean isMvcMember(AnnotatedElement element) {
        return element != null && element.getAnnotation(MVCMember.class) != null;
    }

    protected void doAddGroup(@Nonnull MVCGroup group) {
        addGroup(group);
    }

    public void destroyMVCGroup(@Nonnull String mvcId) {
        MVCGroup group = findGroup(mvcId);
        LOG.debug("Group '{}' points to {}", mvcId, group);

        if (group == null) { return; }

        LOG.debug("Destroying MVC group identified by '{}'", mvcId);

        if (isConfigFlagEnabled(group.getConfiguration(), CONFIG_KEY_EVENTS_LISTENER)) {
            BasiliskController controller = group.getController();
            if (controller != null) {
                getApplication().getEventRouter().removeEventListener(controller);
            }
        }

        destroyMembers(group);

        doRemoveGroup(group);
        group.destroy();

        if (isConfigFlagEnabled(group.getConfiguration(), CONFIG_KEY_EVENTS_LIFECYCLE)) {
            getApplication().getEventRouter().publishEvent(ApplicationEvent.DESTROY_MVC_GROUP.getName(), asList(group));
        }
    }

    protected void destroyMembers(@Nonnull MVCGroup group) {
        for (Map.Entry<String, Object> memberEntry : group.getMembers().entrySet()) {
            Object member = memberEntry.getValue();
            if (member instanceof BasiliskArtifact) {
                destroyArtifactMember(memberEntry.getKey(), (BasiliskArtifact) member);
            } else {
                destroyNonArtifactMember(memberEntry.getKey(), member);
            }

        }

        if (group instanceof AbstractMVCGroup) {
            List<Object> injectedInstances = ((AbstractMVCGroup) group).getInjectedInstances();
            for (Object instance : injectedInstances) {
                getApplication().getInjector().release(instance);
            }
            injectedInstances.clear();
        }
    }

    protected void destroyArtifactMember(@Nonnull String type, @Nonnull BasiliskArtifact member) {
        if (member instanceof BasiliskMvcArtifact) {
            final BasiliskMvcArtifact artifact = (BasiliskMvcArtifact) member;

            if (artifact instanceof BasiliskView) {
                getApplication().getUIThreadManager().runInsideUISync(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            artifact.mvcGroupDestroy();
                        } catch (RuntimeException e) {
                            throw (RuntimeException) sanitize(e);
                        }
                    }
                });
            } else {
                artifact.mvcGroupDestroy();
            }

            // clear all parent* references
            for (String parentMemberName : new String[]{"parentModel", "parentView", "parentController", "parentGroup"}) {
                setPropertyOrFieldValueNoException(member, parentMemberName, null);
            }
        }

        destroyContextualMemberProperties(type, member);
    }

    protected void destroyContextualMemberProperties(@Nonnull String type, @Nonnull BasiliskArtifact member) {
        for (Field field : getAllDeclaredFields(member.getTypeClass())) {
            if (isContextual(field) && !field.getType().isPrimitive()) {
                try {
                    setFieldValue(member, field.getName(), null);
                } catch (FieldException e) {
                    throw new IllegalStateException("Could not nullify field '" +
                        field.getName() + "' in " + type + " (" + member.getTypeClass().getName() + ")", e);
                }
            }
        }
    }

    protected void destroyNonArtifactMember(@Nonnull String type, @Nonnull Object member) {
        // empty
    }

    protected void doRemoveGroup(@Nonnull MVCGroup group) {
        removeGroup(group);
    }

    protected boolean isConfigFlagEnabled(@Nonnull MVCGroupConfiguration configuration, @Nonnull String key) {
        return getConfigValueAsBoolean(configuration.getConfig(), key, true);
    }

    @Nonnull
    private static Class<?> resolveMemberClass(@Nonnull Object member) {
        if (member instanceof BasiliskArtifact) {
            return ((BasiliskArtifact) member).getTypeClass();
        }
        return member.getClass();
    }

    @Nullable
    protected Class<?> loadClass(@Nonnull String className) {
        try {
            return applicationClassLoader.get().loadClass(className);
        } catch (ClassNotFoundException e) {
            // #39 do not ignore this CNFE
            throw new BasiliskException(e.toString(), e);
        }
    }

    protected static final class ClassHolder {
        protected Class<?> regularClass;
        protected Class<? extends BasiliskArtifact> artifactClass;
    }
}
