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
package org.kordamp.basilisk.runtime.core;

import basilisk.core.ApplicationClassLoader;
import basilisk.core.ApplicationConfigurer;
import basilisk.core.Configuration;
import basilisk.core.Context;
import basilisk.core.ContextFactory;
import basilisk.core.ExceptionHandler;
import basilisk.core.ExecutorServiceManager;
import basilisk.core.LifecycleHandler;
import basilisk.core.PlatformHandler;
import basilisk.core.addon.AddonManager;
import basilisk.core.artifact.ArtifactHandler;
import basilisk.core.artifact.ArtifactManager;
import basilisk.core.controller.ActionManager;
import basilisk.core.env.Environment;
import basilisk.core.env.Lifecycle;
import basilisk.core.env.Metadata;
import basilisk.core.env.RunMode;
import basilisk.core.event.EventHandler;
import basilisk.core.event.EventRouter;
import basilisk.core.i18n.MessageSource;
import basilisk.core.mvc.MVCGroupConfigurationFactory;
import basilisk.core.mvc.MVCGroupFactory;
import basilisk.core.mvc.MVCGroupManager;
import basilisk.core.resources.ResourceHandler;
import basilisk.core.resources.ResourceInjector;
import basilisk.core.resources.ResourceResolver;
import basilisk.core.threading.UIThreadManager;
import basilisk.core.view.WindowManager;
import basilisk.util.CompositeResourceBundleBuilder;
import org.kordamp.basilisk.runtime.core.addon.DefaultAddonManager;
import org.kordamp.basilisk.runtime.core.artifact.ControllerArtifactHandler;
import org.kordamp.basilisk.runtime.core.artifact.DefaultArtifactManager;
import org.kordamp.basilisk.runtime.core.artifact.ModelArtifactHandler;
import org.kordamp.basilisk.runtime.core.artifact.ServiceArtifactHandler;
import org.kordamp.basilisk.runtime.core.artifact.ViewArtifactHandler;
import org.kordamp.basilisk.runtime.core.controller.DefaultActionManager;
import org.kordamp.basilisk.runtime.core.env.EnvironmentProvider;
import org.kordamp.basilisk.runtime.core.env.MetadataProvider;
import org.kordamp.basilisk.runtime.core.env.RunModeProvider;
import org.kordamp.basilisk.runtime.core.event.DefaultEventHandler;
import org.kordamp.basilisk.runtime.core.event.DefaultEventRouter;
import org.kordamp.basilisk.runtime.core.i18n.DefaultMessageSourceDecoratorFactory;
import org.kordamp.basilisk.runtime.core.i18n.MessageSourceDecoratorFactory;
import org.kordamp.basilisk.runtime.core.i18n.MessageSourceProvider;
import org.kordamp.basilisk.runtime.core.injection.AbstractModule;
import org.kordamp.basilisk.runtime.core.mvc.DefaultMVCGroupConfigurationFactory;
import org.kordamp.basilisk.runtime.core.mvc.DefaultMVCGroupFactory;
import org.kordamp.basilisk.runtime.core.mvc.DefaultMVCGroupManager;
import org.kordamp.basilisk.runtime.core.resources.DefaultApplicationResourceInjector;
import org.kordamp.basilisk.runtime.core.resources.DefaultResourceHandler;
import org.kordamp.basilisk.runtime.core.resources.DefaultResourceResolverDecoratorFactory;
import org.kordamp.basilisk.runtime.core.resources.ResourceResolverDecoratorFactory;
import org.kordamp.basilisk.runtime.core.resources.ResourceResolverProvider;
import org.kordamp.basilisk.runtime.core.threading.DefaultExecutorServiceProvider;
import org.kordamp.basilisk.runtime.core.threading.DefaultUIThreadManager;
import org.kordamp.basilisk.runtime.core.view.NoopWindowManager;
import org.kordamp.basilisk.runtime.util.DefaultCompositeResourceBundleBuilder;
import org.kordamp.basilisk.runtime.util.ResourceBundleProvider;

import javax.inject.Named;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import static basilisk.util.AnnotationUtils.named;

/**
 * @author Andres Almiray
 */
