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
import basilisk.core.BasiliskApplication;
import basilisk.core.artifact.BasiliskService;
import basilisk.core.env.BasiliskEnvironment;
import basilisk.core.injection.Binding;
import basilisk.core.injection.Injector;
import basilisk.core.injection.InjectorFactory;
import basilisk.core.injection.Module;
import basilisk.util.BasiliskClassUtils;
import basilisk.util.ServiceLoaderUtils;
import org.kordamp.basilisk.runtime.core.injection.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static basilisk.core.BasiliskExceptionHandler.sanitize;
import static basilisk.util.AnnotationUtils.sortByDependencies;
import static basilisk.util.ServiceLoaderUtils.load;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractApplicationBootstrapper implements ApplicationBootstrapper {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultApplicationBootstrapper.class);
    private static final String INJECTOR = "injector";
    private static final String BASILISK_PATH = "META-INF/basilisk";
    private static final String PROPERTIES = ".properties";
    protected final BasiliskApplication application;

    public AbstractApplicationBootstrapper(@Nonnull BasiliskApplication application) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
    }

    @Override
    public void bootstrap() throws Exception {
        // 1 initialize environment settings
        LOG.info("Basilisk {}", BasiliskEnvironment.getBasiliskVersion());
        LOG.info("Build: {}", BasiliskEnvironment.getBuildDateTime());
        LOG.info("Revision: {}", BasiliskEnvironment.getBuildRevision());
        LOG.info("JVM: {}", BasiliskEnvironment.getJvmVersion());
        LOG.info("OS: {}", BasiliskEnvironment.getOsVersion());

        // 2 create bindings
        LOG.debug("Creating module bindings");
        Iterable<Binding<?>> bindings = createBindings();

        if (LOG.isTraceEnabled()) {
            for (Binding<?> binding : bindings) {
                LOG.trace(binding.toString());
            }
        }

        // 3 create injector
        LOG.debug("Creating application injector");
        createInjector(bindings);
    }

    @Override
    public void run() {
        application.initialize();
        application.startup();
        application.ready();
    }

    @Nonnull
    protected Iterable<Binding<?>> createBindings() {
        Map<Key, Binding<?>> map = new LinkedHashMap<>();

        List<Module> modules = new ArrayList<>();
        createApplicationModule(modules);
        createArtifactsModule(modules);
        collectModuleBindings(modules);

        for (Module module : modules) {
            for (Binding<?> binding : module.getBindings()) {
                map.put(Key.of(binding), binding);
            }
        }

        return unmodifiableCollection(map.values());
    }

    protected void createArtifactsModule(@Nonnull List<Module> modules) {
        final List<Class<?>> classes = new ArrayList<>();
        load(getClass().getClassLoader(), BASILISK_PATH, new ServiceLoaderUtils.PathFilter() {
            @Override
            public boolean accept(@Nonnull String path) {
                return !path.endsWith(PROPERTIES);
            }
        }, new ServiceLoaderUtils.ResourceProcessor() {
            @Override
            public void process(@Nonnull ClassLoader classLoader, @Nonnull String line) {
                line = line.trim();
                try {
                    classes.add(classLoader.loadClass(line));
                } catch (ClassNotFoundException e) {
                    LOG.warn("'" + line + "' could not be resolved as a Class");
                }
            }
        });

        modules.add(new AbstractModule() {
            @Override
            protected void doConfigure() {
                for (Class<?> clazz : classes) {
                    if (BasiliskService.class.isAssignableFrom(clazz)) {
                        bind(clazz).asSingleton();
                    } else {
                        bind(clazz);
                    }
                }
            }
        });
    }

    protected void createApplicationModule(@Nonnull List<Module> modules) {
        modules.add(new AbstractModule() {
            @Override
            protected void doConfigure() {
                bind(BasiliskApplication.class)
                    .toInstance(application);
            }
        });
    }

    protected void collectModuleBindings(@Nonnull Collection<Module> modules) {
        List<Module> moduleInstances = loadModules();
        moduleInstances.add(0, new DefaultApplicationModule());
        Map<String, Module> sortedModules = sortModules(moduleInstances);
        for (Map.Entry<String, Module> entry : sortedModules.entrySet()) {
            LOG.debug("Loading module bindings from {}:{}", entry.getKey(), entry.getValue());
            modules.add(entry.getValue());
        }
    }

    @Nonnull
    protected Map<String, Module> sortModules(@Nonnull List<Module> moduleInstances) {
        return sortByDependencies(moduleInstances, "Module", "module");
    }

    @Nonnull
    protected abstract List<Module> loadModules();

    private void createInjector(@Nonnull Iterable<Binding<?>> bindings) throws Exception {
        ServiceLoader<InjectorFactory> serviceLoader = ServiceLoader.load(InjectorFactory.class);
        try {
            Iterator<InjectorFactory> iterator = serviceLoader.iterator();
            InjectorFactory injectorFactory = iterator.next();
            LOG.debug("Injector will be created by {}", injectorFactory);
            Injector<?> injector = injectorFactory.createInjector(application, bindings);
            BasiliskClassUtils.setProperty(application, INJECTOR, injector);
        } catch (Exception e) {
            LOG.error("An error occurred while initializing the injector", sanitize(e));
            throw e;
        }
    }
}
