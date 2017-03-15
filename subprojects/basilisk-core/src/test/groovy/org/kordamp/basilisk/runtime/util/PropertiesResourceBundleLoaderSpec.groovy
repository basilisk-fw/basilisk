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
package org.kordamp.basilisk.runtime.util

import com.google.guiceberry.GuiceBerryModule
import com.google.guiceberry.junit4.GuiceBerryRule
import com.google.inject.AbstractModule
import basilisk.core.ApplicationClassLoader
import basilisk.core.env.Environment
import basilisk.core.env.Metadata
import basilisk.core.resources.ResourceHandler
import basilisk.util.AnnotationUtils
import basilisk.util.PropertiesReader
import basilisk.util.ResourceBundleLoader
import org.kordamp.basilisk.runtime.core.DefaultApplicationClassLoader
import org.kordamp.basilisk.runtime.core.env.EnvironmentProvider
import org.kordamp.basilisk.runtime.core.env.MetadataProvider
import org.kordamp.basilisk.runtime.core.resources.DefaultResourceHandler
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import static com.google.inject.util.Providers.guicify

@Unroll
class PropertiesResourceBundleLoaderSpec extends Specification {
    @Rule
    final GuiceBerryRule guiceBerry = new GuiceBerryRule(TestModule)

    @Inject @Named('properties') private ResourceBundleLoader resourceBundleLoader

    def 'Load bundle and check #key = #value'() {
        when:
        Collection<ResourceBundle> bundles = resourceBundleLoader.load('org/kordamp/basilisk/runtime/util/PropertiesBundle')

        then:
        println bundles[0].keySet()
        bundles.size() == 1
        bundles[0].getString(key) == value

        where:
        key << ['string', 'integer', 'keys.bar', 'foo']
        value << ['string', '42', 'bar', 'dev']
    }

    static final class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new GuiceBerryModule())
            bind(ApplicationClassLoader).to(DefaultApplicationClassLoader).in(Singleton)
            bind(ResourceHandler).to(DefaultResourceHandler).in(Singleton)
            bind(PropertiesReader).toProvider(guicify(new PropertiesReader.Provider())).in(Singleton)
            bind(Metadata).toProvider(guicify(new MetadataProvider())).in(Singleton)
            bind(Environment).toProvider(guicify(new EnvironmentProvider())).in(Singleton)
            bind(ResourceBundleLoader).annotatedWith(AnnotationUtils.named('properties')).to(PropertiesResourceBundleLoader).in(Singleton)
        }
    }
}
