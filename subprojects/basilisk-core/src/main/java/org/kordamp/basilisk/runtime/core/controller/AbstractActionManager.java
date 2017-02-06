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
package org.kordamp.basilisk.runtime.core.controller;

import basilisk.core.BasiliskApplication;
import basilisk.core.Configuration;
import basilisk.core.Context;
import basilisk.core.artifact.BasiliskController;
import basilisk.core.artifact.BasiliskControllerClass;
import basilisk.core.controller.AbortActionExecution;
import basilisk.core.controller.Action;
import basilisk.core.controller.ActionExecutionStatus;
import basilisk.core.controller.ActionHandler;
import basilisk.core.controller.ActionInterceptor;
import basilisk.core.controller.ActionManager;
import basilisk.core.i18n.MessageSource;
import basilisk.core.i18n.NoSuchMessageException;
import basilisk.core.mvc.MVCGroup;
import basilisk.core.threading.UIThreadManager;
import basilisk.exceptions.BasiliskException;
import basilisk.exceptions.InstanceMethodInvocationException;
import basilisk.inject.Contextual;
import basilisk.transform.Threading;
import basilisk.util.AnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static basilisk.core.BasiliskExceptionHandler.sanitize;
import static basilisk.util.BasiliskClassUtils.EMPTY_ARGS;
import static basilisk.util.BasiliskClassUtils.invokeExactInstanceMethod;
import static basilisk.util.BasiliskClassUtils.invokeInstanceMethod;
import static basilisk.util.BasiliskNameUtils.capitalize;
import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static basilisk.util.BasiliskNameUtils.uncapitalize;
import static basilisk.util.CollectionUtils.reverse;
import static basilisk.util.TypeUtils.castToBoolean;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractActionManager implements ActionManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractActionManager.class);

    private static final String KEY_THREADING = "controller.threading";
    private static final String KEY_THREADING_DEFAULT = "controller.threading.default";
    private static final String KEY_DISABLE_THREADING_INJECTION = "basilisk.disable.threading.injection";
    private static final String ERROR_CONTROLLER_NULL = "Argument 'controller' must not be null";
    private static final String ERROR_ACTION_NAME_BLANK = "Argument 'actionName' must not be blank";
    private static final String ERROR_ACTION_HANDLER_NULL = "Argument 'actionHandler' must not be null";
    private static final String ERROR_ACTION_NULL = "Argument 'action' must not be null";
    private final ActionCache actionCache = new ActionCache();
    private final Map<String, Threading.Policy> threadingPolicies = new ConcurrentHashMap<>();
    private final List<ActionHandler> handlers = new CopyOnWriteArrayList<>();

    private final BasiliskApplication application;

    @Inject
    public AbstractActionManager(@Nonnull BasiliskApplication application) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
    }

    @Nullable
    private static Method findActionAsMethod(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        for (Method method : controller.getTypeClass().getMethods()) {
            if (actionName.equals(method.getName()) &&
                isPublic(method.getModifiers()) &&
                !isStatic(method.getModifiers()) &&
                method.getReturnType() == Void.TYPE) {
                return method;
            }
        }
        return null;
    }

    @Nonnull
    protected Configuration getConfiguration() {
        return application.getConfiguration();
    }

    @Nonnull
    protected MessageSource getMessageSource() {
        return application.getMessageSource();
    }

    @Nonnull
    protected UIThreadManager getUiThreadManager() {
        return application.getUIThreadManager();
    }

    @Nonnull
    protected Map<String, Threading.Policy> getThreadingPolicies() {
        return threadingPolicies;
    }

    @Nonnull
    public Map<String, Action> actionsFor(@Nonnull BasiliskController controller) {
        requireNonNull(controller, ERROR_CONTROLLER_NULL);
        Map<String, ActionWrapper> actions = actionCache.get(controller);
        if (actions.isEmpty()) {
            LOG.trace("No actions defined for controller {}", controller);
        }
        return Collections.<String, Action>unmodifiableMap(actions);
    }

    @Nullable
    public Action actionFor(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        requireNonNull(controller, ERROR_CONTROLLER_NULL);
        requireNonBlank(actionName, ERROR_ACTION_NAME_BLANK);
        return actionCache.get(controller).get(normalizeName(actionName));
    }

    public void createActions(@Nonnull BasiliskController controller) {
        BasiliskControllerClass basiliskClass = (BasiliskControllerClass) controller.getBasiliskClass();
        for (String actionName : basiliskClass.getActionNames()) {
            Method method = findActionAsMethod(controller, actionName);
            if (method == null) {
                throw new BasiliskException(controller.getTypeClass().getCanonicalName() + " does not define an action named " + actionName);
            }

            ActionWrapper action = wrapAction(createAndConfigureAction(controller, actionName), method);

            final String qualifiedActionName = action.getFullyQualifiedName();
            for (ActionHandler handler : handlers) {
                LOG.debug("Configuring action {} with {}", qualifiedActionName, handler);
                handler.configure(action, method);
            }

            Map<String, ActionWrapper> actions = actionCache.get(controller);
            if (actions.isEmpty()) {
                actions = new TreeMap<>();
                actionCache.set(controller, actions);
            }
            String actionKey = normalizeName(actionName);
            LOG.trace("Action for {} stored as {}", qualifiedActionName, actionKey);
            actions.put(actionKey, action);
        }
    }

    @Nonnull
    private ActionWrapper wrapAction(@Nonnull Action action, @Nonnull Method method) {
        return new ActionWrapper(action, method);
    }

    @Override
    public void updateActions() {
        for (Action action : actionCache.allActions()) {
            updateAction(action);
        }
    }

    @Override
    public void updateActions(@Nonnull BasiliskController controller) {
        for (Action action : actionsFor(controller).values()) {
            updateAction(action);
        }
    }

    @Override
    public void updateAction(@Nonnull Action action) {
        requireNonNull(action, ERROR_ACTION_NULL);

        final String qualifiedActionName = action.getFullyQualifiedName();
        for (ActionHandler handler : handlers) {
            LOG.trace("Calling {}.update() on {}", handler, qualifiedActionName);
            handler.update(action);
        }
    }

    @Override
    public void updateAction(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        requireNonNull(controller, ERROR_CONTROLLER_NULL);
        requireNonBlank(actionName, ERROR_ACTION_NAME_BLANK);
        updateAction(actionFor(controller, actionName));
    }

    @Override
    public void invokeAction(@Nonnull final Action action, @Nonnull final Object... args) {
        requireNonNull(action, ERROR_ACTION_NULL);
        final BasiliskController controller = action.getController();
        final String actionName = action.getActionName();
        Runnable runnable = new Runnable() {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            public void run() {
                Object[] updatedArgs = args;
                List<ActionHandler> copy = new ArrayList<>(handlers);
                List<ActionHandler> invokedHandlers = new ArrayList<>();

                final String qualifiedActionName = action.getFullyQualifiedName();
                ActionExecutionStatus status = ActionExecutionStatus.OK;

                try {
                    LOG.trace("Resolving contextual arguments for " + qualifiedActionName);
                    updatedArgs = injectFromContext(action, updatedArgs);
                } catch (IllegalStateException ise) {
                    LOG.debug("Execution of " + qualifiedActionName + " was aborted", ise);
                    throw ise;
                }

                if (LOG.isDebugEnabled()) {
                    int size = copy.size();
                    LOG.debug("Executing " + size + " handler" + (size != 1 ? "s" : "") + " for " + qualifiedActionName);
                }

                for (ActionHandler handler : copy) {
                    invokedHandlers.add(handler);
                    try {
                        LOG.trace("Calling {}.before() on {}", handler, qualifiedActionName);
                        updatedArgs = handler.before(action, updatedArgs);
                    } catch (AbortActionExecution aae) {
                        status = ActionExecutionStatus.ABORTED;
                        LOG.debug("Execution of {} was aborted by {}", qualifiedActionName, handler);
                        break;
                    }
                }

                LOG.trace("Status before execution of {} is {}", qualifiedActionName, status);
                RuntimeException exception = null;
                boolean exceptionWasHandled = false;
                if (status == ActionExecutionStatus.OK) {
                    try {
                        doInvokeAction(controller, actionName, updatedArgs);
                    } catch (RuntimeException e) {
                        status = ActionExecutionStatus.EXCEPTION;
                        exception = (RuntimeException) sanitize(e);
                        LOG.warn("An exception occurred when executing {}", qualifiedActionName, exception);
                    }
                    LOG.trace("Status after execution of {} is {}", qualifiedActionName, status);

                    if (exception != null) {
                        for (ActionHandler handler : reverse(invokedHandlers)) {
                            LOG.trace("Calling {}.exception() on {}", handler, qualifiedActionName);
                            exceptionWasHandled = handler.exception(exception, action, updatedArgs);
                        }
                    }
                }

                for (ActionHandler handler : reverse(invokedHandlers)) {
                    LOG.trace("Calling {}.after() on {}", handler, qualifiedActionName);
                    handler.after(status, action, updatedArgs);
                }

                if (exception != null && !exceptionWasHandled) {
                    // throw it again
                    throw exception;
                }
            }
        };
        invokeAction(controller, actionName, runnable);
    }

    @Nonnull
    private Object[] injectFromContext(@Nonnull Action action, @Nonnull Object[] args) {
        ActionWrapper wrappedAction = null;
        if (action instanceof ActionWrapper) {
            wrappedAction = (ActionWrapper) action;
        } else {
            wrappedAction = wrapAction(action, findActionAsMethod(action.getController(), action.getActionName()));
        }

        MVCGroup group = action.getController().getMvcGroup();
        if (group == null) {
            // This case only occurs during testing, when an artifact is
            // instantiated without a group
            return args;
        }

        Context context = group.getContext();
        if (wrappedAction.hasContextualArgs) {
            Object[] newArgs = new Object[wrappedAction.argumentsInfo.size()];
            for (int i = 0; i < newArgs.length; i++) {
                ArgInfo argInfo = wrappedAction.argumentsInfo.get(i);
                newArgs[i] = argInfo.contextual ? context.get(argInfo.name) : args[i];
                if (argInfo.contextual && newArgs[i] != null) { context.put(argInfo.name, newArgs[i]); }
                if (argInfo.contextual && !argInfo.nullable && newArgs[i] == null) {
                    throw new IllegalStateException("Could not find an instance of type " +
                        argInfo.type.getName() + " under key '" + argInfo.name +
                        "' in the context of MVCGroup[" + group.getMvcType() + ":" + group.getMvcId() +
                        "] to be injected as argument " + i +
                        " at " + action.getFullyQualifiedName() + "(). Argument does not accept null values.");
                }
            }
            return newArgs;
        }

        return args;
    }

    public void invokeAction(@Nonnull final BasiliskController controller, @Nonnull final String actionName, @Nonnull final Object... args) {
        requireNonNull(controller, ERROR_CONTROLLER_NULL);
        requireNonBlank(actionName, ERROR_ACTION_NAME_BLANK);
        invokeAction(actionFor(controller, actionName), args);
    }

    protected void doInvokeAction(@Nonnull BasiliskController controller, @Nonnull String actionName, @Nonnull Object[] updatedArgs) {
        try {
            invokeInstanceMethod(controller, actionName, updatedArgs);
        } catch (InstanceMethodInvocationException imie) {
            if (imie.getCause() instanceof NoSuchMethodException) {
                // try again but this time remove the 1st arg if it's
                // descendant of java.util.EventObject
                if (updatedArgs.length == 1 && updatedArgs[0] != null && EventObject.class.isAssignableFrom(updatedArgs[0].getClass())) {
                    invokeExactInstanceMethod(controller, actionName, EMPTY_ARGS);
                } else {
                    throw imie;
                }
            } else {
                throw imie;
            }
        }
    }

    private void invokeAction(@Nonnull BasiliskController controller, @Nonnull String actionName, @Nonnull Runnable runnable) {
        String fullQualifiedActionName = controller.getTypeClass().getName() + "." + actionName;
        Threading.Policy policy = threadingPolicies.get(fullQualifiedActionName);
        if (policy == null) {
            if (isThreadingDisabled(fullQualifiedActionName)) {
                policy = Threading.Policy.SKIP;
            } else {
                policy = resolveThreadingPolicy(controller, actionName);
            }
            threadingPolicies.put(fullQualifiedActionName, policy);
        }

        LOG.debug("Executing {} with policy {}", fullQualifiedActionName, policy);

        switch (policy) {
            case OUTSIDE_UITHREAD:
                getUiThreadManager().runOutsideUI(runnable);
                break;
            case INSIDE_UITHREAD_SYNC:
                getUiThreadManager().runInsideUISync(runnable);
                break;
            case INSIDE_UITHREAD_ASYNC:
                getUiThreadManager().runInsideUIAsync(runnable);
                break;
            case SKIP:
            default:
                runnable.run();
        }
    }

    @Nonnull
    private Threading.Policy resolveThreadingPolicy(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        Method method = findActionAsMethod(controller, actionName);
        if (method != null) {
            Threading annotation = method.getAnnotation(Threading.class);
            return annotation == null ? resolveThreadingPolicy(controller) : annotation.value();
        }

        return Threading.Policy.OUTSIDE_UITHREAD;
    }

    @Nonnull
    private Threading.Policy resolveThreadingPolicy(@Nonnull BasiliskController controller) {
        Threading annotation = AnnotationUtils.findAnnotation(controller.getTypeClass(), Threading.class);
        return annotation == null ? resolveThreadingPolicy() : annotation.value();
    }

    @Nonnull
    private Threading.Policy resolveThreadingPolicy() {
        Object value = getConfiguration().get(KEY_THREADING_DEFAULT);
        if (value == null) {
            return Threading.Policy.OUTSIDE_UITHREAD;
        }

        if (value instanceof Threading.Policy) {
            return (Threading.Policy) value;
        }

        String policy = String.valueOf(value).toLowerCase();
        switch (policy) {
            case "sync":
            case "inside sync":
            case "inside uithread sync":
            case "inside_uithread_sync":
                return Threading.Policy.INSIDE_UITHREAD_SYNC;
            case "async":
            case "inside async":
            case "inside uithread async":
            case "inside_uithread_async":
                return Threading.Policy.INSIDE_UITHREAD_ASYNC;
            case "outside":
            case "outside uithread":
            case "outside_uithread":
                return Threading.Policy.OUTSIDE_UITHREAD;
            case "skip":
                return Threading.Policy.SKIP;
            default:
                throw new IllegalArgumentException("Value '" + policy + "' cannot be translated into " + Threading.Policy.class.getName());
        }
    }

    private boolean isThreadingDisabled(@Nonnull String actionName) {
        if (getConfiguration().getAsBoolean(KEY_DISABLE_THREADING_INJECTION, false)) {
            return true;
        }

        Map<String, Object> settings = getConfiguration().asFlatMap();

        String keyName = KEY_THREADING + "." + actionName;
        while (!KEY_THREADING.equals(keyName)) {
            Object value = settings.get(keyName);
            keyName = keyName.substring(0, keyName.lastIndexOf("."));
            if (value != null && !castToBoolean(value)) { return true; }
        }

        return false;
    }

    public void addActionHandler(@Nonnull ActionHandler actionHandler) {
        requireNonNull(actionHandler, ERROR_ACTION_HANDLER_NULL);
        if (handlers.contains(actionHandler)) {
            return;
        }
        handlers.add(actionHandler);
    }

    public void addActionInterceptor(@Nonnull ActionInterceptor actionInterceptor) {
        throw new UnsupportedOperationException(ActionInterceptor.class.getName() + " has been deprecated and is no longer supported");
    }

    @Nonnull
    protected Action createAndConfigureAction(@Nonnull BasiliskController controller, @Nonnull String actionName) {
        requireNonNull(controller, ERROR_CONTROLLER_NULL);
        requireNonBlank(actionName, ERROR_ACTION_NAME_BLANK);
        Action action = createControllerAction(controller, actionName);

        String normalizeNamed = capitalize(normalizeName(actionName));
        String keyPrefix = controller.getTypeClass().getName() + ".action.";

        doConfigureAction(action, controller, normalizeNamed, keyPrefix);

        action.initialize();

        return action;
    }

    protected abstract void doConfigureAction(@Nonnull Action action, @Nonnull BasiliskController controller, @Nonnull String normalizeNamed, @Nonnull String keyPrefix);

    @Nonnull
    protected abstract Action createControllerAction(@Nonnull BasiliskController controller, @Nonnull String actionName);

    @Nonnull
    public String normalizeName(@Nonnull String actionName) {
        requireNonBlank(actionName, ERROR_ACTION_NAME_BLANK);
        if (actionName.endsWith(ACTION)) {
            actionName = actionName.substring(0, actionName.length() - ACTION.length());
        }
        return uncapitalize(actionName);
    }

    @Nullable
    protected String msg(@Nonnull String key, @Nonnull String actionName, @Nonnull String subkey, @Nullable String defaultValue) {
        try {
            return getMessageSource().getMessage(key + actionName + "." + subkey, application.getLocale());
        } catch (NoSuchMessageException nsme) {
            return getMessageSource().getMessage("application.action." + actionName + "." + subkey, application.getLocale(), defaultValue);
        }
    }

    private static class ActionWrapper extends ActionDecorator {
        private final List<ArgInfo> argumentsInfo = new ArrayList<>();
        private boolean hasContextualArgs;

        public ActionWrapper(@Nonnull Action delegate, @Nonnull Method method) {
            super(delegate);

            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            hasContextualArgs = method.getAnnotation(Contextual.class) != null;
            for (int i = 0; i < parameterTypes.length; i++) {
                ArgInfo argInfo = new ArgInfo();
                argInfo.type = parameterTypes[i];
                argInfo.name = argInfo.type.getCanonicalName();

                Annotation[] annotations = parameterAnnotations[i];
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (Contextual.class.isAssignableFrom(annotation.annotationType())) {
                            hasContextualArgs = true;
                            argInfo.contextual = true;
                        }
                        if (Nonnull.class.isAssignableFrom(annotation.annotationType())) {
                            argInfo.nullable = false;
                        }
                        if (Named.class.isAssignableFrom(annotation.annotationType())) {
                            Named named = (Named) annotation;
                            if (!isBlank(named.value())) {
                                argInfo.name = named.value();
                            }
                        }
                    }
                }
                argumentsInfo.add(argInfo);
            }
        }
    }

    private static class ArgInfo {
        private Class<?> type;
        private String name;
        private boolean nullable = true;
        private boolean contextual = false;
    }

    private static class ActionCache {
        private final Map<WeakReference<BasiliskController>, Map<String, ActionWrapper>> cache = new ConcurrentHashMap<>();

        @Nonnull
        public Map<String, ActionWrapper> get(@Nonnull BasiliskController controller) {
            synchronized (cache) {
                for (Map.Entry<WeakReference<BasiliskController>, Map<String, ActionWrapper>> entry : cache.entrySet()) {
                    BasiliskController test = entry.getKey().get();
                    if (test == controller) {
                        return entry.getValue();
                    }
                }
            }
            return Collections.emptyMap();
        }

        public void set(@Nonnull BasiliskController controller, @Nonnull Map<String, ActionWrapper> actions) {
            WeakReference<BasiliskController> existingController = null;
            synchronized (cache) {
                for (WeakReference<BasiliskController> key : cache.keySet()) {
                    if (key.get() == controller) {
                        existingController = key;
                        break;
                    }
                }
            }

            if (null != existingController) {
                cache.remove(existingController);
            }

            cache.put(new WeakReference<>(controller), actions);
        }

        public Collection<Action> allActions() {
            // create a copy to avoid CME
            List<Action> actions = new ArrayList<>();

            synchronized (cache) {
                for (Map<String, ActionWrapper> map : cache.values()) {
                    actions.addAll(map.values());
                }
            }

            return actions;
        }
    }
}
