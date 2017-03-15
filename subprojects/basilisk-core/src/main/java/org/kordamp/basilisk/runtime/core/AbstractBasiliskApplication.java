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
package org.kordamp.basilisk.runtime.core;

import basilisk.core.ApplicationBootstrapper;
import basilisk.core.ApplicationClassLoader;
import basilisk.core.ApplicationConfigurer;
import basilisk.core.ApplicationEvent;
import basilisk.core.BasiliskApplication;
import basilisk.core.BasiliskExceptionHandler;
import basilisk.core.Context;
import basilisk.core.ExecutorServiceManager;
import basilisk.core.RunnableWithArgs;
import basilisk.core.ShutdownHandler;
import basilisk.core.addon.AddonManager;
import basilisk.core.addon.BasiliskAddon;
import basilisk.core.artifact.ArtifactManager;
import basilisk.core.configuration.Configuration;
import basilisk.core.configuration.ConfigurationManager;
import basilisk.core.controller.ActionManager;
import basilisk.core.env.ApplicationPhase;
import basilisk.core.env.Lifecycle;
import basilisk.core.event.EventRouter;
import basilisk.core.i18n.MessageSource;
import basilisk.core.injection.Injector;
import basilisk.core.mvc.MVCGroupManager;
import basilisk.core.resources.ResourceHandler;
import basilisk.core.resources.ResourceInjector;
import basilisk.core.resources.ResourceResolver;
import basilisk.core.threading.UIThreadManager;
import basilisk.core.view.WindowManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static basilisk.util.AnnotationUtils.named;
import static basilisk.util.BasiliskApplicationUtils.parseLocale;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Implements the basics for a skeleton BasiliskApplication.<p>
 *
 * @author Danno Ferrin
 * @author Andres Almiray
 */
public abstract class AbstractBasiliskApplication implements BasiliskApplication {
    public static final String[] EMPTY_ARGS = new String[0];
    private static final String ERROR_SHUTDOWN_HANDLER_NULL = "Argument 'shutdownHandler' must not be null";
    private static final Class<?>[] CTOR_ARGS = new Class<?>[]{String[].class};
    protected final Object[] lock = new Object[0];
    private final List<ShutdownHandler> shutdownHandlers = new ArrayList<>();
    private final String[] startupArgs;
    private final Object shutdownLock = new Object();
    private final Logger log;
    private Injector<?> injector;

    private ObjectProperty<Locale> locale;
    private ReadOnlyObjectWrapper<ApplicationPhase> phase;

    public AbstractBasiliskApplication() {
        this(EMPTY_ARGS);
    }

    public AbstractBasiliskApplication(@Nonnull String[] args) {
        requireNonNull(args, "Argument 'args' must not be null");
        startupArgs = Arrays.copyOf(args, args.length);
        log = LoggerFactory.getLogger(getClass());
    }

    @Nonnull
    public static BasiliskApplication run(@Nonnull Class<? extends BasiliskApplication> applicationClass, @Nonnull String[] args) throws Exception {
        BasiliskExceptionHandler.registerExceptionHandler();

        BasiliskApplication application = applicationClass.getDeclaredConstructor(CTOR_ARGS).newInstance(new Object[]{args});
        ApplicationBootstrapper bootstrapper = new DefaultApplicationBootstrapper(application);
        bootstrapper.bootstrap();
        bootstrapper.run();

        return application;
    }

    @Nonnull
    @Override
    public ObjectProperty<Locale> localeProperty() {
        if (locale == null) {
            locale = new SimpleObjectProperty<>(this, PROPERTY_LOCALE, Locale.getDefault());
        }
        return locale;
    }

    @Nonnull
    @Override
    public ReadOnlyObjectProperty<ApplicationPhase> phaseProperty() {
        if (phase == null) {
            phase = new ReadOnlyObjectWrapper<>(this, PROPERTY_PHASE, ApplicationPhase.INITIALIZE);
        }
        return phase.getReadOnlyProperty();
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        return localeProperty().get();
    }

    public void setLocale(@Nonnull Locale locale) {
        requireNonNull(locale, "Argument 'locale' must not be null");
        localeProperty().set(locale);
        Locale.setDefault(locale);
    }

    @Nonnull
    @Override
    public ApplicationPhase getPhase() {
        return phaseProperty().get();
    }

    protected void setPhase(@Nonnull ApplicationPhase phase) {
        requireNonNull(phase, "Argument 'phase' must not be null");
        synchronized (lock) {
            phaseProperty();
            this.phase.set(phase);
        }
    }