@Named("core")
public class DefaultApplicationModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(ApplicationClassLoader.class)
            .to(DefaultApplicationClassLoader.class)
            .asSingleton();

        bind(Metadata.class)
            .toProvider(MetadataProvider.class)
            .asSingleton();

        bind(RunMode.class)
            .toProvider(RunModeProvider.class)
            .asSingleton();

        bind(Environment.class)
            .toProvider(EnvironmentProvider.class)
            .asSingleton();

        bind(ContextFactory.class)
            .to(DefaultContextFactory.class)
            .asSingleton();

        bind(Context.class)
            .withClassifier(named("applicationContext"))
            .toProvider(DefaultContextProvider.class)
            .asSingleton();

        bind(ApplicationConfigurer.class)
            .to(DefaultApplicationConfigurer.class)
            .asSingleton();

        bind(ResourceHandler.class)
            .to(DefaultResourceHandler.class)
            .asSingleton();

        bind(CompositeResourceBundleBuilder.class)
            .to(DefaultCompositeResourceBundleBuilder.class)
            .asSingleton();

        bind(ResourceBundle.class)
            .withClassifier(named("applicationResourceBundle"))
            .toProvider(new ResourceBundleProvider("Config"))
            .asSingleton();

        bind(ConfigurationDecoratorFactory.class)
            .to(DefaultConfigurationDecoratorFactory.class);

        bind(Configuration.class)
            .toProvider(ResourceBundleConfigurationProvider.class)
            .asSingleton();

        bind(ExecutorServiceManager.class)
            .to(DefaultExecutorServiceManager.class)
            .asSingleton();

        bind(EventRouter.class)
            .withClassifier(named("applicationEventRouter"))
            .to(DefaultEventRouter.class)
            .asSingleton();

        bind(EventRouter.class)
            .to(DefaultEventRouter.class);

        bind(ResourceResolverDecoratorFactory.class)
            .to(DefaultResourceResolverDecoratorFactory.class);

        bind(MessageSourceDecoratorFactory.class)
            .to(DefaultMessageSourceDecoratorFactory.class);

        bind(ResourceResolver.class)
            .withClassifier(named("applicationResourceResolver"))
            .toProvider(new ResourceResolverProvider("resources"))
            .asSingleton();

        bind(MessageSource.class)
            .withClassifier(named("applicationMessageSource"))
            .toProvider(new MessageSourceProvider("messages"))
            .asSingleton();

        bind(ResourceInjector.class)
            .withClassifier(named("applicationResourceInjector"))
            .to(DefaultApplicationResourceInjector.class)
            .asSingleton();

        bind(ExecutorService.class)
            .withClassifier(named("defaultExecutorService"))
            .toProvider(DefaultExecutorServiceProvider.class)
            .asSingleton();

        bind(UIThreadManager.class)
            .to(DefaultUIThreadManager.class)
            .asSingleton();

        bind(MVCGroupConfigurationFactory.class)
            .to(DefaultMVCGroupConfigurationFactory.class)
            .asSingleton();

        bind(MVCGroupFactory.class)
            .to(DefaultMVCGroupFactory.class)
            .asSingleton();

        bind(MVCGroupManager.class)
            .to(DefaultMVCGroupManager.class)
            .asSingleton();

        for (Lifecycle lifecycle : Lifecycle.values()) {
            bind(LifecycleHandler.class)
                .withClassifier(named(lifecycle.getName()))
                .toProvider(new LifecycleHandlerProvider(lifecycle.getName()))
                .asSingleton();
        }

        bind(WindowManager.class)
            .to(NoopWindowManager.class)
            .asSingleton();

        bind(ActionManager.class)
            .to(DefaultActionManager.class)
            .asSingleton();

        bind(ArtifactManager.class)
            .to(DefaultArtifactManager.class)
            .asSingleton();

        bind(ArtifactHandler.class)
            .to(ModelArtifactHandler.class)
            .asSingleton();

        bind(ArtifactHandler.class)
            .to(ViewArtifactHandler.class)
            .asSingleton();

        bind(ArtifactHandler.class)
            .to(ControllerArtifactHandler.class)
            .asSingleton();

        bind(ArtifactHandler.class)
            .to(ServiceArtifactHandler.class)
            .asSingleton();

        bind(PlatformHandler.class)
            .toProvider(PlatformHandlerProvider.class)
            .asSingleton();

        bind(AddonManager.class)
            .to(DefaultAddonManager.class)
            .asSingleton();

        bind(EventHandler.class)
            .to(DefaultEventHandler.class)
            .asSingleton();

        bind(ExceptionHandler.class)
            .toProvider(BasiliskExceptionHandlerProvider.class)
            .asSingleton();
        // end::bindings[]
    }
}
