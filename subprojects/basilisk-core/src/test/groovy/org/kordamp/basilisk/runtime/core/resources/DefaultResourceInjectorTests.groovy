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
package org.kordamp.basilisk.runtime.core.resources

import basilisk.core.ApplicationClassLoader
import basilisk.core.editors.IntegerPropertyEditor
import basilisk.core.editors.PropertyEditorResolver
import basilisk.core.editors.StringPropertyEditor
import basilisk.core.injection.Injector
import basilisk.core.resources.ResourceHandler
import basilisk.core.resources.ResourceInjector
import basilisk.core.resources.ResourceResolver
import basilisk.util.AnnotationUtils
import basilisk.util.CompositeResourceBundleBuilder
import basilisk.util.Instantiator
import basilisk.util.ResourceBundleLoader
import com.google.guiceberry.GuiceBerryModule
import com.google.guiceberry.junit4.GuiceBerryRule
import com.google.inject.AbstractModule
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.kordamp.basilisk.runtime.core.DefaultApplicationClassLoader
import org.kordamp.basilisk.runtime.util.DefaultCompositeResourceBundleBuilder
import org.kordamp.basilisk.runtime.util.DefaultInstantiator
import org.kordamp.basilisk.runtime.util.PropertiesResourceBundleLoader

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import static com.google.inject.util.Providers.guicify
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class DefaultResourceInjectorTests {
    @Rule
    public final GuiceBerryRule guiceBerry = new GuiceBerryRule(TestModule)

    @Inject private CompositeResourceBundleBuilder bundleBuilder
    @Inject private Provider<Injector> injector
    @Inject @Named('properties') private ResourceBundleLoader propertiesResourceBundleLoader

    @Before
    void setupMethod() {
        when(injector.get().getInstances(ResourceBundleLoader)).thenReturn([propertiesResourceBundleLoader])
    }

    @BeforeClass
    public static void setup() {
        PropertyEditorResolver.clear()
        PropertyEditorResolver.registerEditor(String, StringPropertyEditor)
        PropertyEditorResolver.registerEditor(Integer, IntegerPropertyEditor)
        PropertyEditorResolver.registerEditor(int.class, IntegerPropertyEditor)
    }

    @AfterClass
    public static void cleanup() {
        PropertyEditorResolver.clear()
    }

    @Test
    void resolveAllFormatsByProperties() {
        ResourceResolver resourceResolver = new DefaultResourceResolver(bundleBuilder, 'org.kordamp.basilisk.runtime.core.resources.injector')
        ResourceInjector resourcesInjector = new DefaultResourceInjector(resourceResolver)
        Bean bean = new Bean()
        resourcesInjector.injectResources(bean)

        assert bean.@privateField == 'privateField'
        assert bean.@fieldBySetter == 'fieldBySetter'
        assert bean.@privateIntField == 42
        assert bean.@intFieldBySetter == 21
        assert bean.@fieldWithKey == 'no_args'
        assert bean.@fieldWithKeyAndArgs == 'with_args 1 2'
        assert bean.@fieldWithKeyNoArgsWithDefault == 'DEFAULT_NO_ARGS'
        assert bean.@fieldWithKeyWithArgsWithDefault == 'DEFAULT_WITH_ARGS'
        assert !bean.@notFound
    }

    static final class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new GuiceBerryModule())
            bind(ApplicationClassLoader).to(DefaultApplicationClassLoader).in(Singleton)
            bind(ResourceHandler).to(DefaultResourceHandler).in(Singleton)
            bind(CompositeResourceBundleBuilder).to(DefaultCompositeResourceBundleBuilder).in(Singleton)
            bind(Instantiator).to(DefaultInstantiator).in(Singleton)
            bind(ResourceBundleLoader).annotatedWith(AnnotationUtils.named('properties')).to(PropertiesResourceBundleLoader).in(Singleton)
            bind(Injector).toProvider(guicify({ mock(Injector) } as Provider<Injector>)).in(Singleton)
        }
    }
}