    @Nonnull
    public String[] getStartupArgs() {
        return startupArgs;
    }

    @Nonnull
    public Logger getLog() {
        return log;
    }

    public void setLocaleAsString(@Nullable String locale) {
        setLocale(parseLocale(locale));
    }

    public void addShutdownHandler(@Nonnull ShutdownHandler handler) {
        requireNonNull(handler, ERROR_SHUTDOWN_HANDLER_NULL);
        if (!shutdownHandlers.contains(handler)) { shutdownHandlers.add(handler); }
    }

    public void removeShutdownHandler(@Nonnull ShutdownHandler handler) {
        requireNonNull(handler, ERROR_SHUTDOWN_HANDLER_NULL);
        shutdownHandlers.remove(handler);
    }

    @Nonnull
    @Override
    public ApplicationClassLoader getApplicationClassLoader() {
        return injector.getInstance(ApplicationClassLoader.class);
    }

    @Nonnull
    @Override
    public Context getContext() {
        return injector.getInstance(Context.class, named("applicationContext"));
    }

    @Nonnull
    @Override
    public Configuration getConfiguration() {
        return getConfigurationManager().getConfiguration();
    }

    @Nonnull
    @Override
    public ConfigurationManager getConfigurationManager() {
        return injector.getInstance(ConfigurationManager.class);
    }

    @Nonnull
    @Override
    public UIThreadManager getUIThreadManager() {
        return injector.getInstance(UIThreadManager.class);
    }

    @Nonnull
    @Override
    public EventRouter getEventRouter() {
        return injector.getInstance(EventRouter.class, named("applicationEventRouter"));
    }

    @Nonnull
    @Override
    public ArtifactManager getArtifactManager() {
        return injector.getInstance(ArtifactManager.class);
    }

    @Nonnull
    @Override
    public ActionManager getActionManager() {
        return injector.getInstance(ActionManager.class);
    }

    @Nonnull
    @Override
    public AddonManager getAddonManager() {
        return injector.getInstance(AddonManager.class);
    }

    @Nonnull
    @Override
    public MVCGroupManager getMvcGroupManager() {
        return injector.getInstance(MVCGroupManager.class);
    }

    @Nonnull
    @Override
    public MessageSource getMessageSource() {
        return injector.getInstance(MessageSource.class, named("applicationMessageSource"));
    }

    @Nonnull
    @Override
    public ResourceResolver getResourceResolver() {
        return injector.getInstance(ResourceResolver.class, named("applicationResourceResolver"));
    }

    @Nonnull
    @Override
    public ResourceHandler getResourceHandler() {
        return injector.getInstance(ResourceHandler.class);
    }

    @Nonnull
    @Override
    public ResourceInjector getResourceInjector() {
        return injector.getInstance(ResourceInjector.class, named("applicationResourceInjector"));
    }

    @Nonnull
    @Override
    public Injector<?> getInjector() {
        return injector;
    }

