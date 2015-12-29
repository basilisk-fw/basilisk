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
package org.kordamp.basilisk.runtime.core

import basilisk.core.BasiliskApplication
import basilisk.core.ExceptionHandler
import basilisk.core.ExecutorServiceManager
import basilisk.core.LifecycleHandler
import basilisk.core.threading.ThreadingHandler
import basilisk.core.threading.UIThreadManager
import basilisk.util.AnnotationUtils
import com.google.guiceberry.GuiceBerryModule
import com.google.guiceberry.junit4.GuiceBerryRule
import com.google.inject.AbstractModule
import org.junit.Rule
import org.kordamp.basilisk.runtime.core.threading.DefaultExecutorServiceProvider
import org.kordamp.basilisk.runtime.core.threading.UIThreadManagerTestSupport
import spock.lang.Specification

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class AbstractLifecycleHandlerSpec extends Specification {
    @Rule
    final GuiceBerryRule guiceBerry = new GuiceBerryRule(TestModule)

    @Inject
    private LifecycleHandler lifecycleHandler

    def 'Query if UI thread'() {
        expect:
        !lifecycleHandler.UIThread
    }

    def 'Execute callable inside UI sync'() {
        given:
        boolean invoked = false

        when:
        invoked = lifecycleHandler.runInsideUISync({
            true
        } as Callable)

        then:
        invoked
    }

    def 'Execute runnable inside UI sync'() {
        given:
        boolean invoked = false

        when:
        lifecycleHandler.runInsideUISync {
            invoked = true
        }

        then:
        invoked
    }

    def 'Execute runnable inside UI async'() {
        given:
        boolean invoked = false

        when:
        lifecycleHandler.runInsideUIAsync {
            invoked = true
        }

        then:
        invoked
    }

    def 'Execute runnable outside UI'() {
        given:
        boolean invoked = false

        when:
        lifecycleHandler.runOutsideUI() {
            invoked = true
        }

        then:
        invoked
    }

    def 'Execute future'() {
        given:
        boolean invoked = false

        when:
        Future future = lifecycleHandler.runFuture {
            invoked = true
        }
        future.get()

        then:
        invoked
    }

    def 'Execute future with ExecutorService'() {
        given:
        boolean invoked = false

        when:
        Future future = lifecycleHandler.runFuture(Executors.newFixedThreadPool(1)) {
            invoked = true
        }
        future.get()

        then:
        invoked
    }

    static final class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new GuiceBerryModule())
            bind(ExecutorServiceManager).to(DefaultExecutorServiceManager).in(Singleton)
            bind(UIThreadManager).to(UIThreadManagerTestSupport).in(Singleton)
            bind(ThreadingHandler).to(TestThreadingHandler).in(Singleton)
            bind(LifecycleHandler).to(TestLifecycleHandler).in(Singleton)
            bind(ExceptionHandler).toProvider(ExceptionHandlerProvider).in(Singleton)
            bind(ExecutorService).annotatedWith(AnnotationUtils.named('defaultExecutorService')).toProvider(DefaultExecutorServiceProvider).in(Singleton)
            bind(BasiliskApplication).to(TestBasiliskApplication).in(Singleton)
        }
    }

    static class TestLifecycleHandler extends AbstractLifecycleHandler {
        @Inject
        TestLifecycleHandler(@Nonnull BasiliskApplication application) {
            super(application)
        }

        @Override
        void execute() {

        }
    }

    static class TestBasiliskApplication extends AbstractBasiliskApplication {
        @Inject
        private UIThreadManager uiThreadManager

        TestBasiliskApplication() {
        }

        TestBasiliskApplication(String[] args) {
            super(args)
        }

        @Nonnull
        @Override
        UIThreadManager getUIThreadManager() {
            this.uiThreadManager
        }

        @Nonnull
        @Override
        Object createApplicationContainer(@Nonnull Map<String, Object> attributes) {
            return null
        }
    }
}
