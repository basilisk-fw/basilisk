/*
 * Copyright 2008-2015 the original author or authors.
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
package integration;

import basilisk.core.LifecycleHandler;
import basilisk.core.addon.BasiliskAddon;
import basilisk.core.controller.ActionHandler;
import basilisk.core.env.Lifecycle;
import basilisk.core.i18n.MessageSource;
import basilisk.core.resources.ResourceResolver;
import org.kordamp.basilisk.runtime.core.LifecycleHandlerProvider;
import org.kordamp.basilisk.runtime.core.i18n.MessageSourceProvider;
import org.kordamp.basilisk.runtime.core.injection.AbstractModule;
import org.kordamp.basilisk.runtime.core.resources.ResourceResolverProvider;
import org.kordamp.basilisk.runtime.util.ResourceBundleProvider;

import javax.inject.Named;
import java.util.ResourceBundle;

import static basilisk.util.AnnotationUtils.named;

@Named("integration")
public class IntegrationModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        bind(ResourceBundle.class)
            .withClassifier(named("applicationResourceBundle"))
            .toProvider(new ResourceBundleProvider("integration.Config"))
            .asSingleton();

        bind(ResourceResolver.class)
            .withClassifier(named("applicationResourceResolver"))
            .toProvider(new ResourceResolverProvider("integration.resources"))
            .asSingleton();

        bind(MessageSource.class)
            .withClassifier(named("applicationMessageSource"))
            .toProvider(new MessageSourceProvider("integration.messages"))
            .asSingleton();

        for (Lifecycle lifecycle : Lifecycle.values()) {
            bind(LifecycleHandler.class)
                .withClassifier(named(lifecycle.getName()))
                .toProvider(new LifecycleHandlerProvider("integration.Integration" + lifecycle.getName()))
                .asSingleton();
        }

        bind(BasiliskAddon.class)
            .to(IntegrationAddon.class)
            .asSingleton();

        bind(BasiliskAddon.class)
            .to(GroupsAddon.class)
            .asSingleton();

        bind(ActionHandler.class)
            .to(InvokeActionHandler.class)
            .asSingleton();
    }
}