    public void setInjector(@Nonnull Injector<?> injector) {
        this.injector = requireNonNull(injector, "Argument 'injector' must not be null");
        this.injector.injectMembers(this);
        addShutdownHandler(getWindowManager());
        MVCGroupExceptionHandler.registerWith(this);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <W> WindowManager<W> getWindowManager() {
        return injector.getInstance(WindowManager.class);
    }

    protected ApplicationConfigurer getApplicationConfigurer() {
        return injector.getInstance(ApplicationConfigurer.class);
    }

    public void initialize() {
        if (getPhase() == ApplicationPhase.INITIALIZE) {
            getApplicationConfigurer().init();
        }
    }

    public void ready() {
        if (getPhase() != ApplicationPhase.STARTUP) { return; }

        showStartingWindow();

        setPhase(ApplicationPhase.READY);
        event(ApplicationEvent.READY_START, asList(this));
        getApplicationConfigurer().runLifecycleHandler(Lifecycle.READY);
        event(ApplicationEvent.READY_END, asList(this));
        setPhase(ApplicationPhase.MAIN);
    }

    protected void showStartingWindow() {
        Object startingWindow = getWindowManager().getStartingWindow();
        if (startingWindow != null) {
            getWindowManager().show(startingWindow);
        }
    }

    public boolean canShutdown() {
        event(ApplicationEvent.SHUTDOWN_REQUESTED, asList(this));
        synchronized (shutdownLock) {
            for (ShutdownHandler handler : shutdownHandlers) {
                if (!handler.canShutdown(this)) {
                    event(ApplicationEvent.SHUTDOWN_ABORTED, asList(this));
                    try {
                        log.debug("Shutdown aborted by {}", handler);
                    } catch (UnsupportedOperationException uoe) {
                        log.debug("Shutdown aborted by a handler");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public boolean shutdown() {
        // avoids reentrant calls to shutdown()
        // once permission to quit has been granted
        if (getPhase() == ApplicationPhase.SHUTDOWN) { return false; }

        if (!canShutdown()) { return false; }
        log.info("Shutdown is in process");

        // signal that shutdown is in process
        setPhase(ApplicationPhase.SHUTDOWN);

        // stage 1 - alert all app event handlers
        // wait for all handlers to complete before proceeding
        // with stage #2 if and only if the current thread is
        // the ui thread
        log.debug("Shutdown stage 1: notify all event listeners");
        if (getEventRouter().isEventPublishingEnabled()) {
            final CountDownLatch latch = new CountDownLatch(getUIThreadManager().isUIThread() ? 1 : 0);
            getEventRouter().addEventListener(ApplicationEvent.SHUTDOWN_START.getName(), new RunnableWithArgs() {
                @Override
                public void run(@Nullable Object... args) {
                    latch.countDown();
                }
            });
            event(ApplicationEvent.SHUTDOWN_START, asList(this));
            try {
                latch.await();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // stage 2 - alert all shutdown handlers
        log.debug("Shutdown stage 2: notify all shutdown handlers");
        synchronized (shutdownLock) {
            for (ShutdownHandler handler : shutdownHandlers) {
                handler.onShutdown(this);
            }
        }

        // stage 3 - destroy all mvc groups
        log.debug("Shutdown stage 3: destroy all MVC groups");
        List<String> mvcIds = new ArrayList<>();
        mvcIds.addAll(getMvcGroupManager().getGroups().keySet());
        for (String id : mvcIds) {
            getMvcGroupManager().destroyMVCGroup(id);
        }

        // stage 4 - call shutdown script
        log.debug("Shutdown stage 4: execute Shutdown script");
        getApplicationConfigurer().runLifecycleHandler(Lifecycle.SHUTDOWN);

        injector.getInstance(ExecutorServiceManager.class).shutdownAll();
        injector.close();

        return true;
    }

    @SuppressWarnings("unchecked")
    public void startup() {
        if (getPhase() != ApplicationPhase.INITIALIZE) { return; }

        setPhase(ApplicationPhase.STARTUP);
        event(ApplicationEvent.STARTUP_START, asList(this));

        Object startupGroups = getConfiguration().get("application.startupGroups", null);
        if (startupGroups instanceof List) {
            log.info("Initializing all startup groups: {}", startupGroups);

            for (String groupName : (List<String>) startupGroups) {
                getMvcGroupManager().createMVC(groupName.trim());
            }
        } else if (startupGroups != null && startupGroups.getClass().isArray()) {
            Object[] groups = (Object[]) startupGroups;
            log.info("Initializing all startup groups: {}", Arrays.toString(groups));

            for (Object groupName : groups) {
                getMvcGroupManager().createMVC(String.valueOf(groupName).trim());
            }
        } else if (startupGroups != null && startupGroups instanceof CharSequence) {
            String[] groups = (String.valueOf(startupGroups)).split(",");
            log.info("Initializing all startup groups: {}", Arrays.toString(groups));

            for (String groupName : groups) {
                getMvcGroupManager().createMVC(groupName.trim());
            }
        }

        for (Map.Entry<String, BasiliskAddon> e : getAddonManager().getAddons().entrySet()) {
            List<String> groups = e.getValue().getStartupGroups();
            if (groups.isEmpty()) {
                continue;
            }
            log.info("Initializing all {} startup groups: {}", e.getKey(), groups);
            Map<String, Map<String, Object>> mvcGroups = e.getValue().getMvcGroups();
            for (String groupName : groups) {
                if (mvcGroups.containsKey(groupName)) {
                    getMvcGroupManager().createMVC(groupName.trim());
                }
            }
        }

        getApplicationConfigurer().runLifecycleHandler(Lifecycle.STARTUP);

        event(ApplicationEvent.STARTUP_END, asList(this));
    }

    protected void event(@Nonnull ApplicationEvent event, @Nullable List<?> args) {
        getEventRouter().publishEvent(event.getName(), args);
    }
}
