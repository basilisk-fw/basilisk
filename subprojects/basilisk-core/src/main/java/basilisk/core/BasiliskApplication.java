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
package basilisk.core;

import basilisk.core.addon.AddonManager;
import basilisk.core.artifact.ArtifactManager;
import basilisk.core.controller.ActionManager;
import basilisk.core.env.ApplicationPhase;
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
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

/**
 * Defines the basic contract of a Basilisk application.<p>
 *
 * @author Danno Ferrin
 * @author Andres Almiray
 */
public interface BasiliskApplication {
    String PROPERTY_LOCALE = "locale";
    String PROPERTY_PHASE = "phase";

    @Nonnull
    Object createApplicationContainer(@Nonnull Map<String, Object> attributes);

    @Nonnull
    ApplicationClassLoader getApplicationClassLoader();

    @Nonnull
    Configuration getConfiguration();

    @Nonnull
    UIThreadManager getUIThreadManager();

    @Nonnull
    EventRouter getEventRouter();

    @Nonnull
    ArtifactManager getArtifactManager();

    @Nonnull
    ActionManager getActionManager();

    @Nonnull
    AddonManager getAddonManager();

    @Nonnull
    MVCGroupManager getMvcGroupManager();

    @Nonnull
    MessageSource getMessageSource();

    @Nonnull
    ResourceResolver getResourceResolver();

    @Nonnull
    ResourceHandler getResourceHandler();

    @Nonnull
    ResourceInjector getResourceInjector();

    @Nonnull
    Injector<?> getInjector();

    @Nonnull
    Context getContext();

    @Nonnull
    <W> WindowManager<W> getWindowManager();

    // --== Lifecycle ==--

    /**
     * Executes the 'Initialize' life cycle phase.
     */
    void initialize();

    /**
     * Executes the 'Startup' life cycle phase.
     */
    void startup();

    /**
     * Executes the 'Ready' life cycle phase.
     */
    void ready();

    /**
     * Executes the 'Shutdown' life cycle phase.
     *
     * @return false if the shutdown sequence was aborted
     */
    boolean shutdown();

    /**
     * Queries any available ShutdownHandlers.
     *
     * @return true if the shutdown sequence can proceed, false otherwise
     */
    boolean canShutdown();

    /**
     * Registers a ShutdownHandler on this application
     *
     * @param handler the shutdown handler to be registered; null and/or
     *                duplicated values should be ignored
     */
    void addShutdownHandler(@Nonnull ShutdownHandler handler);

    /**
     * Removes a ShutdownHandler from this application
     *
     * @param handler the shutdown handler to be removed; null and/or
     *                duplicated values should be ignored
     */
    void removeShutdownHandler(@Nonnull ShutdownHandler handler);

    // --== Properties ==--

    /**
     * Gets the application locale.
     *
     * @return the current Locale used by the application. Never returns null.
     */
    @Nonnull
    Locale getLocale();

    /**
     * Sets the application locale.<p>
     * This is a bound property.
     *
     * @param locale the Locale value to use
     */
    void setLocale(@Nonnull Locale locale);

    /**
     * Sets the application locale.<p>
     * This is a bound property.
     *
     * @param locale a literal representation of a Locale
     */
    void setLocaleAsString(@Nullable String locale);

    /**
     * Returns the current phase.
     *
     * @return returns the current ApplicationPhase. Never returns null.
     */
    @Nonnull
    ApplicationPhase getPhase();

    /**
     * Returns the arguments set on the command line (if any).<p>
     *
     * @return an array of command line arguments. Never returns null.
     */
    @Nonnull
    String[] getStartupArgs();

    /**
     * Returns a Logger instance suitable for this application.
     *
     * @return a Logger instance.
     */
    @Nonnull
    Logger getLog();

    /**
     * Returns an observable for the {@code locale} property.
     *
     * @return an observable for the {@code locale} property.
     *
     * @since 0.4.0
     */
    @Nonnull
    ObjectProperty<Locale> localeProperty();

    /**
     * Returns an observable for the {@code applicationPhase} property.
     *
     * @return an observable for the {@code applicationPhase} property.
     *
     * @since 0.4.0
     */
    @Nonnull
    ReadOnlyObjectProperty<ApplicationPhase> phaseProperty();
}
