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
package basilisk.javafx;

import basilisk.core.ApplicationClassLoader;
import basilisk.core.ApplicationConfigurer;
import basilisk.core.ApplicationEvent;
import basilisk.core.BasiliskApplication;
import basilisk.core.Configuration;
import basilisk.core.Context;
import basilisk.core.ExecutorServiceManager;
import basilisk.core.RunnableWithArgs;
import basilisk.core.ShutdownHandler;
import basilisk.core.addon.AddonManager;
import basilisk.core.addon.BasiliskAddon;
import basilisk.core.artifact.ArtifactManager;
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
import com.googlecode.openbeans.PropertyChangeEvent;
import com.googlecode.openbeans.PropertyChangeListener;
import com.googlecode.openbeans.PropertyChangeSupport;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.kordamp.basilisk.runtime.core.MVCGroupExceptionHandler;
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
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of {@code BasiliskApplication} that runs in applet mode.
 *
 * @author Andres Almiray
 */
public abstract class AbstractJavaFXBasiliskApplication extends Application implements BasiliskApplication {
    public static final String[] EMPTY_ARGS = new String[0];
    private static final String ERROR_SHUTDOWN_HANDLER_NULL = "Argument 'shutdownHandler' must not be null";
    protected final Object[] lock = new Object[0];
    protected final PropertyChangeSupport pcs;
    private final List<ShutdownHandler> shutdownHandlers = new ArrayList<>();
    private final Object shutdownLock = new Object();
    private final Logger log;
    private Locale locale = Locale.getDefault();
    private ApplicationPhase phase = ApplicationPhase.INITIALIZE;
    private String[] startupArgs;
    private Injector<?> injector;

    public AbstractJavaFXBasiliskApplication() {
        this(EMPTY_ARGS);
    }

    public AbstractJavaFXBasiliskApplication(@Nonnull String[] args) {
        requireNonNull(args, "Argument 'args' must not be null");
        pcs = new PropertyChangeSupport(this);
        startupArgs = Arrays.copyOf(args, args.length);
        log = LoggerFactory.getLogger(getClass());
    }

    // ------------------------------------------------------

    @Override
    public void start(Stage stage) throws Exception {
        stage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                AbstractJavaFXBasiliskApplication.this.shutdown();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        shutdown();
    }

    public void addPropertyChangeListener(@Nullable PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(@Nullable String propertyName, @Nullable PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(@Nullable PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(@Nullable String propertyName, @Nullable PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Nonnull
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Nonnull
    public PropertyChangeListener[] getPropertyChangeListeners(@Nullable String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    protected void firePropertyChange(@Nonnull PropertyChangeEvent event) {
        pcs.firePropertyChange(requireNonNull(event, "Argument 'event' must not be null"));
    }

    protected void firePropertyChange(@Nonnull String propertyName, @Nullable Object oldValue, @Nullable Object newValue) {
        pcs.firePropertyChange(requireNonBlank(propertyName, "Argument 'propertyName' must not be blank"), oldValue, newValue);
    }

    // ------------------------------------------------------

    @Nonnull
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(@Nonnull Locale locale) {
        Locale oldValue = this.locale;
        this.locale = locale;
        Locale.setDefault(locale);
        firePropertyChange(PROPERTY_LOCALE, oldValue, locale);
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
        if (!shutdownHandlers.contains(handler)) shutdownHandlers.add(handler);
    }

    public void removeShutdownHandler(@Nonnull ShutdownHandler handler) {
        requireNonNull(handler, ERROR_SHUTDOWN_HANDLER_NULL);
        shutdownHandlers.remove(handler);
    }

    @Nonnull
    public ApplicationPhase getPhase() {
        synchronized (lock) {
            return this.phase;
        }
    }

    protected void setPhase(@Nonnull ApplicationPhase phase) {
        requireNonNull(phase, "Argument 'phase' must not be null");
        synchronized (lock) {
            firePropertyChange(PROPERTY_PHASE, this.phase, this.phase = phase);
        }
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
        return injector.getInstance(Configuration.class);
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
        this.injector = requireNonNull(injector, "Argument 'injector' cannot be null");
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
            Parameters parameters = getParameters();
            if (parameters != null && parameters.getRaw().size() > 0) {
                int length = parameters.getRaw().size();
                startupArgs = new String[length];
                System.arraycopy(parameters.getRaw().toArray(), 0, startupArgs, 0, length);
            }

            getApplicationConfigurer().init();
        }
    }

    public void ready() {
        if (getPhase() != ApplicationPhase.STARTUP) return;

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
                    if (log.isDebugEnabled()) {
                        try {
                            log.debug("Shutdown aborted by " + handler);
                        } catch (UnsupportedOperationException uoe) {
                            log.debug("Shutdown aborted by a handler");
                        }
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
        if (getPhase() == ApplicationPhase.SHUTDOWN) return false;

        if (!canShutdown()) return false;
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
        if (getPhase() != ApplicationPhase.INITIALIZE) return;

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
        } else if (startupGroups != null && startupGroups instanceof String) {
            String[] groups = ((String) startupGroups).split(",");
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
            log.info("Initializing all {} startup groups: {}", e.getKey(), groups);
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
