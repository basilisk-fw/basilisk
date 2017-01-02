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
package org.kordamp.basilisk.runtime.injection

import basilisk.core.ApplicationBootstrapper
import basilisk.core.BasiliskApplication
import basilisk.core.ExceptionHandler
import basilisk.core.ExecutorServiceManager
import basilisk.core.event.EventRouter
import basilisk.core.injection.Binding
import basilisk.core.injection.Module
import basilisk.core.threading.UIThreadManager
import basilisk.exceptions.ClosedInjectorException
import basilisk.exceptions.InstanceNotFoundException
import basilisk.exceptions.MembersInjectionException
import com.google.inject.CreationException
import org.kordamp.basilisk.runtime.core.BasiliskExceptionHandlerProvider
import org.kordamp.basilisk.runtime.core.DefaultExecutorServiceManager
import org.kordamp.basilisk.runtime.core.event.DefaultEventRouter
import org.kordamp.basilisk.runtime.core.injection.AbstractModule
import org.kordamp.basilisk.runtime.core.threading.DefaultExecutorServiceProvider
import org.kordamp.basilisk.runtime.core.threading.DefaultUIThreadManager
import spock.lang.Specification

import javax.annotation.Nonnull
import javax.inject.Provider
import javax.inject.Qualifier
import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.util.concurrent.ExecutorService

import static basilisk.util.AnnotationUtils.named
import static java.util.Collections.unmodifiableCollection

class GuiceInjectorFactorySpec extends Specification {
    void 'Verify bindings'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))

        then:
        injector.getInstances(Animal).size() == 6
        injector.getInstance(Animal, named('amber'))
        injector.getInstance(Animal, named('butch'))
        injector.getInstance(Animal, named('rufus'))
        injector.getInstance(Animal, named('happy'))
        injector.getQualifiedInstances(Animal).size() == 6
        named('amber') in injector.getQualifiedInstances(Animal).qualifier
        named('butch') in injector.getQualifiedInstances(Animal).qualifier
        named('rufus') in injector.getQualifiedInstances(Animal).qualifier
        named('happy') in injector.getQualifiedInstances(Animal).qualifier
        null in injector.getQualifiedInstances(Animal).qualifier
    }

    void 'Invalid bindings'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        factory.createInjector(application, createBindings(application, true))

        then:
        thrown(CreationException)
    }

    void 'Non existent binding results in exception (1)'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))
        injector.close()
        injector.getInstance(Dog)

        then:
        thrown(InstanceNotFoundException)
    }

    void 'Non existent binding results in exception (2)'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))
        injector.close()
        injector.getInstances(Dog)

        then:
        thrown(InstanceNotFoundException)
    }

    void 'Closed injector throws exception (1)'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))
        injector.close()
        injector.getInstance(Animal)

        then:
        Exception ex = thrown(InstanceNotFoundException)
        ex.cause instanceof ClosedInjectorException
    }

    void 'Closed injector throws exception (2)'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))
        injector.close()
        injector.getInstances(Animal)

        then:
        Exception ex = thrown(InstanceNotFoundException)
        ex.cause instanceof ClosedInjectorException
    }

    void 'Closed injector throws exception (3)'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))
        injector.close()
        injector.getInstance(Animal, named('happy'))

        then:
        Exception ex = thrown(InstanceNotFoundException)
        ex.cause instanceof ClosedInjectorException
    }

    void 'Closed injector throws exception (4)'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))
        injector.close()
        injector.getQualifiedInstances(Animal)

        then:
        Exception ex = thrown(InstanceNotFoundException)
        ex.cause instanceof ClosedInjectorException
    }

    void 'Closed injector throws exception (5)'() {
        given:
        GuiceInjectorFactory factory = new GuiceInjectorFactory()
        BasiliskApplication application = new TestBasiliskApplication()

        when:
        GuiceInjector injector = factory.createInjector(application, createBindings(application))
        injector.close()
        injector.injectMembers(new Dog())

        then:
        Exception ex = thrown(MembersInjectionException)
        ex.cause instanceof ClosedInjectorException
    }

    @Nonnull
    private
    static Iterable<Binding<?>> createBindings(BasiliskApplication application, boolean withFailure = false) {
        Map<ApplicationBootstrapper.Key, Binding<?>> map = new LinkedHashMap<>()

        for (Binding<?> binding : createModule(application).bindings) {
            map.put(ApplicationBootstrapper.Key.of(binding), binding)
        }
        if (withFailure) {
            Binding<?> binding = new InvalidBinding<>(Animal)
            map.put(ApplicationBootstrapper.Key.of(binding), binding)
        }

        return unmodifiableCollection(map.values())
    }

    @Nonnull
    private static Module createModule(BasiliskApplication application) {
        return new AbstractModule() {
            @Override
            protected void doConfigure() {
                bind(BasiliskApplication)
                    .toInstance(application)

                bind(ExecutorServiceManager)
                    .to(DefaultExecutorServiceManager)
                    .asSingleton()

                bind(EventRouter)
                    .withClassifier(named('applicationEventRouter'))
                    .to(DefaultEventRouter)
                    .asSingleton()

                bind(UIThreadManager)
                    .to(DefaultUIThreadManager)
                    .asSingleton()

                bind(ExecutorService)
                    .withClassifier(named('defaultExecutorService'))
                    .toProvider(DefaultExecutorServiceProvider)
                    .asSingleton()

                bind(ExceptionHandler)
                    .toProvider(BasiliskExceptionHandlerProvider)
                    .asSingleton()

                bind(Animal).to(Dog).asSingleton()
                bind(Animal).withClassifier(Special).to(Dog).asSingleton()
                bind(Animal).withClassifier(named('amber')).to(Dog).asSingleton()
                bind(Animal).withClassifier(named('butch')).toProvider(DogProvider).asSingleton()
                bind(Animal).withClassifier(named('rufus')).toProvider(DogProvider).asSingleton()
                bind(Animal).withClassifier(named('happy')).toInstance(new Dog())
            }
        }
    }
}

class InvalidBinding<T> implements Binding<T> {
    final Class<T> source

    InvalidBinding(Class<T> source) {
        this.source = source
    }

    @Nonnull
    @Override
    Class<T> getSource() {
        return source
    }

    @Override
    Class<? extends Annotation> getClassifierType() {
        return null
    }

    @Override
    Annotation getClassifier() {
        return null
    }

    @Override
    boolean isSingleton() {
        return true
    }
}

interface Animal {}

class Dog implements Animal {}

class DogProvider implements Provider<Dog> {
    @Override
    Dog get() {
        new Dog()
    }
}

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Special {}
